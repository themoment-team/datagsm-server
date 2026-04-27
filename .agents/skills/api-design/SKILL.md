---
name: api-design
description: REST API design guide for new endpoints — RESTful URL structure, query parameter binding rules (@RequestParam vs @ModelAttribute), OpenAPI annotations, and CommonApiResponse usage.
---

# REST API Design Guide

## URL Design

- RESTful principles: `/v1/auth/api-keys`
- Use plural: `/students`, `/clubs`
- Hierarchy: `/students/{id}/projects`

## Query Parameters

- Filtering: `?status=active`
- Pagination: `?page=0&size=20`
- Sorting: `?sort=createdAt,desc`

## OpenAPI Documentation

```kotlin
@Operation(summary = "Create API key", description = "...")
@ApiResponse(responseCode = "200", description = "Success")
@PostMapping("/api-keys")
fun create(@Valid @RequestBody reqDto: CreateApiKeyReqDto): ApiKeyResDto
```

## Response Format

- Success: `CommonApiResponse(data = ...)`
- Error: `ExpectedException` → Global Handler
