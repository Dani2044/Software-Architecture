# --- build stage ---
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY sam/pom.xml .
RUN mvn -B -q dependency:go-offline
COPY sam/src ./src
RUN mvn -B -q package -DskipTests

# --- runtime stage ---
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S sps && adduser -S sps -G sps
USER sps
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8086
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
