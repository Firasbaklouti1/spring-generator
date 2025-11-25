# 🚀 Project Roadmap — spring-generator (Custom Spring Project Generator)

## 🎯 Vision
spring-generator aims to become a fully customizable, extensible alternative to start.spring.io  
that not only generates Spring Boot starter projects, but also:
- Auto-generates code from database schemas (entities, repos, services, controllers)
- Offers opinionated architectures (clean architecture, layered, hexagonal)
- Supports plugin-based features
- Provides a public API + MCP server for IDE integrations
- Generates boilerplate code for other frameworks (optional future goal)

---

## 🗺️ Phase 1 — Core MVP (Foundation)
**Goal: Make a functional clone of Start.Spring.io with your features.**

### ✔ Completed
- Basic UI for project configuration  
- Backend that builds Spring Boot structure  
- Export project as ZIP  

### 🔜 To Do
- Add SQL → Entities generator  
- Simple JPA Repository generator  
- Service + REST Controller templates  
- Add presets: `Web API`, `Full CRUD`, `Security Basic`, `MySQL Starter`  

---

## 🧩 Phase 2 — Advanced Code Generator
**Goal: Go beyond Spring Initializr.**

### Features
- Upload SQL file or connect to DB → auto-generate:
  - Entities with relationships  
  - DTOs  
  - Services  
  - Repositories  
  - Controllers  
- Ability to select:
  - Architecture style: Layered / Clean / Hexagonal  
  - Build tool: Maven / Gradle  
  - Java version  
- Custom template engine (Freemarker / Mustache)

---

## ⚡ Phase 3 — Developer Tools & Automation
### Features
- **Live preview** of generated code  
- **Error checking** in SQL schema  
- **Auto-generate Postman collection**  
- **Auto-add docker-compose** for database  
- **Project initializer presets** (Ex: microservice preset)  
- **CLI tool**: `springforge init`  

---

## 🧠 Phase 4 — AI & MCP Integration
### Features
- MCP server to let IDEs create and modify projects  
- AI-assisted code generation:
  - Write CRUD
  - Create database schema
  - Add new modules to existing projects  
- Visual ERD builder → generates Spring boot modules  
- VS Code extension for 1-click project creation  

---

## 🌐 Phase 5 — SaaS / Monetization (Optional)
### Features
- User accounts + cloud storage for templates  
- Paid tiers:  
  1. **Starter** → basic generators  
  2. **Pro** → AI + advanced codegen  
  3. **Team** → shared templates + private generators  
- API key billing for automated project generation  

---

## 🏁 Phase 6 — Ecosystem Expansion
### Future possible directions
- Generate Angular/React frontend paired with backend  
- Generate microservices monorepo with gateway, discovery, config server  
- Plugin marketplace  
- CRUD panel builder  
- DevOps generator (Docker + CI/CD pipelines)  

---

## 📌 Notes & Priorities
- Keep generator modules clean and extensible  
- Build solid documentation  
- Provide high-quality templates  
- Ensure fast ZIP generation  
- Implement robust error handling  

---

# 🧭 Long-term Goal
Build the **#1 open-source Spring Boot code generator** with optional AI support —  
something that developers actually use daily to start projects faster.