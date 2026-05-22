# ── FASE 1: COMPILAR ─────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiamos solo el pom.xml primero para aprovechar el cache de capas de Docker.
# Si el pom.xml no cambia, Docker reutiliza la capa de dependencias en el
# siguiente build sin volver a descargarlas.
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests


# ── FASE 2: CORRER ───────────────────────────────────────────────────────────
# Imagen JRE slim — no incluye Maven ni el compilador, solo lo necesario para
# ejecutar el JAR. Pesa ~180MB vs ~500MB de la imagen de build.
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
