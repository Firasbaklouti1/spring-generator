package com.firas.generator.service;

import com.firas.generator.model.*;
import com.firas.generator.model.AI.AIGeneratedTables;
import com.firas.generator.model.AI.AIGeneratedTablesRequest;
import com.firas.generator.model.AI.TableAction;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import io.reactivex.rxjava3.core.Flowable;

@Service
public class AIGeneratedTablesService {
    private static String USER_ID = "student";
    private static String NAME = "sql_table_assistent";


    // In-memory store for session context (optional, can be replaced with database)
    private final Map<String, List<Table>> sessionTables = new ConcurrentHashMap<>();

    // Maximum tables limit as defined in frontend
    private static final int MAX_TABLES = 50;

    // reuse object mapper
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Optionally cache the agent so we don't recreate it each request
    private final BaseAgent ROOT_AGENT = initAgent();

    private BaseAgent initAgent() {
        /*
         * Build a simple LLM agent. The instruction asks the model to return only valid JSON
         * with the structure the frontend expects:
         * {
         *   "sessionId": "optional-string",
         *   "actions": [ { "type": "create"|"edit"|"delete"|"replace", "tables": [...], "tableNames": [...], "newSchema": [...] } ],
         *   "explanation": "human readable explanation"
         * }
         *
         * IMPORTANT: You will likely need to tune this instruction for your specific model
         * and environment so the model reliably emits clean JSON.
         */
        String instruction =
                "You are an SQL schema assistant that outputs ONLY valid JSON matching the AIGeneratedTables structure.\n"
                        + "INPUT: A user prompt and currentTables (array of Table objects).\n"
                        + "OUTPUT: A JSON object with EXACTLY these three fields:\n"
                        + "1. sessionId: optional string (can be empty or null)\n"
                        + "2. actions: array of TableAction objects\n"
                        + "3. explanation: string describing what was done\n\n"
                        + "TABLE ACTION STRUCTURE:\n"
                        + "Each TableAction MUST have:\n"
                        + "- type: string (must be one of: \"create\", \"edit\", \"delete\", \"replace\")\n"
                        + "- ONE of the following based on type:\n"
                        + "  * For \"create\" or \"edit\": include \"tables\" array with Table objects\n"
                        + "  * For \"delete\": include \"tableNames\" array with string table names\n"
                        + "  * For \"replace\": include \"newSchema\" array with Table objects\n\n"
                        + "TABLE STRUCTURE:\n"
                        + "{\n"
                        + "  \"name\": \"table_name\",\n"
                        + "  \"className\": \"ClassName\",\n"
                        + "  \"columns\": [array of Column objects],\n"
                        + "  \"relationships\": [array of Relationship objects],\n"
                        + "  \"joinTable\": boolean\n"
                        + "}\n\n"
                        + "COLUMN STRUCTURE:\n"
                        + "{\n"
                        + "  \"name\": \"column_name\",\n"
                        + "  \"type\": \"SQL_TYPE\",\n"
                        + "  \"fieldName\": \"javaFieldName\",\n"
                        + "  \"javaType\": \"JavaType\",\n"
                        + "  \"primaryKey\": boolean,\n"
                        + "  \"autoIncrement\": boolean,\n"
                        + "  \"nullable\": boolean,\n"
                        + "  \"foreignKey\": boolean,\n"
                        + "  \"referencedTable\": \"string or null\",\n"
                        + "  \"referencedColumn\": \"string or null\",\n"
                        + "  \"unique\": boolean\n"
                        + "}\n\n"
                        + "RELATIONSHIP STRUCTURE:\n"
                        + "{\n"
                        + "  \"type\": \"ONE_TO_MANY|MANY_TO_ONE|ONE_TO_ONE|MANY_TO_MANY\",\n"
                        + "  \"sourceTable\": \"table_name\",\n"
                        + "  \"targetTable\": \"table_name\",\n"
                        + "  \"sourceColumn\": \"column_name\",\n"
                        + "  \"targetColumn\": \"column_name\",\n"
                        + "  \"joinTable\": \"string or null\",\n"
                        + "  \"mappedBy\": \"string or null\",\n"
                        + "  \"fieldName\": \"javaFieldName\",\n"
                        + "  \"targetClassName\": \"ClassName\"\n"
                        + "}\n\n"
                        + "CRITICAL RULES:\n"
                        + "1. Output ONLY the JSON object, no additional text, no markdown, no code fences\n"
                        + "2. NEVER nest action types like {\"create\": {...}}. Use {\"type\": \"create\", \"tables\": [...]}\n"
                        + "3. For relationships:\n"
                        + "   - ONE_TO_MANY: sourceTable has many targetTable records\n"
                        + "   - MANY_TO_ONE: many sourceTable records reference one targetTable\n"
                        + "   - For bidirectional relationships, use mappedBy appropriately\n"
                        + "   - For MANY_TO_MANY, set joinTable to the join table name\n"
                        + "4. For foreign keys:\n"
                        + "   - Set foreignKey: true\n"
                        + "   - Set referencedTable and referencedColumn\n"
                        + "   - Set nullable based on relationship optionality\n"
                        + "5. Primary key columns should have: primaryKey: true, autoIncrement: true (for IDs), unique: true\n"
                        + "6. Java types must match SQL types:\n"
                        + "   - VARCHAR/TEXT → String\n"
                        + "   - INT/INTEGER → Integer\n"
                        + "   - BIGINT → Long\n"
                        + "   - DECIMAL/NUMERIC → java.math.BigDecimal\n"
                        + "   - BOOLEAN → Boolean\n"
                        + "   - DATE → java.time.LocalDate\n"
                        + "   - TIMESTAMP → java.time.Instant or java.time.LocalDateTime\n"
                        + "7. Field names should be camelCase versions of column names\n"
                        + "\n"
                        + "EXAMPLE CORRECT FORMAT:\n"
                        + "{\n"
                        + "  \"sessionId\": \"session-123\",\n"
                        + "  \"actions\": [\n"
                        + "    {\n"
                        + "      \"type\": \"create\",\n"
                        + "      \"tables\": [\n"
                        + "        {\n"
                        + "          \"name\": \"users\",\n"
                        + "          \"className\": \"User\",\n"
                        + "          \"columns\": [\n"
                        + "            {\n"
                        + "              \"name\": \"id\",\n"
                        + "              \"type\": \"BIGINT\",\n"
                        + "              \"fieldName\": \"id\",\n"
                        + "              \"javaType\": \"Long\",\n"
                        + "              \"primaryKey\": true,\n"
                        + "              \"autoIncrement\": true,\n"
                        + "              \"nullable\": false,\n"
                        + "              \"foreignKey\": false,\n"
                        + "              \"referencedTable\": null,\n"
                        + "              \"referencedColumn\": null,\n"
                        + "              \"unique\": true\n"
                        + "            }\n"
                        + "          ],\n"
                        + "          \"relationships\": [],\n"
                        + "          \"joinTable\": false\n"
                        + "        }\n"
                        + "      ]\n"
                        + "    }\n"
                        + "  ],\n"
                        + "  \"explanation\": \"Created users table with primary key.\"\n"
                        + "}\n\n"
                        + "IMPORTANT: Return ONLY the JSON object. No extra text before or after.";

        return LlmAgent.builder()
                .name(NAME)
                // model id can be tuned; keep a commonly used one from docs
                .model("gemini-2.0-flash")
                .description("Agent to assist on SQL schema generation and modification.")
                .instruction(instruction)
                // no function tools required for this JSON-only assistant flow
                .build();
    }

