# Build stage
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw -DskipTests -B package

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV PORT=8080
EXPOSE 8080
CMD ["sh","-c","java -jar -Dserver.port=${PORT} app.jar"]
