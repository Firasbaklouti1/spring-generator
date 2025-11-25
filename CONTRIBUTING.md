# Contributing to spring-generator

First off, thank you for considering contributing to spring-generator! It's people like you that make this tool better for everyone.

## Code of Conduct

This project and everyone participating in it is governed by respect and professionalism. Please be kind and courteous to others.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the issue list as you might find out that you don't need to create one. When you are creating a bug report, please include as many details as possible:

* **Use a clear and descriptive title**
* **Describe the exact steps to reproduce the problem**
* **Provide specific examples to demonstrate the steps**
* **Describe the behavior you observed and what you expected**
* **Include screenshots if possible**
* **Include your environment details** (OS, Java version, Node version, etc.)

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, please include:

* **Use a clear and descriptive title**
* **Provide a detailed description of the suggested enhancement**
* **Explain why this enhancement would be useful**
* **List any alternatives you've considered**

### Pull Requests

* Fill in the required template
* Follow the Java and TypeScript/React coding standards
* Include appropriate test coverage
* Update documentation as needed
* End all files with a newline

## Development Setup

### Backend Development
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### Frontend Development
```bash
cd frontend
npm install
npm run dev
```

## Styleguides

### Java Code Style
* Follow standard Java conventions
* Use meaningful variable and method names
* Add JavaDoc comments for public methods
* Keep methods focused and small

### TypeScript/React Code Style
* Use functional components with hooks
* Follow React best practices
* Use TypeScript types appropriately
* Keep components focused and reusable

### Git Commit Messages
* Use the present tense ("Add feature" not "Added feature")
* Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
* Limit the first line to 72 characters or less
* Reference issues and pull requests liberally after the first line

## Project Structure

* `backend/src/main/java/` - Java source code
* `backend/src/main/resources/templates/` - Freemarker templates
* `frontend/app/` - Next.js pages
* `frontend/components/` - React components

## Testing

Before submitting a pull request:
* Test backend: `mvn test`
* Test frontend: `npm test`
* Manual testing of the full generation flow

## Questions?

Feel free to open an issue for any questions!
