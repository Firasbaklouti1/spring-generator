# Frontend Changes Required (Strict Requirements)

This document defines the mandatory API format and frontend changes required to support the multi-stack generator architecture.
Backward compatibility still exists, but the frontend must adopt the new format going forward.

---

## Summary

The backend now supports multiple technology stacks (Spring, Node, NestJS, FastAPI).
All new requests must use the new unified stackType and stack-specific config structure.

---

## API Changes

### 1. New Endpoint: `GET /api/stacks`

**Purpose**: List all available technology stacks.

**Response**:
```json
[
  {
    "id": "spring",
    "displayName": "Spring Boot",
    "language": "java",
    "defaultVersion": "17"
  }
  // Future: node, nest, fastapi
]
```

**Frontend Action**: Call this endpoint to populate a stack selector dropdown.

---

### 2. Updated: `GET /api/dependencies/groups`

**Change**: Now accepts optional `stackType` query parameter.

**Request**:
```
GET /api/dependencies/groups?stackType=SPRING
GET /api/dependencies/groups  // Defaults to SPRING
```

**Frontend Action**: You must pass the selected stackType when requesting dependencies.

---

### 3. Updated: `POST /api/generate/project` and `POST /api/generate/preview`

**Change**: `ProjectRequest` now uses composition with stack-specific configs.

#### New Format (must)
```json
{
  "stackType": "SPRING",
  "name": "Demo",
  "description": "Demo project",
  "packageName": "com.example.demo",
  "dependencies": [...],
  "tables": [...],
  
  "includeEntity": true,
  "includeRepository": true,
  "includeService": true,
  "includeController": true,
  "includeDto": false,
  "includeMapper": false,

  "springConfig": {
    "groupId": "com.example",
    "artifactId": "demo",
    "javaVersion": "17",
    "bootVersion": "3.2.0",
    "buildTool": "maven",
    "packaging": "jar"
  }
}
```

**Frontend Requirements**: 
1. Add `stackType` field to request
2. Move Spring-specific fields into `springConfig` object
3. Keep `groupId`, `artifactId`, etc. at root level for backward compatibility (both work)

---

## TypeScript Interface Updates

### Before
```typescript
interface ProjectRequest {
  groupId: string;
  artifactId: string;
  name: string;
  description: string;
  packageName: string;
  javaVersion: string;
  bootVersion: string;
  dependencies: Dependency[];
  tables: Table[];
  includeEntity: boolean;
  includeRepository: boolean;
  includeService: boolean;
  includeController: boolean;
  includeDto: boolean;
  includeMapper: boolean;
}
```

### After (must)
```typescript
type StackType = 'SPRING' | 'NODE' | 'NEST' | 'FASTAPI';

interface SpringConfig {
  groupId: string;
  artifactId: string;
  javaVersion: string;
  bootVersion: string;
  buildTool: 'maven' | 'gradle';
  packaging: 'jar' | 'war';
}

interface NodeConfig {
  nodeVersion: string;
  packageManager: 'npm' | 'yarn' | 'pnpm';
  useTypeScript: boolean;
  orm: 'prisma' | 'sequelize' | 'typeorm';
}

interface NestConfig {
  nodeVersion: string;
  packageManager: 'npm' | 'yarn' | 'pnpm';
  orm: 'typeorm' | 'prisma' | 'mikro-orm';
  useSwagger: boolean;
  useValidation: boolean;
}

interface FastAPIConfig {
  pythonVersion: string;
  packageManager: 'pip' | 'poetry' | 'pipenv';
  orm: 'sqlalchemy' | 'tortoise' | 'sqlmodel';
  useAsync: boolean;
  useAlembic: boolean;
}

interface ProjectRequest {
  // Stack selection
  stackType: StackType;
  
  // Common fields (all stacks)
  name: string;
  description: string;
  packageName: string;
  dependencies: Dependency[];
  tables: Table[];
  includeEntity: boolean;
  includeRepository: boolean;
  includeService: boolean;
  includeController: boolean;
  includeDto: boolean;
  includeMapper: boolean;
  
  // Stack-specific configs
  springConfig?: SpringConfig;
  nodeConfig?: NodeConfig;
  nestConfig?: NestConfig;
  fastapiConfig?: FastAPIConfig;
  
  // Legacy (backward compatible)
  groupId?: string;
  artifactId?: string;
  javaVersion?: string;
  bootVersion?: string;
}
```

---

## Required UI Changes

### 1. Stack Selector
A stack dropdown is required at the top of the form.
```
Technology Stack: [Spring Boot ▼]
```

### 2. Dynamic Form Fields
Show different config fields based on selected stack:
- **Spring**: groupId, artifactId, javaVersion, bootVersion
- **Node**: nodeVersion, packageManager (npm/yarn), useTypeScript
- **Nest**: Same as Node + useSwagger, useValidation
- **FastAPI**: pythonVersion, packageManager (pip/poetry), useAsync

### 3. Dependency Selector
Update dependency fetching to include stack type:
```typescript
const deps = await fetch(`/api/dependencies/groups?stackType=${selectedStack}`);
```

---

## Migration Checklist

- [ ] Fetch available stacks from `/api/stacks`
- [ ] Add stack selector UI
- [ ] Update `ProjectRequest` interface
- [ ] Conditionally show stack-specific config fields
- [ ] Update dependency fetching to include stackType
- [ ] Test with existing Spring projects (backward compatibility)
