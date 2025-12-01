"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import axios from "axios"
import DependencyPicker from "@/components/DependencyPicker"

interface Column {
  name: string
  fieldName: string
  javaType: string
  sqlType: string
  primaryKey?: boolean
  autoIncrement?: boolean
  unique?: boolean
  nullable?: boolean
  length?: number
}

interface Table {
  name: string
  className: string
  columns: Column[]
  relationships?: any[]
}

export default function Home() {
  const [formData, setFormData] = useState({
    groupId: "com.example",
    artifactId: "demo",
    name: "demo",
    description: "Demo Spring Boot Project",
    packageName: "com.example.demo",
    javaVersion: "17",
    bootVersion: "3.2.0",
    dependencies: [] as string[],
    sqlContent: ""
  })

  const [loading, setLoading] = useState(false)
  const [loadingMessage, setLoadingMessage] = useState("")
  const [parsedTables, setParsedTables] = useState<Table[]>([])
  const [parsingSQL, setParsingSQL] = useState(false)
  const [expandedTable, setExpandedTable] = useState<number | null>(null)

  const handleParseSql = async () => {
    if (!formData.sqlContent || formData.sqlContent.trim() === "") {
      alert("Please enter SQL schema first")
      return
    }

    setParsingSQL(true)
    try {
      const encodedSql = encodeURIComponent(formData.sqlContent)
      const response = await axios.get(
        `http://localhost:8080/api/sqlParser/${encodedSql}`
      )

      setParsedTables(response.data)
      console.log("Parsed tables:", response.data)
    } catch (error) {
      console.error("Error parsing SQL:", error)
      alert("Failed to parse SQL. Check console for details.")
    } finally {
      setParsingSQL(false)
    }
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    try {
      let tables = parsedTables

      // If no tables were pre-parsed, parse them now
      if (formData.sqlContent && formData.sqlContent.trim() !== "" && tables.length === 0) {
        setLoadingMessage("Parsing SQL schema...")

        const encodedSql = encodeURIComponent(formData.sqlContent)
        const sqlParseResponse = await axios.get(
          `http://localhost:8080/api/sqlParser/${encodedSql}`
        )

        tables = sqlParseResponse.data
        console.log("Parsed tables:", tables)
      }

      // Phase 2: Generate project with tables
      setLoadingMessage("Generating Spring Boot project...")

      const projectRequest = {
        groupId: formData.groupId,
        artifactId: formData.artifactId,
        name: formData.name,
        description: formData.description,
        packageName: formData.packageName,
        javaVersion: formData.javaVersion,
        bootVersion: formData.bootVersion,
        dependencies: formData.dependencies,
        tables: tables
      }

      const response = await axios.post(
        "http://localhost:8080/api/generate/project",
        projectRequest,
        { responseType: "blob" }
      )

      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement("a")
      link.href = url
      link.setAttribute("download", `${formData.artifactId}.zip`)
      document.body.appendChild(link)
      link.click()
      link.remove()

      setLoadingMessage("Project generated successfully!")
    } catch (error) {
      console.error("Error generating project:", error)
      alert("Failed to generate project. Check console for details.")
    } finally {
      setLoading(false)
      setLoadingMessage("")
    }
  }

  const updateTableName = (index: number, newName: string) => {
    const updated = [...parsedTables]
    updated[index].name = newName
    setParsedTables(updated)
  }

  const updateClassName = (index: number, newClassName: string) => {
    const updated = [...parsedTables]
    updated[index].className = newClassName
    setParsedTables(updated)
  }

  const updateColumn = (tableIndex: number, columnIndex: number, field: keyof Column, value: any) => {
    const updated = [...parsedTables]
    updated[tableIndex].columns[columnIndex] = {
      ...updated[tableIndex].columns[columnIndex],
      [field]: value
    }
    setParsedTables(updated)
  }

  const deleteTable = (index: number) => {
    setParsedTables(parsedTables.filter((_, i) => i !== index))
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      <div className="container mx-auto px-4 py-12">
        <div className="max-w-6xl mx-auto">
          {/* Header */}
          <div className="text-center mb-12">
            <h1 className="text-5xl font-bold text-white mb-4">
              Firas Spring Generator
            </h1>
            <p className="text-xl text-purple-200">
              Generate customizable Spring Boot projects with SQL-to-CRUD support
            </p>
          </div>

          {/* Main Form */}
          <div className="bg-white/10 backdrop-blur-lg rounded-2xl p-8 shadow-2xl border border-white/20">
            <form onSubmit={handleSubmit} className="space-y-6">
              {/* Project Metadata */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-medium text-white mb-2">
                    Group ID
                  </label>
                  <Input
                    type="text"
                    value={formData.groupId}
                    onChange={(e) => setFormData({ ...formData, groupId: e.target.value })}
                    className="bg-white/20 border-white/30 text-white placeholder:text-white/50"
                    placeholder="com.example"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-white mb-2">
                    Artifact ID
                  </label>
                  <Input
                    type="text"
                    value={formData.artifactId}
                    onChange={(e) => setFormData({ ...formData, artifactId: e.target.value })}
                    className="bg-white/20 border-white/30 text-white placeholder:text-white/50"
                    placeholder="demo"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-white mb-2">
                    Name
                  </label>
                  <Input
                    type="text"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    className="bg-white/20 border-white/30 text-white placeholder:text-white/50"
                    placeholder="demo"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-white mb-2">
                    Package Name
                  </label>
                  <Input
                    type="text"
                    value={formData.packageName}
                    onChange={(e) => setFormData({ ...formData, packageName: e.target.value })}
                    className="bg-white/20 border-white/30 text-white placeholder:text-white/50"
                    placeholder="com.example.demo"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-white mb-2">
                    Java Version
                  </label>
                  <select
                    value={formData.javaVersion}
                    onChange={(e) => setFormData({ ...formData, javaVersion: e.target.value })}
                    className="w-full h-10 px-3 rounded-md bg-white/20 border border-white/30 text-white"
                  >
                    <option value="17">Java 17</option>
                    <option value="21">Java 21</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-white mb-2">
                    Spring Boot Version
                  </label>
                  <Input
                    type="text"
                    value={formData.bootVersion}
                    onChange={(e) => setFormData({ ...formData, bootVersion: e.target.value })}
                    className="bg-white/20 border-white/30 text-white placeholder:text-white/50"
                    placeholder="3.2.0"
                  />
                </div>
              </div>

              {/* Description */}
              <div>
                <label className="block text-sm font-medium text-white mb-2">
                  Description
                </label>
                <Input
                  type="text"
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="bg-white/20 border-white/30 text-white placeholder:text-white/50"
                  placeholder="Project description"
                />
              </div>

              {/* Dependency Picker */}
              <DependencyPicker
                selectedDependencies={formData.dependencies}
                onDependenciesChange={(deps) => setFormData({ ...formData, dependencies: deps })}
              />

              {/* SQL Content */}
              <div>
                <div className="flex justify-between items-center mb-2">
                  <label className="block text-sm font-medium text-white">
                    SQL Schema (Optional - for CRUD generation)
                  </label>
                  <Button
                    type="button"
                    onClick={handleParseSql}
                    disabled={parsingSQL}
                    className="bg-gradient-to-r from-cyan-500 to-blue-500 hover:from-cyan-600 hover:to-blue-600 text-white text-sm px-4 py-2 h-auto"
                  >
                    {parsingSQL ? "Parsing..." : "🔍 Parse SQL"}
                  </Button>
                </div>
                <textarea
                  value={formData.sqlContent}
                  onChange={(e) => setFormData({ ...formData, sqlContent: e.target.value })}
                  className="w-full h-32 px-3 py-2 rounded-md bg-white/20 border border-white/30 text-white placeholder:text-white/50 font-mono text-sm"
                  placeholder="CREATE TABLE users (&#10;  id INT PRIMARY KEY AUTO_INCREMENT,&#10;  username VARCHAR(255),&#10;  email VARCHAR(255)&#10;);"
                />
                <p className="text-xs text-purple-200 mt-2">
                  Click "Parse SQL" to preview and edit tables before generation
                </p>
              </div>

              {/* Parsed Tables Preview */}
              {parsedTables.length > 0 && (
                <div className="space-y-4">
                  <div className="flex justify-between items-center">
                    <h3 className="text-lg font-semibold text-white">
                      📋 Parsed Tables ({parsedTables.length})
                    </h3>
                    <button
                      type="button"
                      onClick={() => setParsedTables([])}
                      className="text-xs text-red-300 hover:text-red-200"
                    >
                      Clear All
                    </button>
                  </div>

                  <div className="space-y-3">
                    {parsedTables.map((table, tableIndex) => (
                      <div
                        key={tableIndex}
                        className="bg-white/5 rounded-lg border border-white/20 overflow-hidden"
                      >
                        {/* Table Header */}
                        <div
                          className="p-4 cursor-pointer hover:bg-white/5 transition-colors"
                          onClick={() => setExpandedTable(expandedTable === tableIndex ? null : tableIndex)}
                        >
                          <div className="flex justify-between items-center">
                            <div className="flex items-center gap-4 flex-1">
                              <span className="text-white font-mono text-sm">
                                {expandedTable === tableIndex ? "▼" : "▶"}
                              </span>
                              <div className="flex-1 grid grid-cols-2 gap-4">
                                <div>
                                  <span className="text-xs text-purple-300">Table Name:</span>
                                  <Input
                                    type="text"
                                    value={table.name}
                                    onChange={(e) => {
                                      e.stopPropagation()
                                      updateTableName(tableIndex, e.target.value)
                                    }}
                                    onClick={(e) => e.stopPropagation()}
                                    className="mt-1 bg-white/10 border-white/20 text-white text-sm h-8"
                                  />
                                </div>
                                <div>
                                  <span className="text-xs text-purple-300">Class Name:</span>
                                  <Input
                                    type="text"
                                    value={table.className}
                                    onChange={(e) => {
                                      e.stopPropagation()
                                      updateClassName(tableIndex, e.target.value)
                                    }}
                                    onClick={(e) => e.stopPropagation()}
                                    className="mt-1 bg-white/10 border-white/20 text-white text-sm h-8"
                                  />
                                </div>
                              </div>
                              <div className="text-sm text-purple-200">
                                {table.columns.length} columns
                              </div>
                            </div>
                            <button
                              type="button"
                              onClick={(e) => {
                                e.stopPropagation()
                                deleteTable(tableIndex)
                              }}
                              className="ml-4 text-red-300 hover:text-red-200 text-sm"
                            >
                              🗑️
                            </button>
                          </div>
                        </div>

                        {/* Columns Details */}
                        {expandedTable === tableIndex && (
                          <div className="p-4 pt-0 space-y-2">
                            <div className="text-xs text-purple-300 mb-2 font-semibold">Columns:</div>
                            {table.columns.map((column, colIndex) => (
                              <div
                                key={colIndex}
                                className="grid grid-cols-4 gap-2 bg-white/5 p-2 rounded border border-white/10"
                              >
                                <div>
                                  <span className="text-xs text-purple-300">Field Name</span>
                                  <Input
                                    type="text"
                                    value={column.fieldName}
                                    onChange={(e) => updateColumn(tableIndex, colIndex, 'fieldName', e.target.value)}
                                    className="mt-1 bg-white/10 border-white/20 text-white text-xs h-7"
                                  />
                                </div>
                                <div>
                                  <span className="text-xs text-purple-300">Java Type</span>
                                  <Input
                                    type="text"
                                    value={column.javaType}
                                    onChange={(e) => updateColumn(tableIndex, colIndex, 'javaType', e.target.value)}
                                    className="mt-1 bg-white/10 border-white/20 text-white text-xs h-7"
                                  />
                                </div>
                                <div>
                                  <span className="text-xs text-purple-300">SQL Type</span>
                                  <Input
                                    type="text"
                                    value={column.sqlType}
                                    onChange={(e) => updateColumn(tableIndex, colIndex, 'sqlType', e.target.value)}
                                    className="mt-1 bg-white/10 border-white/20 text-white text-xs h-7"
                                  />
                                </div>
                                <div className="flex items-end gap-2">
                                  <label className="flex items-center text-xs text-white">
                                    <input
                                      type="checkbox"
                                      checked={column.primaryKey || false}
                                      onChange={(e) => updateColumn(tableIndex, colIndex, 'primaryKey', e.target.checked)}
                                      className="mr-1"
                                    />
                                    PK
                                  </label>
                                  <label className="flex items-center text-xs text-white">
                                    <input
                                      type="checkbox"
                                      checked={column.unique || false}
                                      onChange={(e) => updateColumn(tableIndex, colIndex, 'unique', e.target.checked)}
                                      className="mr-1"
                                    />
                                    Unique
                                  </label>
                                </div>
                              </div>
                            ))}
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Submit Button */}
              <Button
                type="submit"
                disabled={loading}
                className="w-full h-12 text-lg font-semibold bg-gradient-to-r from-purple-500 to-pink-500 hover:from-purple-600 hover:to-pink-600 text-white"
              >
                {loading ? (loadingMessage || "Generating...") : "Generate Project"}
              </Button>

              {/* Loading status message */}
              {loading && loadingMessage && (
                <div className="text-center text-purple-200 text-sm animate-pulse">
                  {loadingMessage}
                </div>
              )}
            </form>
          </div>

          {/* Features */}
          <div className="mt-12 grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="bg-white/10 backdrop-blur-lg rounded-xl p-6 border border-white/20">
              <h3 className="text-xl font-bold text-white mb-2">⚡ Fast Generation</h3>
              <p className="text-purple-200">Generate complete Spring Boot projects in seconds</p>
            </div>
            <div className="bg-white/10 backdrop-blur-lg rounded-xl p-6 border border-white/20">
              <h3 className="text-xl font-bold text-white mb-2">🗄️ SQL to CRUD</h3>
              <p className="text-purple-200">Auto-generate entities and layers from SQL schemas</p>
            </div>
            <div className="bg-white/10 backdrop-blur-lg rounded-xl p-6 border border-white/20">
              <h3 className="text-xl font-bold text-white mb-2">📦 Ready to Use</h3>
              <p className="text-purple-200">Download and run your project immediately</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
