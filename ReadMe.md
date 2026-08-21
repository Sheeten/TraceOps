# TraceOps

TraceOps is a graph-powered software analysis platform that helps developers analyze Java projects, discover code dependencies, detect potential incidents, and perform impact and root-cause analysis.

The application allows users to upload a Java project as a ZIP file. TraceOps analyzes the project structure and source code, stores dependency relationships in CognoDB, and provides an interactive web interface for exploring the resulting graph.

---

## Features

- Upload Java projects as ZIP files
- Analyze Java project structure
- Detect Java classes and dependencies
- Store dependency relationships in CognoDB
- Interactive dependency graph visualization
- View dependencies of a selected class
- View classes that depend on a selected class
- Perform multi-hop dependency traversal
- Detect potential code incidents
- Detect broad exception handling
- Detect potential exception conditions
- Perform incident impact analysis
- Perform root-cause analysis
- Analyze multiple uploaded projects

---

## Use Case

Modern Java applications contain many interconnected classes, services, repositories, and other components.

When a problem occurs, developers need to understand:

- What does the affected class depend on?
- Which classes depend on it?
- What components could be affected?
- What is the dependency path between components?
- What code pattern may have caused the issue?
- What is the likely root cause?
- How can the issue be resolved?

TraceOps addresses these questions by representing software dependencies and incident relationships as a graph.

---

## Why a Graph Database?

Software dependency analysis is primarily a relationship-based problem.

For example:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
````

In a relational database, discovering such multi-level relationships can require multiple joins.

A graph database represents these relationships directly:

```text
Controller
    │
    └── DEPENDS_ON
            ↓
         Service
            │
            └── DEPENDS_ON
                    ↓
                 Repository
```

This makes dependency traversal and impact analysis natural graph operations.

TraceOps uses CognoDB as its graph database and uses parameterized Cypher queries to retrieve and analyze graph relationships.

---

## Technology Stack

### Backend

* Java
* Spring Boot
* Spring Web
* Spring Data Neo4j
* Neo4j Java Driver
* CognoDB
* Maven

### Frontend

* React
* JavaScript
* React Flow
* Axios
* HTML
* CSS

### Database

* CognoDB
* Cypher
* Bolt protocol

---

# System Architecture

```text
                    ┌──────────────────────┐
                    │      React UI        │
                    │                      │
                    │ Project Analysis     │
                    │ Dependency Graph     │
                    │ Incident Analysis    │
                    │ Impact Analysis      │
                    │ Root Cause Analysis  │
                    └──────────┬───────────┘
                               │
                               │ REST API
                               ↓
                    ┌──────────────────────┐
                    │    Spring Boot       │
                    │      Backend         │
                    └──────────┬───────────┘
                               │
             ┌─────────────────┼─────────────────┐
             ↓                 ↓                 ↓
       Controllers         Services           Analyzers
             │                 │                 │
             └─────────────────┼─────────────────┘
                               ↓
                    ┌──────────────────────┐
                    │ IncidentRepository   │
                    │                      │
                    │ Parameterized        │
                    │ Cypher Queries       │
                    └──────────┬───────────┘
                               │
                               ↓
                    ┌──────────────────────┐
                    │       CognoDB        │
                    │    Graph Database    │
                    └──────────────────────┘
```

---

# Graph Data Model

## Project and Service Graph

```text
Project
   │
   └── HAS_SERVICE
          ↓
       Service
          │
          └── DEPENDS_ON
                 ↓
              Service
```

## Incident Graph

```text
Incident
   │
   ├── AFFECTS ───────→ Service
   │
   └── HAS_ERROR ─────→ Error
                           │
                           └── HAS_CAUSE
                                  ↓
                                Cause
                                  │
                                  └── HAS_SOLUTION
                                         ↓
                                      Solution
                                         │
                                         └── APPLIES_TO
                                                ↓
                                            Database
```

## Code Dependency Graph

```text
CodeClass
   │
   └── CODE_DEPENDS_ON
             ↓
          CodeClass
```

The `CodeClass` node contains:

* `name`
* `project`
* `module`

The `CODE_DEPENDS_ON` relationship contains:

* `relationship`

---

# Project Analysis

A Java project can be uploaded as a ZIP file.

The processing flow is:

