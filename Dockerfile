# syntax=docker/dockerfile:1

# --- Build stage -------------------------------------------------------
# Same JDK major version the app targets (see build.gradle toolchain) plus Node.js,
# since Gradle's buildFrontend task shells out to npm to build the React app.
FROM eclipse-temurin:25-jdk-noble AS build

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates gnupg \
    && curl -fsSL https://deb.nodesource.com/setup_24.x | bash - \
    && apt-get install -y --no-install-recommends nodejs \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace
COPY . .
# Windows Git checkouts (core.autocrlf=true) can corrupt gradlew's shebang with a trailing \r,
# which breaks `./gradlew` with "not found" (the interpreter path itself is invalid) — normalize
# line endings and re-set the executable bit unconditionally so the build works regardless of
# how the build context was checked out (.gitattributes prevents this for fresh clones, but
# this stays as a defensive fallback for existing corrupted checkouts).
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN ./gradlew build -x test --no-daemon \
    && find build/libs -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' -exec cp {} app.jar \;

# --- Runtime stage -------------------------------------------------------
FROM eclipse-temurin:25-jre-noble AS runtime

RUN groupadd --system app && useradd --system --gid app --home /app app
WORKDIR /app
COPY --from=build /workspace/app.jar app.jar
RUN chown app:app app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
