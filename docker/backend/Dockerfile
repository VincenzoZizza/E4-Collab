# ── STAGE 1: BUILD ─────────────────────────────────────────
FROM gradle:8.0-jdk17 AS builder
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar -x test --no-daemon

# ── STAGE 2: RUNTIME ───────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy AS runtime
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

# profilo backend e mysql di default
ARG SPRING_PROFILE=backend,mysql
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILE}

EXPOSE 8081

ENTRYPOINT ["java","-jar","app.jar"]