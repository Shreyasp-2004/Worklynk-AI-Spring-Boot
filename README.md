
# Worklynk AI — Intelligent HR Helpdesk

An AI-powered HR helpdesk built with **Java, Spring Boot, PostgreSQL, Spring AI, and Retrieval-Augmented Generation (RAG)**. The platform helps employees find answers to HR policy questions using uploaded PDF documents and provides HR ticket management capabilities.

## Overview

Worklynk AI simplifies access to company HR policies by allowing employees to ask questions in natural language. The application retrieves relevant information from uploaded policy documents and uses a locally running Large Language Model (LLM) to generate context-aware responses.

The project combines backend development, document processing, vector search, and local AI integration in a Spring Boot application.

## Key Features

- **AI-Powered HR Assistant:** Answer employee questions about HR policies using natural language.
- **Retrieval-Augmented Generation (RAG):** Ground AI responses in relevant information retrieved from uploaded documents.
- **PDF Policy Ingestion:** Upload HR policy PDFs and extract text for processing.
- **Intelligent Document Chunking:** Split extracted text into smaller chunks for retrieval.
- **Vector Similarity Search:** Store and retrieve document embeddings using PostgreSQL and pgvector.
- **Local LLM Integration:** Generate responses using Ollama without relying on a hosted LLM API.
- **Source Attribution:** Include the source document name with AI-generated responses.
- **HR Ticket Management:** Create support tickets, retrieve tickets by employee, and update ticket status.

## Technology Stack

| Component | Technology |
|---|---|
| Programming Language | Java |
| Backend Framework | Spring Boot |
| AI Integration | Spring AI |
| Language Model Runtime | Ollama |
| Chat Model | Llama 3.2 |
| Embedding Model | nomic-embed-text |
| Relational Database | PostgreSQL |
| Vector Search | pgvector |
| ORM | Spring Data JPA / Hibernate |
| PDF Processing | Apache PDFBox |
| Build Tool | Apache Maven |
| API Testing | Postman |
| Version Control | Git and GitHub |

## System Architecture

### AI Question-Answering Workflow

```text
Employee
   |
   v
REST API
   |
   v
AI Assistant Service
   |
   v
Vector Similarity Search
   |
   v
PostgreSQL + pgvector
   |
   v
Relevant Policy Chunks
   |
   v
Ollama — Llama 3.2
   |
   v
Context-Aware Answer
```

### Document Ingestion Workflow

```text
HR Policy PDF
     |
     v
Text Extraction using PDFBox
     |
     v
Text Chunking
     |
     v
Embedding Generation
     |
     v
PostgreSQL + pgvector
```

## How RAG Works

Worklynk AI uses Retrieval-Augmented Generation to answer questions using uploaded HR policy documents.

1. An administrator uploads a policy PDF.
2. The application extracts text from the document.
3. The extracted text is divided into smaller chunks.
4. The chunks are converted into vector embeddings and stored in PostgreSQL using pgvector.
5. When an employee asks a question, the application searches for relevant chunks.
6. The retrieved context is passed to the language model through Spring AI.
7. Ollama generates a response based on the retrieved policy information.

This approach helps connect AI-generated answers with the organization's available HR documentation. Answer accuracy depends on document quality, retrieval relevance, and model behavior.

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/ai/ask` | Ask an HR policy question |
| POST | `/api/policies` | Upload a PDF policy document |
| POST | `/api/tickets` | Create an HR support ticket |
| GET | `/api/tickets/employee/{employeeId}` | Retrieve tickets for an employee |
| PATCH | `/api/tickets/{ticketId}/status` | Update ticket status |

### Ask the AI Assistant

**Endpoint**

```http
POST /api/ai/ask
Content-Type: application/json
```

**Request Body**

```json
{
  "question": "How many casual leave days are available per year?"
}
```

The application searches the vector store for relevant policy information and generates a response using the retrieved context.

### Upload an HR Policy

**Endpoint**

```http
POST /api/policies
```

Send a `multipart/form-data` request with the PDF document in the `file` field.

### Ticket Management

The ticket APIs support creating employee support tickets, retrieving tickets associated with an employee, and updating ticket status.

## Prerequisites

Install the following software before running the application:

- Java 25, matching the current development environment
- Apache Maven 3.9+
- PostgreSQL
- pgvector extension
- Ollama

## Installation and Setup

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/Worklynk-AI-Spring-Boot.git
cd Worklynk-AI-Spring-Boot
```

Replace `YOUR_USERNAME` with your GitHub username.

### 2. Create the Database

Create the PostgreSQL database:

```sql
CREATE DATABASE worklynkjava;
```

Connect to the database and enable pgvector if it is installed:

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

Ensure your PostgreSQL server is running before starting the application.

### 3. Configure Environment Variables

Set your database credentials as environment variables.

**Windows PowerShell**

```powershell
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your_database_password"
```

Update `src/main/resources/application.yml` to reference these variables:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/worklynkjava
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

Preserve the other existing application settings, including JPA, Ollama, and vector store configuration.


Install Ollama from [the official website](https://ollama.com/).

Download the required models:

```bash
ollama pull llama3.2:3b
ollama pull nomic-embed-text
```

Ensure Ollama is running at:

```text
http://localhost:11434
```

Configure the Ollama base URL and model names in `application.yml` according to your Spring AI configuration.

### 5. Run the Application

From the project root, execute:

```bash
mvn spring-boot:run
```

The application runs at:

```text
http://localhost:8080
```

## Project Structure

The backend follows a layered Spring Boot architecture.

```text
Worklynk-AI-Spring-Boot/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/shreyas/worklynk_ai/
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── .gitignore
├── pom.xml
└── README.md
```

The directory structure is illustrative; the exact package organization may evolve as development continues.

## Configuration Reference

| Setting | Purpose |
|---|---|
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| Datasource URL | PostgreSQL connection configuration |
| Ollama base URL | Local model runtime endpoint |
| Chat model | Model used to generate answers |
| Embedding model | Model used to create document embeddings |

## Engineering Considerations

- **Retrieval Quality:** Relevant document chunks are essential for accurate responses.
- **Hallucination Control:** Answers should remain grounded in retrieved policy content.
- **Document Processing:** Scanned PDFs may require OCR because text extraction alone may not recover their content.
- **Data Security:** Database credentials and sensitive employee information must be protected.
- **Error Handling:** Invalid uploads, empty documents, and database errors should be handled safely.
- **Scalability:** Retrieval configuration, indexing, and model selection can be optimized as the document collection grows.

## Future Enhancements

- Employee-facing web interface
- Authentication and role-based authorization
- Automated unit and integration tests
- Improved retrieval evaluation and document ranking
- Support for additional document formats
- Deployment with production-ready configuration
- Enhanced observability and error reporting

## Disclaimer

Worklynk AI is a development project intended to demonstrate AI-assisted HR policy retrieval and backend application development. AI-generated responses may be incomplete or inaccurate. Official HR documentation and authorized HR personnel should remain the source of truth for important employment and benefits decisions.

## Author

**Shreyas Palsodkar**

GitHub: [Shreyasp_2004](https://github.com/Shreyasp-2004)

---

*Built with Java, Spring Boot, Spring AI, PostgreSQL, pgvector, and Ollama.*
