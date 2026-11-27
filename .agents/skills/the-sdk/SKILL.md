---
name: the-sdk
description: Usage guide for the-sdk 2.0 common library (Spring Boot 4.0.x) — HTTP request/response logging with UUID Log-ID, CommonApiResponse wrapping, ExpectedException handling, and Swagger auto-configuration.
---

# the-sdk Usage Guide

`io.github.themoment-team:the-sdk:2.0` — the core common library for this project (Maven Central).  
Controlled via `sdk.*` settings in `application.yml`. Package name is `team.themoment.sdk`.

## Requirements

- Java 17+, Spring Boot 4.0.x (Spring Framework 7, Jackson 3)
  - Spring Boot 3.x projects use the-sdk 1.x and pin this skill to `v1` in `.harness/sync.yml` (`overrides: { codex/skills/the-sdk: "v1" }`)
- The SDK does not bring Spring Boot dependencies or the BOM — declare them in the consuming project:
  - Spring Boot version management: `org.springframework.boot` plugin with the `spring-boot-dependencies` BOM
  - `org.springframework.boot:spring-boot-starter-web`
  - Kotlin: `tools.jackson.module:jackson-module-kotlin` (Jackson 3)
- The SDK brings `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3` (Spring Boot 4.0.x line) as an `api` dependency — no separate springdoc declaration is needed
- Resolved from `mavenCentral()` — no extra repository needed

```kotlin
plugins {
    id("org.springframework.boot") version "4.0.9" // version the-sdk 2.0 is built and tested against
}

repositories {
    mavenCentral()
}

dependencies {
    // The Spring Boot plugin alone does not manage dependency versions; import the BOM
    implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("tools.jackson.module:jackson-module-kotlin") // Kotlin projects only
    implementation("io.github.themoment-team:the-sdk:2.0")
}
```

## Features

- **Logging**: Assigns a UUID `Log-ID` to every HTTP request/response; automatic logging
- **Response Wrapper**: Automatically wraps controller return values in `CommonApiResponse`. Use `success()` / `created()` / `error()` factory methods
  - HTTP status set via `@ResponseStatus` or `ResponseEntity` is preserved (e.g. `@ResponseStatus(HttpStatus.CREATED)` → 201), and the body's `status`/`code`/`message` follow it
  - Returning `null` with the default 200 becomes 204; 204/205/304 responses have no body
  - `status` is serialized with the application's Jackson settings. Jackson 3 enables `EnumFeature.WRITE_ENUMS_USING_TO_STRING` by default, so it is `HttpStatus.toString()` — e.g. `"200 OK"`, `"201 CREATED"`, `"404 NOT_FOUND"`. Assert these values in tests
- **Exception Handler**: Throwing `ExpectedException(message, HttpStatus)` returns a standard error response automatically
- **Swagger**: Auto-configured at `/v3/api-docs` and `/swagger-ui`. Only `/v1/**` paths are documented
  - For an API that may return DTO or `null` (default 200), declare both `200` and body-less `204` in `@ApiResponses`
