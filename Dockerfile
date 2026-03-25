# ==========================================
# Stage 1: Build
# ==========================================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml .
COPY src ./src

# Build do projeto
RUN mvn clean package -DskipTests -q

# ==========================================
# Stage 2: Runtime
# ==========================================
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="Itaú Desafio"
LABEL description="API de Elegibilidade de Cartões"
LABEL version="0.0.1"

WORKDIR /app

# Copia JAR da etapa de build
COPY --from=builder /build/target/cartoes-0.0.1-SNAPSHOT.jar app.jar

# Cria diretório de logs
RUN mkdir -p logs && chmod 777 logs

# Expõe porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Variáveis de ambiente
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=prod

# Entrypoint
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