```text
ZIP Upload
    ↓
ProjectUploadService
    ↓
ZIP Extraction
    ↓
ProjectAnalyzerService
    ↓
Java Source Analysis
    ↓
Class Detection
    ↓
Dependency Detection
    ↓
Graph Creation
    ↓
CognoDB
```

TraceOps analyzes Java source files and detects relationships between classes.

The resulting code dependency graph is stored in CognoDB.

---

# Dependency Graph

TraceOps provides an interactive dependency graph using React Flow.

For example:

```text
PatientController
        │
        │ USES
        ↓
PatientService
        │
        │ USES
        ↓
PatientRepository
```

Users can select a node to view:

### Dependencies

Classes that the selected class depends on.

### Used By

Classes that depend on the selected class.

TraceOps also supports complete dependency-path traversal for multi-hop relationships.

---

# Incident Detection

TraceOps performs static source-code analysis to detect potentially problematic code patterns.

Examples include:

### Broad Exception Handling

```java
catch (Exception e)
```

### Explicit Exception Throwing

```java
throw new RuntimeException("...");
```

### Optional Access

```java
optional.get();
```

### Optional Exception Handling

```java
optional.orElseThrow();
```

### Array Access

```java
array[index];
```

### List Access

```java
list.get(index);
```

### Number Parsing

```java
Integer.parseInt(value);
```

### Division by Zero

```java
value / 0;
```

### Runtime Error Indicators

```text
logger.error(...)
log.error(...)
connection refused
timeout
```

Detected incidents contain:

* Incident type
* Message
* File
* Line number
* Class name
* Method name

---

# Impact Analysis

TraceOps provides impact analysis for detected incidents.

For a selected class and method, the application determines:

```text
Dependencies
```

and:

```text
Used By
```

Example:

```text
PatientController
        ↓
PatientService
        ↓
PatientRepository
```

If a component is affected, its dependency relationships can be traversed to understand potentially affected components.

---

# Root Cause Analysis

TraceOps provides rule-based root-cause analysis for detected incidents.

The RCA result contains:

* Issue
* Likely cause
* Trigger
* Suggested resolution

For example, for broad exception handling:

```text
Issue:
The method catches the generic Exception type instead of
handling specific exceptions.

Likely Cause:
Broad exception handling can hide the actual type and cause
of an error.

Trigger:
An exception occurs inside the try block and is caught by
the generic catch block.

Suggested Resolution:
Catch specific exception types and handle them separately.
```

---

# API Endpoints

## Graph

### Dependencies

```http
GET /api/graph/dependencies
```

Parameters:

```text
project
module
className
```

### Dependents

```http
GET /api/graph/dependents
```

Parameters:

```text
project
module
className
```

### Complete Dependency Paths

```http
GET /api/graph/complete
```

Parameters:

```text
project
module
className
```

---

## Projects

### Upload Project

```http
POST /api/projects/upload
```

The endpoint accepts a ZIP file using the multipart field:

```text
file
```

### Get Projects

```http
GET /api/projects
```

### Get Services

```http
GET /api/projects/{projectName}/services
```

### Get Project Information

```http
GET /api/projects/{projectName}/{moduleName}/info
```

---

## Incidents

### Get All Incidents

```http
GET /api/incidents
```

### Get Incident Details

```http
GET /api/incidents/{incidentId}
```

### Get Incident Impact

```http
GET /api/incidents/{incidentId}/impact
```

### Get Incident RCA

```http
GET /api/incidents/{incidentId}/rca
```

### Get Incident Graph

```http
GET /api/incidents/{incidentId}/graph
```

### Get Project Incidents

```http
GET /api/incidents/projects/{projectName}/incidents
```

### Project Impact Analysis

```http
GET /api/incidents/projects/{projectName}/incidents/{className}/impact
```

Query parameter:

```text
methodName
```

### Project RCA

```http
GET /api/incidents/projects/{projectName}/incidents/{className}/rca
```

Query parameter:

```text
methodName
```

---

# Cypher and Graph Queries

TraceOps communicates with CognoDB using Cypher queries.

Queries use parameters for project, module, class, and other user-provided values instead of directly concatenating user input into query strings.

Example:

```cypher
MATCH (c:CodeClass)
WHERE c.project = $project
  AND c.module = $module
RETURN c
```

This allows graph queries to remain parameterized and reusable.

---

# Multi-Hop Traversal

TraceOps supports multi-hop dependency traversal.

For example:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