    public AIGeneratedTables generateTables(AIGeneratedTablesRequest request) {
        String prompt = Optional.ofNullable(request.getPrompt()).orElse("");
        List<Table> currentTables = request.getCurrentTables() != null ?
                new ArrayList<>(request.getCurrentTables()) : new ArrayList<>();
        String sessionId = request.getSessionId();
        boolean allowDestructive = request.isAllowDestructive();

        // If sessionId is provided, try to get session context
        if (sessionId != null && sessionTables.containsKey(sessionId)) {
            // For conversation mode, you might want to merge with current session state
            currentTables = new ArrayList<>(sessionTables.get(sessionId));
        } else if (sessionId == null) {
            // Generate new session ID if not provided (we may override with agent provided sessionId)
            sessionId = "session-" + UUID.randomUUID().toString().substring(0, 8);
        }

        // Build a combined prompt that includes the user's prompt and the current schema as JSON
        String payload;
        try {
            String currentTablesJson = objectMapper.writeValueAsString(currentTables);
            // clearly label the payload for the model to find structured data
            payload = "USER REQUEST: " + prompt + "\n\n"
                    + "CURRENT SCHEMA: " + currentTablesJson + "\n\n"
                    + "INSTRUCTIONS: Analyze the user request and current schema. Return a JSON response "
                    + "in the exact AIGeneratedTables format described above. "
                    + "Apply appropriate schema changes. "
                    + "For destructive operations (delete, replace), ensure they're appropriate. "
                    + "Use sessionId to maintain conversation context if needed.\n\n"
                    + "RESPONSE FORMAT REQUIRED: Pure JSON matching AIGeneratedTables structure.";
        } catch (Exception e) {
            return new AIGeneratedTables(sessionId, Collections.emptyList(),
                    "Error serializing current tables: " + e.getMessage());
        }

        // Use ADK InMemoryRunner to run the agent and capture the final assistant response
        InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT);
        // create or reuse a session in ADK (this is local runtime session not your sessionTables id)
        Session session = runner.sessionService().createSession(NAME, USER_ID).blockingGet();

