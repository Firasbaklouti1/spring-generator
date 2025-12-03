"use client"

import { useState } from "react"
import { motion, AnimatePresence } from "framer-motion"
import {
  Rocket,
  Download,
  Loader2,
  Package,
  Code2,
  Settings,
  CheckCircle2,
  ArrowLeft,
  Sparkles,
  Plus,
  X,
} from "lucide-react"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { useGeneratorStore, type DependencyGroup } from "@/lib/store"
import { DependenciesModal, DEPENDENCY_GROUPS } from "./dependencies-modal"

const javaVersions = ["17", "21"]
const bootVersions = ["3.2.0", "3.1.5", "3.0.12"]

// Helper to get category color
function getCategoryColor(groupName: string): string {
  const colors: Record<string, string> = {
    "Developer Tools": "bg-emerald-500/20 text-emerald-400 border-emerald-500/30",
    Web: "bg-blue-500/20 text-blue-400 border-blue-500/30",
    Security: "bg-red-500/20 text-red-400 border-red-500/30",
    SQL: "bg-amber-500/20 text-amber-400 border-amber-500/30",
    NoSQL: "bg-purple-500/20 text-purple-400 border-purple-500/30",
    Messaging: "bg-pink-500/20 text-pink-400 border-pink-500/30",
    "I/O": "bg-cyan-500/20 text-cyan-400 border-cyan-500/30",
    Ops: "bg-orange-500/20 text-orange-400 border-orange-500/30",
    Testing: "bg-teal-500/20 text-teal-400 border-teal-500/30",
  }
  return colors[groupName] || "bg-gray-500/20 text-gray-400 border-gray-500/30"
}

// Get dependency info from ID
function getDependencyInfo(depId: string): { name: string; group: string } | null {
  for (const group of DEPENDENCY_GROUPS) {
    const dep = group.dependencies.find((d) => d.id === depId)
    if (dep) return { name: dep.name, group: group.name }
  }
  return null
}

