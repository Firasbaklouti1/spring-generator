# 🚀 spring-generator

A powerful, modern Spring Boot project generator with SQL-to-CRUD support. Generate production-ready Spring Boot applications with automatic entity generation, JPA relationships, and customizable dependencies - all through an intuitive web interface.

![spring-generator](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen) ![Java](https://img.shields.io/badge/Java-17%2B-orange) ![Next.js](https://img.shields.io/badge/Next.js-16-black) ![License](https://img.shields.io/badge/license-MIT-blue)

## ✨ Features

### 🎯 Core Features
- **🏗️ Project Generation**: Create complete Spring Boot projects matching start.spring.io functionality
- **📦 Dependency Management**: 25+ popular Spring Boot dependencies with intelligent selection UI
- **🗄️ SQL-to-CRUD**: Automatically generate Entity, Repository, Service, and Controller from SQL schemas
- **🔗 JPA Relationships**: Auto-detect and generate `@OneToMany`, `@ManyToOne`, `@OneToOne`, `@ManyToMany` relationships
- **📝 Smart SQL Parser**: Parses `CREATE TABLE` statements with foreign key constraints
- **⚡ Instant Download**: Get your ready-to-run Spring Boot project as a ZIP file

### 🎨 Frontend Features
- **Modern UI**: Beautiful gradient design with glassmorphism effects
- **Dependency Picker**: Searchable modal with category filtering
- **Real-time Filtering**: Find dependencies instantly as you type
- **Visual Feedback**: Green highlights, checkboxes, and pill-style tags
- **Responsive Design**: Works seamlessly on desktop and mobile

### 🛠️ Technical Features
- **Freemarker Templates**: Flexible code generation engine
- **Maven Support**: Standard pom.xml generation with BOM management
- **Multiple Databases**: Support for MySQL, PostgreSQL, H2, MariaDB, SQL Server
- **Spring Security**: Optional security and OAuth2 integration
- **API Documentation**: SpringDoc OpenAPI support
- **Developer Tools**: Lombok, DevTools, and Configuration Processor

## 🏗️ Architecture

```
springInitializer/
├── backend/          # Spring Boot backend (Java 17+)
│   ├── src/main/
│   │   ├── java/com/firas/generator/
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── service/          # Business logic & registry
│   │   │   ├── model/            # Domain models
│   │   │   └── util/             # SQL parser utilities
│   │   └── resources/
│   │       └── templates/        # Freemarker templates (.ftl)
│   └── pom.xml
│
└── frontend/         # Next.js 16 frontend (React 19)
    ├── app/                      # Next.js app router
    ├── components/               # React components
    │   └── DependencyPicker.tsx  # Dependency selection UI
    └── package.json
```

## 🚀 Getting Started

### Prerequisites
- **Backend**: Java 17+, Maven 3.6+
- **Frontend**: Node.js 18+, npm or pnpm

### Installation

#### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/spring-generator.git
cd spring-generator
```

#### 2. Start the Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
Backend will be available at `http://localhost:8080`

#### 3. Start the Frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend will be available at `http://localhost:3000`

## 📖 Usage

### Basic Project Generation

1. **Open the Web Interface**: Navigate to `http://localhost:3000`
2. **Configure Project Metadata**:
   - Group ID (e.g., `com.example`)
   - Artifact ID (e.g., `my-app`)
   - Java Version (17 or 21)
   - Spring Boot Version
3. **Select Dependencies**: Click "ADD DEPENDENCIES" to choose from:
   - Developer Tools (Lombok, DevTools)
   - Web (Spring Web, WebFlux)
   - Security (Spring Security, OAuth2)
   - SQL (JPA, JDBC, MySQL, PostgreSQL, etc.)
   - NoSQL (MongoDB, Redis, Elasticsearch)
   - I/O (Validation, Mail, Cache)
   - Ops (Actuator, OpenAPI)
4. **Add SQL Schema (Optional)**:
   ```sql
   CREATE TABLE users (
     id BIGINT PRIMARY KEY AUTO_INCREMENT,
     username VARCHAR(255),
     email VARCHAR(255)
   );
   
   CREATE TABLE posts (
     id BIGINT PRIMARY KEY AUTO_INCREMENT,
     title VARCHAR(500),
     content TEXT,
     user_id BIGINT,
     FOREIGN KEY (user_id) REFERENCES users(id)
   );
   ```
5. **Generate**: Click "Generate Project" to download your ZIP file
6. **Run**: Extract and run `mvn spring-boot:run`

### Advanced: JPA Relationship Detection

The SQL parser automatically detects and generates JPA relationships:

#### One-to-Many / Many-to-One
```sql
CREATE TABLE users (id BIGINT PRIMARY KEY, name VARCHAR(255));
CREATE TABLE posts (
  id BIGINT PRIMARY KEY,
  user_id BIGINT,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```
Generates: `User` entity with `@OneToMany List<Post>` and `Post` with `@ManyToOne User`

#### Many-to-Many
```sql
CREATE TABLE students (id BIGINT PRIMARY KEY, name VARCHAR(255));
CREATE TABLE courses (id BIGINT PRIMARY KEY, title VARCHAR(255));
CREATE TABLE student_courses (
  student_id BIGINT,
  course_id BIGINT,
  FOREIGN KEY (student_id) REFERENCES students(id),
  FOREIGN KEY (course_id) REFERENCES courses(id)
);
```
Generates: `Student` with `@ManyToMany List<Course>` and vice versa

#### One-to-One
```sql
CREATE TABLE profiles (
  id BIGINT PRIMARY KEY,
  user_id BIGINT UNIQUE,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```
Generates: `@OneToOne` relationship based on UNIQUE constraint

## 🔧 API Endpoints

### Backend REST API

#### Get Available Dependencies
```http
GET /api/dependencies/groups
```
Returns categorized list of all available Spring Boot dependencies.

#### Generate Project
```http
POST /api/generate/project
Content-Type: application/json

{
  "groupId": "com.example",
  "artifactId": "demo",
  "name": "demo",
  "description": "Demo project",
  "packageName": "com.example.demo",
  "javaVersion": "17",
  "bootVersion": "3.2.0",
  "dependencies": ["web", "data-jpa", "mysql"],
  "sqlContent": "CREATE TABLE users (...);"
}
```
Returns ZIP file with generated Spring Boot project.

## 🎨 Screenshots

### Main Interface
Beautiful gradient UI with all project configuration options

### Dependency Picker
Searchable modal with 25+ dependencies organized by category

### Generated Project
Production-ready Spring Boot application with entities, repositories, services, and controllers

## 🛣️ Roadmap

- [ ] JWT Authentication module generation
- [ ] Docker Compose generation
- [ ] Kubernetes deployment files
- [ ] Self-referencing relationships
- [ ] Cascade type configuration
- [ ] Custom packaging options
- [ ] Gradle support
- [ ] More database support (Oracle, Cassandra)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Firas**

## 🙏 Acknowledgments

- Inspired by [start.spring.io](https://start.spring.io)
- Built with Spring Boot, Next.js, and Tailwind CSS
- UI components from [shadcn/ui](https://ui.shadcn.com)

## 📧 Support

For issues, questions, or suggestions, please [open an issue](https://github.com/yourusername/spring-generator/issues).

---

⭐ **Star this repo** if you find it helpful!