        Content userMsg = Content.fromParts(Part.fromText(payload));

        // We'll capture the final assistant output in this atomic ref
        AtomicReference<String> finalAssistantText = new AtomicReference<>("");
        try {
            Flowable<Event> events = runner.runAsync(USER_ID, session.id(), userMsg);

            // Process events and capture last finalResponse content (matching quickstart)
            events.blockingForEach(event -> {
                // the human-readable text is available via stringifyContent
                // we capture the final response (event.finalResponse())
                if (event.finalResponse()) {
                    // stringifyContent often contains the model assistant text
                    finalAssistantText.set(event.stringifyContent());
                }
            });
        } catch (Exception e) {
            return new AIGeneratedTables(sessionId, Collections.emptyList(),
                    "Error running ADK agent: " + e.getMessage());
        }

        String assistantOutput = finalAssistantText.get();
        if (assistantOutput == null || assistantOutput.trim().isEmpty()) {
            return new AIGeneratedTables(sessionId, Collections.emptyList(),
                    "AI produced no output.");
        }

        // Try to extract/parses JSON from assistantOutput. The instruction asked for pure JSON,
        // but the model might add ephemeral whitespace or ticks. We'll attempt to parse the first
        // JSON object found in the text by trying objectMapper.readValue.
        AIGeneratedTables aiResult = null;
        try {
            // attempt direct parse
            aiResult = objectMapper.readValue(assistantOutput, AIGeneratedTables.class);
        } catch (Exception e) {
            // try to find JSON substring (simple heuristic: first '{' to last '}' )
            try {
                int first = assistantOutput.indexOf('{');
                int last = assistantOutput.lastIndexOf('}');
                if (first >= 0 && last > first) {
                    String json = assistantOutput.substring(first, last + 1);
                    aiResult = objectMapper.readValue(json, AIGeneratedTables.class);
                } else {
                    // cannot find JSON — return helpful message including raw assistant output
                    return new AIGeneratedTables(sessionId, Collections.emptyList(),
                            "AI did not return valid JSON. Raw output:\n" + assistantOutput);
                }
            } catch (Exception ex2) {
                return new AIGeneratedTables(sessionId, Collections.emptyList(),
                        "Failed to parse AI JSON. Raw output:\n" + assistantOutput + "\nParse error: " + ex2.getMessage());
            }
        }

