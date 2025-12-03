"use client"

import { useState } from "react"
import { motion } from "framer-motion"
import { X, Sparkles, Loader2 } from "lucide-react"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { Textarea } from "@/components/ui/textarea"
import { useGeneratorStore, type Table } from "@/lib/store"

interface AiGenerateModalProps {
  onClose: () => void
}

// Mock AI generation - in production this would call an AI API
const mockAiGenerate = async (prompt: string): Promise<Table[]> => {
  // Simulate API delay
  await new Promise((resolve) => setTimeout(resolve, 2000))

  // Generate mock tables based on keywords in prompt
  const tables: Table[] = []
  const promptLower = prompt.toLowerCase()

  if (promptLower.includes("user") || promptLower.includes("auth")) {
    tables.push({
      id: `table-${Date.now()}-users`,
      name: "users",
      className: "User",
      columns: [
        {
          name: "id",
          fieldName: "id",
          javaType: "Long",
          sqlType: "BIGINT",
          primaryKey: true,
          autoIncrement: true,
          nullable: false,
        },
        {
          name: "email",
          fieldName: "email",
          javaType: "String",
          sqlType: "VARCHAR",
          length: 255,
          nullable: false,
          unique: true,
        },
        {
          name: "username",
          fieldName: "username",
          javaType: "String",
          sqlType: "VARCHAR",
          length: 100,
          nullable: false,
        },
        {
          name: "password_hash",
          fieldName: "passwordHash",
          javaType: "String",
          sqlType: "VARCHAR",
          length: 255,
          nullable: false,
        },
        {
          name: "created_at",
          fieldName: "createdAt",
          javaType: "LocalDateTime",
          sqlType: "TIMESTAMP",
          nullable: false,
        },
      ],
      relationships: [],
      position: { x: 100, y: 100 },
    })
  }

  if (promptLower.includes("product") || promptLower.includes("ecommerce") || promptLower.includes("shop")) {
    tables.push({
      id: `table-${Date.now()}-products`,
      name: "products",
      className: "Product",
      columns: [
        {
          name: "id",
          fieldName: "id",
          javaType: "Long",
          sqlType: "BIGINT",
          primaryKey: true,
          autoIncrement: true,
          nullable: false,
        },
        { name: "name", fieldName: "name", javaType: "String", sqlType: "VARCHAR", length: 255, nullable: false },
        { name: "description", fieldName: "description", javaType: "String", sqlType: "TEXT", nullable: true },
        { name: "price", fieldName: "price", javaType: "BigDecimal", sqlType: "DECIMAL", nullable: false },
        { name: "stock", fieldName: "stock", javaType: "Integer", sqlType: "INT", nullable: false },
        { name: "category_id", fieldName: "categoryId", javaType: "Long", sqlType: "BIGINT", nullable: true },
      ],
      relationships: [],
      position: { x: 450, y: 100 },
    })

    tables.push({
      id: `table-${Date.now()}-categories`,
      name: "categories",
      className: "Category",
      columns: [
        {
          name: "id",
          fieldName: "id",
          javaType: "Long",
          sqlType: "BIGINT",
          primaryKey: true,
          autoIncrement: true,
          nullable: false,
        },
        { name: "name", fieldName: "name", javaType: "String", sqlType: "VARCHAR", length: 100, nullable: false },
        { name: "parent_id", fieldName: "parentId", javaType: "Long", sqlType: "BIGINT", nullable: true },
      ],
      relationships: [],
      position: { x: 800, y: 100 },
    })
  }

  if (promptLower.includes("order") || promptLower.includes("cart") || promptLower.includes("purchase")) {
    tables.push({
      id: `table-${Date.now()}-orders`,
      name: "orders",
      className: "Order",
      columns: [
        {
          name: "id",
          fieldName: "id",
          javaType: "Long",
          sqlType: "BIGINT",
          primaryKey: true,
          autoIncrement: true,
          nullable: false,
        },
        { name: "user_id", fieldName: "userId", javaType: "Long", sqlType: "BIGINT", nullable: false },
        { name: "status", fieldName: "status", javaType: "String", sqlType: "VARCHAR", length: 50, nullable: false },
        { name: "total", fieldName: "total", javaType: "BigDecimal", sqlType: "DECIMAL", nullable: false },
        {
          name: "created_at",
          fieldName: "createdAt",
          javaType: "LocalDateTime",
          sqlType: "TIMESTAMP",
          nullable: false,
        },
      ],
      relationships: [],
      position: { x: 100, y: 350 },
    })

    tables.push({
      id: `table-${Date.now()}-order_items`,
      name: "order_items",
      className: "OrderItem",
      columns: [
        {
          name: "id",
          fieldName: "id",
          javaType: "Long",
          sqlType: "BIGINT",
          primaryKey: true,
          autoIncrement: true,
          nullable: false,
        },
        { name: "order_id", fieldName: "orderId", javaType: "Long", sqlType: "BIGINT", nullable: false },
        { name: "product_id", fieldName: "productId", javaType: "Long", sqlType: "BIGINT", nullable: false },
        { name: "quantity", fieldName: "quantity", javaType: "Integer", sqlType: "INT", nullable: false },
        { name: "price", fieldName: "price", javaType: "BigDecimal", sqlType: "DECIMAL", nullable: false },
      ],
      relationships: [],
      position: { x: 450, y: 350 },
    })
  }

  if (promptLower.includes("blog") || promptLower.includes("post") || promptLower.includes("article")) {
    tables.push({
      id: `table-${Date.now()}-posts`,
      name: "posts",
      className: "Post",
      columns: [
        {
          name: "id",
          fieldName: "id",
          javaType: "Long",
          sqlType: "BIGINT",
          primaryKey: true,
          autoIncrement: true,
          nullable: false,
        },
        { name: "title", fieldName: "title", javaType: "String", sqlType: "VARCHAR", length: 255, nullable: false },
        { name: "content", fieldName: "content", javaType: "String", sqlType: "TEXT", nullable: true },
        { name: "author_id", fieldName: "authorId", javaType: "Long", sqlType: "BIGINT", nullable: false },
        { name: "published", fieldName: "published", javaType: "Boolean", sqlType: "BOOLEAN", nullable: false },
        {
          name: "created_at",
          fieldName: "createdAt",
          javaType: "LocalDateTime",
          sqlType: "TIMESTAMP",
          nullable: false,
        },
      ],
      relationships: [],
      position: { x: 100, y: 200 },
    })
  }

  // If no specific keywords matched, create a generic table
  if (tables.length === 0) {
    tables.push({
      id: `table-${Date.now()}-items`,
      name: "items",
      className: "Item",
      columns: [
        {
          name: "id",
          fieldName: "id",
          javaType: "Long",
          sqlType: "BIGINT",
          primaryKey: true,
          autoIncrement: true,
          nullable: false,
        },
        { name: "name", fieldName: "name", javaType: "String", sqlType: "VARCHAR", length: 255, nullable: false },
        { name: "description", fieldName: "description", javaType: "String", sqlType: "TEXT", nullable: true },
        {
          name: "created_at",
          fieldName: "createdAt",
          javaType: "LocalDateTime",
          sqlType: "TIMESTAMP",
          nullable: false,
        },
      ],
      relationships: [],
      position: { x: 100, y: 100 },
    })
  }

  return tables
}

