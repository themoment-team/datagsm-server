---
description: DB migration and schema change guide
---

# DB Migration Guide

## Entity Change Checklist

- [ ] Analyzed impact on existing data
- [ ] Determined need for migration script
- [ ] Prepared test data
- [ ] Planned rollback strategy

## Change Order

1. **Modify Entity**: `datagsm-common/.../domain/`
2. **Modify DTO**: ReqDto, ResDto
3. **Modify Repository**: Adjust QueryDSL queries if needed
4. **Modify Service**: Update business logic
5. **Update Tests**: Entity, Service tests

## Important Notes

- Use JPA DDL auto as `validate` or `none`
- Production environment: Manual migration scripts
- Column deletion: 2-phase deployment (Phase 1: Deprecate, Phase 2: Delete)