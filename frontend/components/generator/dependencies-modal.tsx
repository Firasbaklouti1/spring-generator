"use client"

import { useState, useMemo } from "react"
import { motion } from "framer-motion"
import { X, Search, Check } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import type { DependencyGroup } from "@/lib/store"

const DEPENDENCY_GROUPS: DependencyGroup[] = [
  {
    name: "Developer Tools",
    dependencies: [
      {
        id: "devtools",
        name: "Spring Boot DevTools",
        description: "Provides fast application restarts, LiveReload, and configurations",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-devtools",
        scope: "runtime",
        isStarter: true,
      },
      {
        id: "lombok",
        name: "Lombok",
        description: "Java annotation library to reduce boilerplate code",
        groupId: "org.projectlombok",
        artifactId: "lombok",
        scope: "compile",
        isStarter: false,
      },
      {
        id: "configuration-processor",
        name: "Spring Configuration Processor",
        description: "Generate metadata for configuration properties",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-configuration-processor",
        scope: "compile",
        isStarter: false,
      },
    ],
  },
  {
    name: "Web",
    dependencies: [
      {
        id: "web",
        name: "Spring Web",
        description: "Build web applications using Spring MVC with Tomcat",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-web",
        scope: "compile",
        isStarter: true,
      },
      {
        id: "webflux",
        name: "Spring Reactive Web",
        description: "Build reactive web applications with Spring WebFlux",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-webflux",
        scope: "compile",
        isStarter: true,
      },
      {
        id: "graphql",
        name: "Spring for GraphQL",
        description: "Build GraphQL applications with Spring",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-graphql",
        scope: "compile",
        isStarter: true,
      },
      {
        id: "rest-docs",
        name: "Spring REST Docs",
        description: "Document RESTful services by combining hand-written and auto-generated documentation",
        groupId: "org.springframework.restdocs",
        artifactId: "spring-restdocs-mockmvc",
        scope: "test",
        isStarter: false,
      },
      {
        id: "hateoas",
        name: "Spring HATEOAS",
        description: "Eases the creation of RESTful APIs that follow the HATEOAS principle",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-hateoas",
        scope: "compile",
        isStarter: true,
      },
      {
        id: "websocket",
        name: "WebSocket",
        description: "Build WebSocket applications with SockJS and STOMP",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-websocket",
        scope: "compile",
        isStarter: true,
      },
    ],
  },
  {
    name: "Security",
    dependencies: [
      {
        id: "security",
        name: "Spring Security",
        description: "Highly customizable authentication and access-control framework",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-security",
        scope: "compile",
        isStarter: true,
      },
      {
        id: "oauth2-client",
        name: "OAuth2 Client",
        description: "Spring Boot integration for OAuth2/OpenID Connect clients",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-oauth2-client",
        scope: "compile",
        isStarter: true,
      },
      {
        id: "oauth2-resource-server",
        name: "OAuth2 Resource Server",
        description: "Spring Boot integration for OAuth2 Resource Server",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-oauth2-resource-server",
        scope: "compile",
        isStarter: true,
      },
    ],
  },
  {
    name: "SQL",
    dependencies: [
      {
        id: "jpa",
        name: "Spring Data JPA",
        description: "Persist data in SQL stores with Java Persistence API",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-data-jpa",
        scope: "compile",
        isStarter: true,
      },
      {
        id: "jdbc",
        name: "Spring Data JDBC",
        description: "Persist data in SQL stores with plain JDBC",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-data-jdbc",
        scope: "compile",
        isStarter: true,
      },
      {
        id: "h2",
        name: "H2 Database",
        description: "Fast in-memory database that supports JDBC API",
        groupId: "com.h2database",
        artifactId: "h2",
        scope: "runtime",
        isStarter: false,
      },
      {
        id: "mysql",
        name: "MySQL Driver",
        description: "MySQL JDBC driver",
        groupId: "com.mysql",
        artifactId: "mysql-connector-j",
        scope: "runtime",
        isStarter: false,
      },
      {
        id: "postgresql",
        name: "PostgreSQL Driver",
        description: "A JDBC driver for PostgreSQL",
        groupId: "org.postgresql",
        artifactId: "postgresql",
        scope: "runtime",
        isStarter: false,
      },
      {
        id: "mariadb",
        name: "MariaDB Driver",
        description: "MariaDB JDBC driver",
        groupId: "org.mariadb.jdbc",
        artifactId: "mariadb-java-client",
        scope: "runtime",
        isStarter: false,
      },
      {
        id: "mssql",
        name: "MS SQL Server Driver",
        description: "Microsoft SQL Server JDBC driver",
        groupId: "com.microsoft.sqlserver",
        artifactId: "mssql-jdbc",
        scope: "runtime",
        isStarter: false,
      },
      {
        id: "oracle",
        name: "Oracle Driver",
        description: "Oracle JDBC driver",
        groupId: "com.oracle.database.jdbc",
        artifactId: "ojdbc11",
        scope: "runtime",
        isStarter: false,
      },
      {
        id: "flyway",
        name: "Flyway Migration",
        description: "Version control for your database",
        groupId: "org.flywaydb",
        artifactId: "flyway-core",
        scope: "compile",
        isStarter: false,
      },
      {
        id: "liquibase",
        name: "Liquibase Migration",
        description: "Database version control and migration",
        groupId: "org.liquibase",
        artifactId: "liquibase-core",
        scope: "compile",
        isStarter: false,
      },
    ],
  },
  {
    name: "NoSQL",
    dependencies: [
      {
        id: "mongodb",
        name: "Spring Data MongoDB",
        description: "Store data in flexible, JSON-like documents",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-data-mongodb",
        scope: "compile",
        isStarter: true,
      },
      {
        id: "redis",
        name: "Spring Data Redis",
        description: "Advanced and thread-safe Java Redis client",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-data-redis",
        scope: "compile",
        isStarter: true,
      },
      {
        id: "elasticsearch",
        name: "Spring Data Elasticsearch",
        description: "Distributed search and analytics engine",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-data-elasticsearch",
        scope: "compile",
        isStarter: true,
      },
      {
        id: "cassandra",
        name: "Spring Data Cassandra",
        description: "Distributed database for high availability",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-data-cassandra",
        scope: "compile",
        isStarter: true,
      },
    ],
  },
  {
    name: "Messaging",
    dependencies: [
      {
        id: "amqp",
        name: "Spring for RabbitMQ",
        description: "Build applications with RabbitMQ",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-amqp",
        scope: "compile",
        isStarter: true,
      },
      {
        id: "kafka",
        name: "Spring for Apache Kafka",
        description: "Build applications with Apache Kafka",
        groupId: "org.springframework.kafka",
        artifactId: "spring-kafka",
        scope: "compile",
        isStarter: false,
      },
      {
        id: "activemq",
        name: "Spring for ActiveMQ",
        description: "Build applications with ActiveMQ",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-activemq",
        scope: "compile",
        isStarter: true,
      },
    ],
  },
  {
    name: "I/O",
    dependencies: [
      {
        id: "validation",
        name: "Validation",
        description: "Bean Validation with Hibernate validator",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-validation",
        scope: "compile",
        isStarter: true,
      },
      {
        id: "mail",
        name: "Java Mail Sender",
        description: "Send email using Java Mail and Spring",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-mail",
        scope: "compile",
        isStarter: true,
      },
      {
        id: "quartz",
        name: "Quartz Scheduler",
        description: "Schedule jobs using Quartz",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-quartz",
        scope: "compile",
        isStarter: true,
      },
      {
        id: "cache",
        name: "Spring Cache Abstraction",
        description: "Enable Spring caching support",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-cache",
        scope: "compile",
        isStarter: true,
      },
    ],
  },
  {
    name: "Ops",
    dependencies: [
      {
        id: "actuator",
        name: "Spring Boot Actuator",
        description: "Production ready features to monitor and manage your application",
        groupId: "org.springframework.boot",
        artifactId: "spring-boot-starter-actuator",
        scope: "compile",
        isStarter: true,
      },
      {
        id: "prometheus",
        name: "Prometheus",
        description: "Expose Micrometer metrics in Prometheus format",
        groupId: "io.micrometer",
        artifactId: "micrometer-registry-prometheus",
        scope: "runtime",
        isStarter: false,
      },
    ],
  },
  {
    name: "Testing",
    dependencies: [
      {
        id: "testcontainers",
        name: "Testcontainers",
        description: "Provide lightweight throwaway containers for testing",
        groupId: "org.testcontainers",
        artifactId: "junit-jupiter",
        scope: "test",
        isStarter: false,
      },
      {
        id: "contract-verifier",
        name: "Contract Verifier",
        description: "Moves TDD to the level of software architecture",
        groupId: "org.springframework.cloud",
        artifactId: "spring-cloud-starter-contract-verifier",
        scope: "test",
        isStarter: true,
      },
    ],
  },
]

