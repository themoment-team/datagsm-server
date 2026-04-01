---
name: security-checklist
description: Verify security vulnerabilities — hardcoded secrets, SQL injection, JWT validation, API key masking, sensitive logging, and authorization checks. Run before merging any auth or API-related changes.
---

# Security Checklist

## Verification Items

### 1. Hardcoded Secrets
- [ ] No API Key, Secret, Password in code?
- [ ] Using environment variables or config files?

Verification commands:
```bash
# Basic search in Kotlin files
grep -r "password.*=.*\"" --include="*.kt"
grep -r "secret.*=.*\"" --include="*.kt"
grep -r "apiKey.*=.*\"" --include="*.kt"

# Check YAML/Properties files
grep -r "password\|secret\|apiKey" --include="*.yml" --include="*.yaml" --include="*.properties"
```

### 2. SQL Injection
- [ ] Using PreparedStatement or JPA/QueryDSL?
- [ ] Not concatenating SQL strings directly?

### 3. JWT Verification
- [ ] Verifying JWT signature?
- [ ] Checking expiration time?
- [ ] Validating claims?

### 4. API Key Security
- [ ] Masking API Key in responses?
- [ ] Encrypting API Key when storing?

### 5. Logging
- [ ] Not logging sensitive info (password, token, etc.)?
- [ ] Appropriate log level?

### 6. Authorization
- [ ] Using `@PreAuthorize` or Security Filter for auth-required endpoints?
- [ ] Verifying access to own resources only?

## References

- `datagsm-oauth-authorization/.../auth/service/ApiKeyService.kt` - API Key security example
- `datagsm-common/.../global/common/security/` - Security configuration
