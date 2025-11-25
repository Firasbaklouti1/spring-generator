"use client"

import { useState, useEffect } from "react"
import { X, Search, Plus } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"

interface Dependency {
    id: string
    name: string
    description: string
    groupId: string
    artifactId: string
}

interface DependencyGroup {
    name: string
    dependencies: Dependency[]
}

interface DependencyPickerProps {
    selectedDependencies: string[]
    onDependenciesChange: (deps: string[]) => void
}

export default function DependencyPicker({ selectedDependencies, onDependenciesChange }: DependencyPickerProps) {
    const [isOpen, setIsOpen] = useState(false)
    const [groups, setGroups] = useState<DependencyGroup[]>([])
    const [searchTerm, setSearchTerm] = useState("")
    const [tempSelected, setTempSelected] = useState<string[]>([])

    useEffect(() => {
        fetch("http://localhost:8080/api/dependencies/groups")
            .then(res => res.json())
            .then(data => setGroups(data))
            .catch(err => console.error("Failed to fetch dependencies:", err))
    }, [])

    const handleOpen = () => {
        setTempSelected([...selectedDependencies])
        setIsOpen(true)
    }

    const handleClose = () => {
        setIsOpen(false)
        setSearchTerm("")
    }

    const handleToggle = (depId: string) => {
        if (tempSelected.includes(depId)) {
            setTempSelected(tempSelected.filter(id => id !== depId))
        } else {
            setTempSelected([...tempSelected, depId])
        }
    }

    const handleApply = () => {
        onDependenciesChange(tempSelected)
        handleClose()
    }

    const getDependencyById = (id: string): Dependency | null => {
        for (const group of groups) {
            const dep = group.dependencies.find(d => d.id === id)
            if (dep) return dep
        }
        return null
    }

    const handleRemoveSelected = (depId: string) => {
        onDependenciesChange(selectedDependencies.filter(id => id !== depId))
    }

    const filteredGroups = groups.map(group => ({
        ...group,
        dependencies: group.dependencies.filter(dep =>
            dep.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            dep.description.toLowerCase().includes(searchTerm.toLowerCase())
        )
    })).filter(group => group.dependencies.length > 0)

    return (
        <div className="mb-6">
            <div className="flex items-center justify-between mb-3">
                <label className="block text-sm font-medium text-white">Dependencies</label>
                <Button
                    type="button"
                    onClick={handleOpen}
                    className="bg-black hover:bg-gray-800 text-white text-sm px-4 py-2 h-auto"
                >
                    <Plus size={16} className="mr-2" />
                    ADD DEPENDENCIES
                </Button>
            </div>

            {selectedDependencies.length > 0 && (
                <div className="bg-white/10 backdrop-blur-sm rounded-lg p-4 border border-white/20">
                    <div className="flex flex-wrap gap-2">
                        {selectedDependencies.map(depId => {
                            const dep = getDependencyById(depId)
                            if (!dep) return null

                            return (
                                <div
                                    key={depId}
                                    className="inline-flex items-center gap-2 bg-purple-600/30 hover:bg-purple-600/40 border border-purple-400/50 rounded-md px-3 py-1.5 transition-colors group"
                                >
                                    <span className="text-sm font-medium text-white">{dep.name}</span>
                                    <button
                                        onClick={() => handleRemoveSelected(depId)}
                                        className="opacity-60 hover:opacity-100 transition-opacity"
                                    >
                                        <X size={14} className="text-white" />
                                    </button>
                                </div>
                            )
                        })}
                    </div>
                </div>
            )}

            {isOpen && (
                <div className="fixed inset-0 bg-black/60 backdrop-blur-sm flex items-start justify-center z-50 p-4 pt-16 overflow-y-auto">
                    <div className="bg-slate-800 rounded-xl w-full max-w-4xl shadow-2xl border border-slate-700/50">
                        <div className="sticky top-0 bg-slate-800 p-6 border-b border-slate-700/50 rounded-t-xl">
                            <div className="flex items-center justify-between mb-4">
                                <h2 className="text-2xl font-bold text-white">Add Dependencies</h2>
                                <button
                                    onClick={handleClose}
                                    className="text-gray-400 hover:text-white transition-colors p-1 hover:bg-white/10 rounded"
                                >
                                    <X size={24} />
                                </button>
                            </div>

                            <div className="relative">
                                <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 text-gray-400" size={20} />
                                <Input
                                    type="text"
                                    placeholder="Search dependencies by name or description..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                    className="pl-12 pr-4 bg-slate-700/50 border-slate-600 text-white placeholder:text-gray-400 h-12 text-base focus:border-purple-500 focus:ring-purple-500"
                                    autoFocus
                                />
                            </div>
                        </div>

                        <div className="p-6 max-h-[60vh] overflow-y-auto">
                            {filteredGroups.length === 0 ? (
                                <p className="text-gray-400 text-center py-12">No dependencies found matching "{searchTerm}"</p>
                            ) : (
                                <div className="space-y-6">
                                    {filteredGroups.map(group => (
                                        <div key={group.name} className="space-y-3">
                                            <h3 className="text-sm font-semibold text-gray-300 uppercase tracking-wider px-2">
                                                {group.name}
                                            </h3>
                                            <div className="space-y-2">
                                                {group.dependencies.map(dep => {
                                                    const isSelected = tempSelected.includes(dep.id)
                                                    return (
                                                        <div
                                                            key={dep.id}
                                                            onClick={() => handleToggle(dep.id)}
                                                            className={`p-4 rounded-lg cursor-pointer transition-all ${isSelected
                                                                    ? 'bg-green-600/20 border-2 border-green-500 shadow-lg shadow-green-500/20'
                                                                    : 'bg-slate-700/30 border-2 border-transparent hover:bg-slate-700/50 hover:border-slate-600'
                                                                }`}
                                                        >
                                                            <div className="flex items-start gap-4">
                                                                <div className="flex-shrink-0 mt-0.5">
                                                                    <div className={`w-6 h-6 rounded-md border-2 flex items-center justify-center transition-all ${isSelected
                                                                            ? 'bg-green-500 border-green-500'
                                                                            : 'border-gray-500 bg-slate-800/50'
                                                                        }`}>
                                                                        {isSelected && (
                                                                            <svg className="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={3}>
                                                                                <path strokeLinecap="round" strokeLinejoin="round" d="M5 13l4 4L19 7" />
                                                                            </svg>
                                                                        )}
                                                                    </div>
                                                                </div>
                                                                <div className="flex-1 min-w-0">
                                                                    <div className="flex items-center gap-2 mb-1">
                                                                        <span className="font-semibold text-white text-base">{dep.name}</span>
                                                                        <span className="text-xs px-2 py-0.5 rounded bg-green-600/80 text-white uppercase font-semibold tracking-wide">
                                                                            {group.name.split(' ')[0]}
                                                                        </span>
                                                                    </div>
                                                                    <p className="text-sm text-gray-300 leading-relaxed">{dep.description}</p>
                                                                </div>
                                                            </div>
                                                        </div>
                                                    )
                                                })}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>

                        <div className="sticky bottom-0 bg-slate-800 p-6 border-t border-slate-700/50 flex justify-between items-center rounded-b-xl">
                            <span className="text-gray-300 text-sm">
                                {tempSelected.length} {tempSelected.length === 1 ? 'dependency' : 'dependencies'} selected
                            </span>
                            <div className="flex gap-3">
                                <Button
                                    type="button"
                                    onClick={handleClose}
                                    variant="outline"
                                    className="border-slate-600 text-white hover:bg-slate-700 hover:text-white"
                                >
                                    Cancel
                                </Button>
                                <Button
                                    type="button"
                                    onClick={handleApply}
                                    className="bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 text-white font-semibold px-6"
                                >
                                    Add Selected
                                </Button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
