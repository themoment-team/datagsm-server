---
name: database-schema
description: Database schema design guide — table naming, column conventions, index strategy, and JPA entity mapping patterns.
allowed-tools: AskUserQuestion
---

# Database Schema Design Guide

Before providing schema guidance, ask the user about their migration tooling:

```
AskUserQuestion: "DB 마이그레이션 도구로 무엇을 사용하고 있나요?"
options:
  - Flyway
  - Liquibase
  - 사용하지 않음 (JPA DDL auto)
```

Then provide the relevant migration section below along with the core conventions.

---

## Naming Conventions

- Tables: `snake_case`, plural (`users`, `api_keys`)
- Columns: `snake_case` (`created_at`, `is_active`)
- FK columns: `{referenced_table_singular}_id` (`user_id`, `club_id`)
- Index names: `idx_{table}_{columns}` (`idx_users_email`)
- UK names: `uq_{table}_{columns}` (`uq_users_email`)

## Standard Columns

Include in every entity table:

```sql
id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
```

## Index Strategy

- Single column used frequently in WHERE: simple index
- WHERE + ORDER BY combination: composite index (WHERE column first)
- Low-cardinality columns (`is_active`, `status` enum): indexing rarely helps

```sql
-- Composite index example
CREATE INDEX idx_posts_user_created ON posts (user_id, created_at DESC);
```

## Migration — Flyway

_(Include this section if the user selected Flyway)_

File naming: `V{version}__{description}.sql`

```
db/migration/
  V1__create_users.sql
  V2__add_api_keys.sql
  V3__add_index_users_email.sql
```

```sql
-- V2__add_api_keys.sql
CREATE TABLE api_keys (
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT      NOT NULL,
    key_value  VARCHAR(64) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_api_keys_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_api_keys_value UNIQUE (key_value)
);
```

## Migration — Liquibase

_(Include this section if the user selected Liquibase)_

File naming: `db/changelog/{version}-{description}.yaml`

```yaml
# db/changelog/002-add-api-keys.yaml
databaseChangeLog:
  - changeSet:
      id: 002-add-api-keys
      author: dev
      changes:
        - createTable:
            tableName: api_keys
            columns:
              - column:
                  name: id
                  type: BIGINT
                  autoIncrement: true
                  constraints:
                    primaryKey: true
              - column:
                  name: user_id
                  type: BIGINT
                  constraints:
                    nullable: false
              - column:
                  name: key_value
                  type: VARCHAR(64)
                  constraints:
                    nullable: false
                    unique: true
```

## JPA Entity Mapping

```kotlin
@Entity
@Table(name = "api_keys")
class ApiKey(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "key_value", nullable = false, unique = true, length = 64)
    val keyValue: String,
) : BaseEntity()
```