A multi-hop traversal can discover the complete path rather than only the directly connected node.

TraceOps supports dependency traversal up to three hops in its graph analysis queries.

This is useful for:

* Dependency analysis
* Impact analysis
* Root-cause investigation
* Understanding service relationships

---

# Seed Data

The repository contains a Cypher seed script:

```text
scripts/seed.cypher
```

The seed data demonstrates:

* Projects
* Services
* Service dependencies
* Code classes
* Code dependencies
* Incidents
* Errors
* Causes
* Solutions
* Databases

The script also contains example graph queries including multi-hop traversal and incident impact analysis.

---

# Running the Application

## Prerequisites

Install:

* Java
* Maven
* Node.js
* npm
* CognoDB account and database instance

---

# Backend Setup

Navigate to the Spring Boot backend directory.

Run:

```bash
mvn clean install
```

Then:

```bash
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

---

# Frontend Setup

Navigate to the React frontend directory.

Install dependencies:

```bash
npm install
```

Start the frontend:

```bash
npm run dev
```

The frontend runs on:

```text
http://localhost:5173
```

---

# Environment Configuration

Database credentials must be stored using environment variables.

Example:

```text
COGNODB_URI=your-cognodb-uri
COGNODB_USERNAME=your-username
COGNODB_PASSWORD=your-password
```

Use the variable names configured by the application.

Do not commit passwords, API keys, or other secrets to GitHub.

Local environment files containing secrets should be included in `.gitignore`.

---

# Using TraceOps

1. Start the CognoDB database.
2. Start the Spring Boot backend.
3. Start the React frontend.
4. Open the application in the browser.
5. Upload a Java project ZIP file.
6. Select the uploaded project.
7. Analyze the project.
8. Explore the dependency graph.
9. Select a graph node to view its dependencies and dependents.
10. Open Incident Analysis.
11. Review detected issues.
12. Run Impact Analysis.
13. Run Root Cause Analysis.

---

# Project Structure

```text
TraceOps/
│
├── README.md
├── .gitignore
│
├── backend/
│   ├── pom.xml
│   │
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/
│           │       └── traceops/
│           │           └── traceops/
│           │               │
│           │               ├── controller/
│           │               │   ├── GraphController.java
│           │               │   ├── IncidentController.java
│           │               │   └── ProjectController.java
│           │               │
│           │               ├── model/
│           │               │
│           │               ├── repo/
│           │               │   └── IncidentRepository.java
│           │               │
│           │               └── service/
│           │                   ├── GraphService.java
│           │                   ├── IncidentService.java
│           │                   ├── ProjectAnalyzerService.java
│           │                   ├── ProjectCodeAnalyzerService.java
│           │                   ├── ProjectIncidentAnalyzerService.java
│           │                   ├── ProjectImpactAnalyzerService.java
│           │                   ├── ProjectRcaAnalyzerService.java
│           │                   └── ProjectUploadService.java
│           │
│           └── resources/
│               └── application.properties
│
├── frontend/
│   ├── package.json
│   └── src/
│
├── scripts/
│   └── seed.cypher
│
└── docs/
    └── screenshots/
```

---

# Security

The following must not be committed to GitHub:

* CognoDB passwords
* Database credentials
* API keys
* Private tokens
* Local environment files containing secrets

Use environment variables for sensitive configuration.

---

# Screenshots

Add screenshots of the working application to:

```text
docs/screenshots/
```

Recommended screenshots:

* Project Analysis
* Dependency Graph
* Incident Analysis
* Impact Analysis
* Root Cause Analysis

Example:

```markdown
![Dependency Graph](docs/screenshots/dependency-graph.png)
```

---

# Demo

Hosted application:

```text
TODO: Add hosted application URL
```

---

# Screen Recording

Demo video:

```text
TODO: Add screen recording URL
```

The demonstration should cover:

1. Project upload
2. Project analysis
3. Dependency graph
4. Incident detection
5. Impact analysis
6. Root-cause analysis

---

# Future Improvements

Possible improvements include:

* AST-based Java analysis
* More incident detection rules
* Runtime log correlation
* Historical incident tracking
* Automated remediation suggestions
* Incident severity scoring
* Authentication and authorization
* Support for additional programming languages
* Advanced graph visualization
* Automated deployment

---

# Author

**Sheeten Karmankar**

**TraceOps — Graph-Powered Software Dependency and Incident Analysis**

