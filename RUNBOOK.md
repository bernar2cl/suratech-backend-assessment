# Quote Service – Runbook

## 1. Running the application

### 1.1 Prerequisites

- Java 21 (JDK)
- Docker (optional, for containerized runs)
- Kubernetes cluster (optional, for k8s deployment)
- k6 (for performance testing)
- Access to a relational database (H2 in-memory is configured by default)
- Valid JWT tokens if `spring.security.oauth2.resourceserver.jwt.*` is enabled

### 1.2 Local run with Gradle

# From project root
./gradlew clean bootRun