        // If the agent returned a sessionId, prefer that for session storage
        if (aiResult.getSessionId() != null && !aiResult.getSessionId().trim().isEmpty()) {
            sessionId = aiResult.getSessionId();
        }

        // ensure actions list not null
        List<TableAction> actions = aiResult.getActions() != null ? aiResult.getActions() : new ArrayList<>();

        // Update session context (apply the actions to the in-memory schema)
        try {
            updateSessionTables(sessionId, actions, currentTables);
        } catch (Exception e) {
            // reflect failure but still return agent's explanation
            return new AIGeneratedTables(sessionId, actions, "Agent explanation: " + aiResult.getExplanation()
                    + "\n\nError applying actions: " + e.getMessage());
        }

        // Return the parsed result (with possibly updated sessionId)
        return new AIGeneratedTables(sessionId, actions, aiResult.getExplanation());
    }

    /**
     * Applies the actions returned by AI to the currentTables and stores result in sessionTables map.
     *
     * Actions supported:
     * - create: has tables -> append (skip duplicates by name)
     * - edit: has tables -> find by name and replace/merge
     * - delete: has tableNames -> remove by name (requires allowDestructive to be enabled in front-end)
     * - replace: has newSchema -> completely replace currentTables
     *
     * This method mutates a copy of currentTables and then stores it under sessionId.
     */
    private void updateSessionTables(String sessionId, List<TableAction> actions, List<Table> currentTables) {
        if (sessionId == null) {
            sessionId = "session-" + UUID.randomUUID().toString().substring(0, 8);
        }

        // Work on a copy
        List<Table> working = new ArrayList<>(currentTables != null ? currentTables : Collections.emptyList());

        for (TableAction action : actions) {
            if (action == null || action.getType() == null) continue;
            String type = action.getType().toString().toLowerCase(Locale.ROOT).trim();

            switch (type) {
                case "create":
                    if (action.getTables() != null) {
                        for (Table t : action.getTables()) {
                            boolean exists = working.stream().anyMatch(ex -> ex.getName().equalsIgnoreCase(t.getName()));
                            if (!exists) {
                                // place at end; frontend will compute position
                                working.add(t);
                            }
                        }
                    }
                    break;

                case "edit":
                    if (action.getTables() != null) {
                        for (Table updated : action.getTables()) {
                            int idx = -1;
                            for (int i = 0; i < working.size(); i++) {
                                if (working.get(i).getName().equalsIgnoreCase(updated.getName())) {
                                    idx = i;
                                    break;
                                }
                            }
                            if (idx != -1) {
                                // merge: keep existing fields not provided in updated
                                Table existing = working.get(idx);
                                // naive merge: replace existing with updated but preserve position/relationships if missing

                                if (updated.getRelationships() == null) updated.setRelationships(existing.getRelationships());
                                working.set(idx, updated);
                            } else {
                                // treat as create if not found
                                working.add(updated);
                            }
                        }
                    }
                    break;

                case "delete":
                    if (action.getTableNames() != null) {
                        for (String name : action.getTableNames()) {
                            working.removeIf(t -> t.getName().equalsIgnoreCase(name));
                        }
                    }
                    break;

                case "replace":
                    if (action.getNewSchema() != null) {
                        // full replacement
                        working = new ArrayList<>(action.getNewSchema());
                    }
                    break;

                default:
                    // unknown action: ignore
                    break;
            }

            // enforce MAX_TABLES as safety
            if (working.size() > MAX_TABLES) {
                // trim extras (keep first MAX_TABLES)
                working = new ArrayList<>(working.subList(0, MAX_TABLES));
            }
        }

        // store back
        sessionTables.put(sessionId, working);
    }
}