// Get category color based on group name
function getCategoryColor(groupName: string): string {
  const colors: Record<string, string> = {
    "Developer Tools": "bg-emerald-500/20 text-emerald-400",
    Web: "bg-blue-500/20 text-blue-400",
    Security: "bg-red-500/20 text-red-400",
    SQL: "bg-amber-500/20 text-amber-400",
    NoSQL: "bg-purple-500/20 text-purple-400",
    Messaging: "bg-pink-500/20 text-pink-400",
    "I/O": "bg-cyan-500/20 text-cyan-400",
    Ops: "bg-orange-500/20 text-orange-400",
    Testing: "bg-teal-500/20 text-teal-400",
  }
  return colors[groupName] || "bg-gray-500/20 text-gray-400"
}

interface DependenciesModalProps {
  selectedDependencies: string[]
  onSelect: (dependencies: string[]) => void
  onClose: () => void
}

export function DependenciesModal({ selectedDependencies, onSelect, onClose }: DependenciesModalProps) {
  const [searchQuery, setSearchQuery] = useState("")
  const [localSelected, setLocalSelected] = useState<string[]>(selectedDependencies)

  const filteredGroups = useMemo(() => {
    if (!searchQuery.trim()) return DEPENDENCY_GROUPS

    const query = searchQuery.toLowerCase()
    return DEPENDENCY_GROUPS.map((group) => ({
      ...group,
      dependencies: group.dependencies.filter(
        (dep) =>
          dep.name.toLowerCase().includes(query) ||
          dep.description.toLowerCase().includes(query) ||
          dep.id.toLowerCase().includes(query),
      ),
    })).filter((group) => group.dependencies.length > 0)
  }, [searchQuery])

  const toggleDependency = (depId: string) => {
    setLocalSelected((prev) => (prev.includes(depId) ? prev.filter((id) => id !== depId) : [...prev, depId]))
  }

  const handleAddSelected = () => {
    onSelect(localSelected)
    onClose()
  }

  const totalSelected = localSelected.length

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-sm"
      onClick={onClose}
    >
      <motion.div
        initial={{ opacity: 0, scale: 0.95, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        exit={{ opacity: 0, scale: 0.95, y: 20 }}
        className="glass-strong rounded-2xl w-full max-w-2xl max-h-[80vh] flex flex-col overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-border">
          <h2 className="text-xl font-bold">Add Dependencies</h2>
          <button onClick={onClose} className="p-2 rounded-lg hover:bg-secondary/50 transition-colors">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Search Bar */}
        <div className="px-6 py-4 border-b border-border">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground" />
            <Input
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Search dependencies by name or description..."
              className="pl-10 bg-input/50 border-primary/30 focus:border-primary"
              autoFocus
            />
          </div>
        </div>

        {/* Dependencies List */}
        <div className="flex-1 overflow-y-auto px-6 py-4 space-y-6">
          {filteredGroups.length === 0 ? (
            <div className="text-center py-12 text-muted-foreground">
              No dependencies found matching "{searchQuery}"
            </div>
          ) : (
            filteredGroups.map((group) => (
              <div key={group.name}>
                {/* Group Header */}
                <h3 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground mb-3">
                  {group.name}
                </h3>

                {/* Dependencies */}
                <div className="space-y-2">
                  {group.dependencies.map((dep) => {
                    const isSelected = localSelected.includes(dep.id)
                    return (
                      <button
                        key={dep.id}
                        onClick={() => toggleDependency(dep.id)}
                        className={`w-full flex items-start gap-3 p-4 rounded-xl text-left transition-all ${
                          isSelected
                            ? "bg-primary/10 border border-primary/30"
                            : "bg-secondary/20 border border-transparent hover:bg-secondary/40"
                        }`}
                      >
                        {/* Checkbox */}
                        <div
                          className={`flex-shrink-0 w-5 h-5 rounded border-2 flex items-center justify-center mt-0.5 transition-colors ${
                            isSelected ? "bg-primary border-primary" : "border-muted-foreground/40"
                          }`}
                        >
                          {isSelected && <Check className="w-3 h-3 text-primary-foreground" />}
                        </div>

                        {/* Content */}
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 flex-wrap">
                            <span className="font-medium">{dep.name}</span>
                            <span
                              className={`px-2 py-0.5 text-[10px] font-semibold uppercase rounded ${getCategoryColor(group.name)}`}
                            >
                              {group.name}
                            </span>
                          </div>
                          <p className="text-sm text-muted-foreground mt-1 line-clamp-2">{dep.description}</p>
                        </div>
                      </button>
                    )
                  })}
                </div>
              </div>
            ))
          )}
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between px-6 py-4 border-t border-border bg-card/50">
          <span className="text-sm text-muted-foreground">
            {totalSelected} dependenc{totalSelected === 1 ? "y" : "ies"} selected
          </span>
          <div className="flex gap-3">
            <Button variant="outline" onClick={onClose} className="glass bg-transparent">
              Cancel
            </Button>
            <Button
              onClick={handleAddSelected}
              className="bg-gradient-to-r from-primary to-accent text-primary-foreground hover:opacity-90"
            >
              Add Selected
            </Button>
          </div>
        </div>
      </motion.div>
    </motion.div>
  )
}

export { DEPENDENCY_GROUPS }
