# Resumetor - AI-Powered Job Application Assistant

Resumetor is a private, local-first AI-powered desktop application built with **JavaFX** and **SQLite** that helps users create tailored, ATS-friendly resumes, cover letters, and job-application emails based on their career profile and a target job description.

## Architecture

The application is built using a clean architecture pattern:
- **`app.resumetor`**: App entry point and configuration.
- **`app.resumetor.controller`**: JavaFX programmatic UI layouts (Onboarding, MainLayout, ProfileDashboard).
- **`app.resumetor.model`**: POJOs for domain entities (Profile, Experience, Project, etc.).
- **`app.resumetor.repository`**: SQLite DAOs for local persistence.
- **`app.resumetor.service`**: Business logic, including `AiService` for LLM orchestration.
- **`app.resumetor.ai`**: OpenRouter API integration using Java 11+ `HttpClient`.
- **`app.resumetor.security`**: Privacy layer (`PiiSanitizer`) that redacts personal data before API calls.
- **`app.resumetor.export`**: ATS-friendly document generation (`DocxExporter`, `PdfExporter`).
- **`app.resumetor.importexport`**: Vault management for data portability.

## Prerequisites
- **Java 21** or higher.
- **Maven 3.6+**.

## Getting Started

1. Set up your OpenRouter API Key:
   Create a `.env` file in the root directory and add your API key:
   ```env
   OPENROUTER_API_KEY=sk-or-v1-...
   ```

2. Compile and run the application using Maven:
   ```bash
   mvn clean javafx:run
   ```

## Privacy & Security

Resumetor is designed to keep your personal data secure. The `PiiSanitizer` automatically scrubs direct contact information (Name, Email, Phone, Address, URLs) from your career profile before sending contextual data to the LLM. When the LLM generates the resume, it returns it with placeholders (e.g., `[NAME]`), and the application injects your real details locally before creating the DOCX/PDF files.

## Document Generation

Resumetor uses:
- **Apache POI**: For generating clean, ATS-compliant `.docx` resumes.
- **Apache PDFBox**: For exporting basic `.pdf` resumes.

## Export & Portability

You can export your entire career profile into a `.resumevault` JSON file from the **Settings** menu. This allows you to migrate your data securely between devices.
