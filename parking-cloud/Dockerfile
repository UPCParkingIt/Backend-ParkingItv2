# ─── STAGE 1: Build ───────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /workspace

# Copiar estructura multi-módulo
COPY pom.xml .
COPY parking-shared ./parking-shared
COPY parking-cloud ./parking-cloud
COPY .mvn ./.mvn
COPY mvnw .

RUN chmod +x mvnw
RUN ./mvnw clean package -pl parking-cloud -am -DskipTests

# ─── STAGE 2: Runtime ─────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /workspace/parking-cloud/target/parking-cloud-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]