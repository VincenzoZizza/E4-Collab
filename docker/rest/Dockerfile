FROM gradle:jdk21
WORKDIR /app

COPY rest/src src
COPY rest/build.gradle.kts .
COPY rest/settings.gradle.kts .
COPY rest/gradle/libs.versions.toml gradle/libs.versions.toml

# Build del progetto
RUN gradle build -x test --no-daemon --stacktrace --info

# Profilo backend e mysql di default
VOLUME ["/app/data"]

ARG SPRING_PROFILE=backend,mysql
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILE}

EXPOSE 8090
ENTRYPOINT ["java", "-jar", "/app/build/libs/E4-Collab-Rest-1.0-SNAPSHOT.jar"]