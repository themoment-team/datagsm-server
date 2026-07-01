---
name: the-sdk
description: Usage guide for the-sdk common library — HTTP request/response logging with UUID Log-ID, CommonApiResponse wrapping, ExpectedException handling, and Swagger auto-configuration.
---

# the-sdk Usage Guide

`com.github.themoment-team:the-sdk:1.5` — the core common library for this project.  
Controlled via `sdk.*` settings in `application.yml`.

## Features

- **Logging**: Assigns a UUID `Log-ID` to every HTTP request/response; automatic logging
- **Response Wrapper**: Automatically wraps controller return values in `CommonApiResponse`. Use `success()` / `created()` / `error()` factory methods
- **Exception Handler**: Throwing `ExpectedException(message, HttpStatus)` returns a standard error response automatically
- **Swagger**: Auto-configured at `/v3/api-docs` and `/swagger-ui`. Only `/v1/**` paths are documented