export function ProjectConfigPhase() {
  const { tables, projectConfig, setProjectConfig, setCurrentPhase, isGenerating, setIsGenerating, reset } =
    useGeneratorStore()
  const [showSuccess, setShowSuccess] = useState(false)
  const [showDependenciesModal, setShowDependenciesModal] = useState(false)

  const selectedDependencyIds = projectConfig.dependencies

  const handleDependenciesSelect = (depIds: string[]) => {
    setProjectConfig({ dependencies: depIds })
  }

  const handleRemoveDependency = (depId: string) => {
    const updated = selectedDependencyIds.filter((id) => id !== depId)
    setProjectConfig({ dependencies: updated })
  }

  const handleGenerate = async () => {
    setIsGenerating(true)

    try {
      // Convert dependency IDs to full Dependency objects
      const fullDependencies = selectedDependencyIds.map((depId) => {
        for (const group of DEPENDENCY_GROUPS) {
          const dep = group.dependencies.find((d) => d.id === depId)
          if (dep) return dep
        }
        // Fallback if dependency not found
        return {
          id: depId,
          name: depId,
          description: "",
          groupId: "",
          artifactId: depId,
          scope: "compile",
          isStarter: false,
        }
      })

      const payload = {
        groupId: projectConfig.groupId,
        artifactId: projectConfig.artifactId,
        name: projectConfig.name,
        description: projectConfig.description,
        packageName: projectConfig.packageName,
        javaVersion: projectConfig.javaVersion,
        bootVersion: projectConfig.bootVersion,
        dependencies: fullDependencies, // Send full Dependency objects
        includeEntity: projectConfig.includeEntity,
        includeRepository: projectConfig.includeRepository,
        includeService: projectConfig.includeService,
        includeController: projectConfig.includeController,
        includeDto: projectConfig.includeDto,
        includeMapper: projectConfig.includeMapper,
        tables: tables.map((table) => ({
          name: table.name,
          className: table.className,
          columns: table.columns.map((col) => ({
            name: col.name,
            fieldName: col.fieldName,
            javaType: col.javaType,
            type: col.type, // This is the SQL type
            length: col.length,
            primaryKey: col.primaryKey,
            autoIncrement: col.autoIncrement,
            nullable: col.nullable,
            unique: col.unique,
            foreignKey: col.foreignKey,
            referencedTable: col.referencedTable,
            referencedColumn: col.referencedColumn,
          })),
          relationships: table.relationships.map((rel) => ({
            type: typeof rel.type === "object" ? rel.type.type : rel.type,
            sourceTable: rel.sourceTable,
            targetTable: rel.targetTable,
            sourceColumn: rel.sourceColumn,
            targetColumn: rel.targetColumn,
            joinTable: rel.joinTable,
            mappedBy: rel.mappedBy,
            fieldName: rel.fieldName,
            targetClassName: rel.targetClassName,
          })),
          isJoinTable: table.isJoinTable,
        })),
      }

      try {
        console.log("Generating project...")
        console.log(JSON.stringify(payload))
        const response = await fetch("http://localhost:8080/api/generate/project", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(payload),
          signal: AbortSignal.timeout(10000),
        })

        if (response.ok) {
          const blob = await response.blob()
          const url = window.URL.createObjectURL(blob)
          const a = document.createElement("a")
          a.href = url
          a.download = `${projectConfig.artifactId}.zip`
          document.body.appendChild(a)
          a.click()
          window.URL.revokeObjectURL(url)
          document.body.removeChild(a)

          setShowSuccess(true)
          toast.success("Project generated successfully!")
        } else {
          throw new Error("API returned error")
        }
      } catch {
        console.log("Using mock project generation")

        const mockContent = `
# ${projectConfig.name}

## Project Configuration
- Group ID: ${projectConfig.groupId}
- Artifact ID: ${projectConfig.artifactId}
- Package: ${projectConfig.packageName}
- Java Version: ${projectConfig.javaVersion}
- Spring Boot: ${projectConfig.bootVersion}

## Dependencies
${selectedDependencyIds.map((d) => `- ${getDependencyInfo(d)?.name || d}`).join("\n")}

## Tables
${tables
            .map(
              (t) => `
### ${t.className} (${t.name})
Columns:
${t.columns.map((c) => `  - ${c.fieldName}: ${c.javaType}${c.foreignKey ? ` (FK → ${c.referencedTable})` : ""}`).join("\n")}
`,
            )
            .join("\n")}

---
Generated by Spring Generator
        `.trim()

        const blob = new Blob([mockContent], { type: "text/plain" })
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement("a")
        a.href = url
        a.download = `${projectConfig.artifactId}-spec.txt`
        document.body.appendChild(a)
        a.click()
        window.URL.revokeObjectURL(url)
        document.body.removeChild(a)

        setShowSuccess(true)
        toast.success("Project specification downloaded!", {
          description: "Connect to the backend for full ZIP generation.",
        })
      }
    } catch (error) {
      console.error(error)
      toast.error("Failed to generate project")
    } finally {
      setIsGenerating(false)
    }
  }

  const handleStartOver = () => {
    reset()
    setShowSuccess(false)
  }

  // Success View
  if (showSuccess) {
    return (
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          className="max-w-lg mx-auto text-center"
        >
          <motion.div
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ delay: 0.2, type: "spring" }}
            className="w-24 h-24 rounded-full bg-gradient-to-br from-primary/20 to-accent/20 flex items-center justify-center mx-auto mb-6 glow"
          >
            <CheckCircle2 className="w-12 h-12 text-primary" />
          </motion.div>

          <h1 className="text-3xl font-bold mb-4">
            Project <span className="gradient-text">Generated!</span>
          </h1>

          <p className="text-muted-foreground mb-8">
            Your Spring Boot project has been generated and downloaded. Import it into your favorite IDE and start
            coding!
          </p>

          <div className="glass rounded-xl p-6 mb-8 text-left">
            <h3 className="font-semibold mb-3">Project Summary</h3>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-muted-foreground">Package:</span>
                <span className="font-mono">{projectConfig.packageName}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Tables:</span>
                <span>{tables.length} entities</span>
              </div>
              <div className="flex justify-between">
                <span className="text-muted-foreground">Dependencies:</span>
                <span>{selectedDependencyIds.length} included</span>
              </div>
            </div>
          </div>

          <div className="flex gap-4 justify-center">
            <Button variant="outline" onClick={handleStartOver} className="glass bg-transparent">
              <Sparkles className="w-4 h-4 mr-2" />
              Start New Project
            </Button>
          </div>
        </motion.div>
      </div>
    )
  }

  return (
    <div className="container mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-16 h-16 rounded-2xl bg-gradient-to-br from-primary/20 to-accent/20 mb-4">
            <Rocket className="w-8 h-8 text-primary" />
          </div>
          <h1 className="text-2xl sm:text-3xl font-bold mb-2">Configure Your Project</h1>
          <p className="text-muted-foreground">Set up your Spring Boot project metadata and dependencies</p>
        </motion.div>

        <div className="grid lg:grid-cols-2 gap-8">
          {/* Project Metadata */}
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.1 }}
            className="glass rounded-2xl p-6"
          >
            <div className="flex items-center gap-2 mb-6">
              <Package className="w-5 h-5 text-primary" />
              <h2 className="text-lg font-semibold">Project Metadata</h2>
            </div>

            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="groupId" className="text-sm mb-1 block">
                    Group ID
                  </Label>
                  <Input
                    id="groupId"
                    value={projectConfig.groupId}
                    onChange={(e) => setProjectConfig({ groupId: e.target.value })}
                    placeholder="com.example"
                    className="bg-input/50"
                  />
                </div>
                <div>
                  <Label htmlFor="artifactId" className="text-sm mb-1 block">
                    Artifact ID
                  </Label>
                  <Input
                    id="artifactId"
                    value={projectConfig.artifactId}
                    onChange={(e) => setProjectConfig({ artifactId: e.target.value })}
                    placeholder="demo"
                    className="bg-input/50"
                  />
                </div>
              </div>

              <div>
                <Label htmlFor="name" className="text-sm mb-1 block">
                  Name
                </Label>
                <Input
                  id="name"
                  value={projectConfig.name}
                  onChange={(e) => setProjectConfig({ name: e.target.value })}
                  placeholder="Demo"
                  className="bg-input/50"
                />
              </div>

              <div>
                <Label htmlFor="description" className="text-sm mb-1 block">
                  Description
                </Label>
                <Textarea
                  id="description"
                  value={projectConfig.description}
                  onChange={(e) => setProjectConfig({ description: e.target.value })}
                  placeholder="Demo project for Spring Boot"
                  rows={2}
                  className="bg-input/50 resize-none"
                />
              </div>

              <div>
                <Label htmlFor="packageName" className="text-sm mb-1 block">
                  Package Name
                </Label>
                <Input
                  id="packageName"
                  value={projectConfig.packageName}
                  onChange={(e) => setProjectConfig({ packageName: e.target.value })}
                  placeholder="com.example.demo"
                  className="bg-input/50"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-sm mb-1 block">Java Version</Label>
                  <Select
                    value={projectConfig.javaVersion}
                    onValueChange={(value) => setProjectConfig({ javaVersion: value })}
                  >
                    <SelectTrigger className="bg-input/50">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {javaVersions.map((v) => (
                        <SelectItem key={v} value={v}>
                          Java {v}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div>
                  <Label className="text-sm mb-1 block">Spring Boot Version</Label>
                  <Select
                    value={projectConfig.bootVersion}
                    onValueChange={(value) => setProjectConfig({ bootVersion: value })}
                  >
                    <SelectTrigger className="bg-input/50">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {bootVersions.map((v) => (
                        <SelectItem key={v} value={v}>
                          {v}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
              </div>
            </div>
          </motion.div>

          {/* Dependencies - Redesigned */}
          <motion.div
            initial={{ opacity: 0, x: 20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.2 }}
            className="glass rounded-2xl p-6"
          >
            <div className="flex items-center justify-between mb-6">
              <div className="flex items-center gap-2">
                <Code2 className="w-5 h-5 text-primary" />
                <h2 className="text-lg font-semibold">Dependencies</h2>
              </div>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setShowDependenciesModal(true)}
                className="glass bg-transparent"
              >
                <Plus className="w-4 h-4 mr-1" />
                Add
              </Button>
            </div>

            {/* Selected Dependencies */}
            <div className="space-y-2 max-h-[400px] overflow-y-auto pr-2">
              {selectedDependencyIds.length === 0 ? (
                <div className="text-center py-8 text-muted-foreground">
                  <Code2 className="w-8 h-8 mx-auto mb-2 opacity-50" />
                  <p className="text-sm">No dependencies selected</p>
                  <p className="text-xs mt-1">Click "Add" to browse dependencies</p>
                </div>
              ) : (
                selectedDependencyIds.map((depId) => {
                  const info = getDependencyInfo(depId)
                  if (!info) return null
                  return (
                    <div
                      key={depId}
                      className="flex items-center justify-between p-3 rounded-xl bg-secondary/30 border border-border group"
                    >
                      <div className="flex items-center gap-2">
                        <span className="font-medium text-sm">{info.name}</span>
                        <span
                          className={`px-2 py-0.5 text-[10px] font-semibold uppercase rounded border ${getCategoryColor(info.group)}`}
                        >
                          {info.group}
                        </span>
                      </div>
                      <button
                        onClick={() => handleRemoveDependency(depId)}
                        className="p-1 rounded-lg opacity-0 group-hover:opacity-100 hover:bg-destructive/20 transition-all"
                      >
                        <X className="w-4 h-4 text-destructive" />
                      </button>
                    </div>
                  )
                })
              )}
            </div>
          </motion.div>
        </div>

        {/* Summary & Generate */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.3 }}
          className="mt-8 glass rounded-2xl p-6"
        >
          <div className="flex items-center gap-2 mb-4">
            <Settings className="w-5 h-5 text-primary" />
            <h2 className="text-lg font-semibold">Generation Summary</h2>
          </div>

          <div className="grid sm:grid-cols-3 gap-4 mb-6">
            <div className="text-center p-4 rounded-xl bg-secondary/30">
              <div className="text-2xl font-bold gradient-text">{tables.length}</div>
              <div className="text-sm text-muted-foreground">Entities</div>
            </div>
            <div className="text-center p-4 rounded-xl bg-secondary/30">
              <div className="text-2xl font-bold gradient-text">
                {tables.reduce((acc, t) => acc + t.columns.length, 0)}
              </div>
              <div className="text-sm text-muted-foreground">Fields</div>
            </div>
            <div className="text-center p-4 rounded-xl bg-secondary/30">
              <div className="text-2xl font-bold gradient-text">{selectedDependencyIds.length}</div>
              <div className="text-sm text-muted-foreground">Dependencies</div>
            </div>
          </div>

          <div className="flex flex-col sm:flex-row gap-4 justify-between items-center">
            <Button variant="outline" onClick={() => setCurrentPhase(2)} className="glass w-full sm:w-auto">
              <ArrowLeft className="w-4 h-4 mr-2" />
              Back to Editor
            </Button>

            <motion.div
              animate={
                !isGenerating
                  ? {
                    boxShadow: [
                      "0 0 20px oklch(0.7 0.18 200 / 0.3)",
                      "0 0 40px oklch(0.7 0.18 200 / 0.5)",
                      "0 0 20px oklch(0.7 0.18 200 / 0.3)",
                    ],
                  }
                  : {}
              }
              transition={{ duration: 2, repeat: Number.POSITIVE_INFINITY }}
              className="w-full sm:w-auto rounded-xl"
            >
              <Button
                onClick={handleGenerate}
                disabled={isGenerating || tables.length === 0}
                size="lg"
                className="w-full bg-gradient-to-r from-primary to-accent text-primary-foreground hover:opacity-90 transition-opacity px-8"
              >
                {isGenerating ? (
                  <span className="flex items-center gap-2">
                    <Loader2 className="w-5 h-5 animate-spin" />
                    Generating...
                  </span>
                ) : (
                  <span className="flex items-center gap-2">
                    <Download className="w-5 h-5" />
                    Generate Project
                  </span>
                )}
              </Button>
            </motion.div>
          </div>
        </motion.div>
      </div>

      {/* Dependencies Modal */}
      <AnimatePresence>
        {showDependenciesModal && (
          <DependenciesModal
            selectedDependencies={selectedDependencyIds}
            onSelect={handleDependenciesSelect}
            onClose={() => setShowDependenciesModal(false)}
          />
        )}
      </AnimatePresence>
    </div>
  )
}