export function AiGenerateModal({ onClose }: AiGenerateModalProps) {
  const [prompt, setPrompt] = useState("")
  const [isGenerating, setIsGenerating] = useState(false)
  const { tables, setTables } = useGeneratorStore()

  const handleGenerate = async () => {
    if (!prompt.trim()) {
      toast.error("Please enter a description")
      return
    }

    setIsGenerating(true)

    try {
      const newTables = await mockAiGenerate(prompt)

      // Merge with existing tables
      const mergedTables = [
        ...tables,
        ...newTables.map((t, i) => ({
          ...t,
          position: {
            x: (t.position?.x || 100) + tables.length * 100,
            y: (t.position?.y || 100) + i * 50,
          },
        })),
      ]

      setTables(mergedTables)
      toast.success(`Generated ${newTables.length} table(s)!`)
      onClose()
    } catch {
      toast.error("Failed to generate schema")
    } finally {
      setIsGenerating(false)
    }
  }

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
        className="glass-strong rounded-2xl w-full max-w-lg overflow-hidden"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-border">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-primary/20 to-accent/20 flex items-center justify-center">
              <Sparkles className="w-4 h-4 text-primary" />
            </div>
            <h2 className="text-lg font-semibold">AI Schema Generator</h2>
          </div>
          <button onClick={onClose} className="p-2 rounded-lg hover:bg-secondary/50 transition-colors">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6">
          <p className="text-sm text-muted-foreground mb-4">
            Describe your database schema in plain English and we'll generate the tables for you.
          </p>

          <Textarea
            value={prompt}
            onChange={(e) => setPrompt(e.target.value)}
            placeholder="e.g., I need a database for an e-commerce platform with users, products, categories, orders, and order items..."
            rows={4}
            className="bg-input/50 resize-none mb-4"
          />

          <div className="text-xs text-muted-foreground mb-4">
            <strong>Tip:</strong> Include keywords like "user", "product", "order", "blog", "category" for better
            results.
          </div>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-border">
          <Button variant="outline" onClick={onClose} disabled={isGenerating}>
            Cancel
          </Button>
          <Button
            onClick={handleGenerate}
            disabled={isGenerating || !prompt.trim()}
            className="bg-gradient-to-r from-primary to-accent text-primary-foreground"
          >
            {isGenerating ? (
              <span className="flex items-center gap-2">
                <Loader2 className="w-4 h-4 animate-spin" />
                Generating...
              </span>
            ) : (
              <span className="flex items-center gap-2">
                <Sparkles className="w-4 h-4" />
                Generate Schema
              </span>
            )}
          </Button>
        </div>
      </motion.div>
    </motion.div>
  )
}
