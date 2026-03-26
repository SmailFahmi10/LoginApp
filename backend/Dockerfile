# ---- Etapa 1: Compilación ----
# Usamos la imagen oficial de Maven con JDK 8 (Temurin es la distribución OpenJDK mantenida por Eclipse)
FROM maven:3.8.6-eclipse-temurin-8 AS build

WORKDIR /app

# Copiamos primero solo el pom.xml para aprovechar la caché de capas de Docker:
# si las dependencias no cambian, Docker no las vuelve a descargar
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y empaquetamos (sin ejecutar tests, se corren aparte)
COPY src ./src
RUN mvn package -DskipTests -B

# ---- Etapa 2: Ejecución ----
# Imagen ligera con el JRE de Temurin (sin Maven ni código fuente)
FROM eclipse-temurin:8-jre

WORKDIR /app

# Copiamos únicamente el JAR generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
