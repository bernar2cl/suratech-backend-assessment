# Quote Aggregator Service - SURAtech Technical Assessment

This repository contains the technical assessment for the Backend Developer position at SURAtech. The solution implements a resilient, cloud-native microservice for quote management.

## 🛠️ Tech Stack
- **Language:** Java 21
- **Framework:** Spring Boot 3.3
- **Resilience:** Resilience4j (Circuit Breaker, Retry patterns)
- **Monitoring:** Spring Boot Actuator + Micrometer + Prometheus
- **Testing:** JUnit 5, Mockito, and k6 for Performance Testing
- **Infra:** Docker, Kubernetes manifests, and Azure DevOps Pipeline

## 📂 Project Structure
- **/src**: Main application source code and unit tests.
- **/docs**: OpenAPI 3.0 specification (`openapi.yaml`) and Reliability/Integration design.
- **/k8s**: Kubernetes Deployment, Service, and HPA manifests.
- **/sql**: Optimized SQL queries and indexing strategy.
- **/scripts**: Performance testing scripts (k6).
- **AI_USAGE.md**: Detailed documentation of AI prompts and architectural decisions (Mandatory).
- **RUNBOOK.md**: Operational guide and troubleshooting.

## 🚀 Execution Guide

### 1. Build the application
Compile the project and generate the JAR file using Gradle:
```bash
./gradlew clean bootJar