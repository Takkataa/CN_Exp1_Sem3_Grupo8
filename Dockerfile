# ============================================================
# Imagen Docker del microservicio de tickets (multi-stage)
# Etapa 1 compila el proyecto con Maven; etapa 2 solo trae el .jar
# para que la imagen final sea liviana (sin Maven ni código fuente).
# ============================================================

# ---------- Etapa 1: compilación ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Se copia primero solo el pom.xml para descargar dependencias.
# Docker cachea esta capa: si el pom no cambia, no se vuelven a descargar.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Se copia el código y se genera el .jar (sin tests, porque necesitan la base de datos)
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: ejecución ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Carpeta donde se monta el EFS dentro del contenedor (-v /mnt/efs/tickets:/app/efs)
RUN mkdir -p /app/efs

# Se trae solo el .jar compilado desde la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Puerto en el que escucha Spring Boot
EXPOSE 8080

# Comando que arranca la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
