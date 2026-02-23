# ===== Builder stage =====
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /workspace

# Gradle wrapper + build files first for better layer caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

# Make wrapper executable and build the bootJar
RUN chmod +x ./gradlew && \
    ./gradlew clean bootJar --no-daemon

# ===== Runtime stage =====
FROM eclipse-temurin:21-jre

ENV APP_USER=spring
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

RUN addgroup --system ${APP_USER} && adduser --system --ingroup ${APP_USER} ${APP_USER}
USER ${APP_USER}

WORKDIR /app

# Copy Spring Boot fat jar from builder
COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]