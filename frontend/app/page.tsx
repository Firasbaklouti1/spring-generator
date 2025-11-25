"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import axios from "axios"
import DependencyPicker from "@/components/DependencyPicker"

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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    try {
      const response = await axios.post(
        "http://localhost:8080/api/generate/project",
        formData,
        { responseType: "blob" }
      )

      const url = window.URL.createObjectURL(new Blob([response.data]))
      const link = document.createElement("a")
      link.href = url
      link.setAttribute("download", `${formData.artifactId}.zip`)
      document.body.appendChild(link)
      link.click()
      link.remove()
    } catch (error) {
      console.error("Error generating project:", error)
      alert("Failed to generate project")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-purple-900 to-slate-900">
      <div className="container mx-auto px-4 py-12">
        <div className="max-w-4xl mx-auto">
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
                <label className="block text-sm font-medium text-white mb-2">
                  SQL Schema (Optional - for CRUD generation)
                </label>
                <textarea
                  value={formData.sqlContent}
                  onChange={(e) => setFormData({ ...formData, sqlContent: e.target.value })}
                  className="w-full h-32 px-3 py-2 rounded-md bg-white/20 border border-white/30 text-white placeholder:text-white/50 font-mono text-sm"
                  placeholder="CREATE TABLE users (&#10;  id INT PRIMARY KEY AUTO_INCREMENT,&#10;  username VARCHAR(255),&#10;  email VARCHAR(255)&#10;);"
                />
                <p className="text-xs text-purple-200 mt-2">
                  Paste your CREATE TABLE statements to auto-generate Entity, Repository, Service, and Controller
                </p>
              </div>

              {/* Submit Button */}
              <Button
                type="submit"
                disabled={loading}
                className="w-full h-12 text-lg font-semibold bg-gradient-to-r from-purple-500 to-pink-500 hover:from-purple-600 hover:to-pink-600 text-white"
              >
                {loading ? "Generating..." : "Generate Project"}
              </Button>
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
