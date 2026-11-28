---
name: nextjs-fsd-architecture
description: Use when implementing or reviewing a single Next.js App Router application that uses Feature-Sliced Design, including `views` instead of FSD `pages`.
---

# Next.js + Feature-Sliced Design Guide

Use this skill only in projects that use Next.js App Router and the FSD layout described below.

## Project Structure

```
src/
├── app/         # Next.js routes, layouts, metadata, providers, route guards
├── views/       # Page composition
├── widgets/     # Reusable page sections
├── features/    # User actions, forms, mutations, feature schemas
├── entities/    # Domain types, API functions, query hooks, entity UI
└── shared/      # Shared clients, hooks, stores, utilities, config, constants, assets
```

`views` replaces the standard FSD `pages` layer to avoid a collision with the Next.js Pages Router. The FSD `app` layer is intentionally combined with Next.js `src/app` so routes, providers, and global styles have one home.

## Slice Segments

Each business slice may contain:

- `ui/`: components
- `model/`: types, query/mutation hooks, schemas, constants
- `api/`: server fetch functions; expose them through `index.server.ts`
- `lib/`: slice-specific utilities
- `config/`: slice-specific configuration and feature flags

Every business slice must expose its external API through `index.ts`; expose server-only APIs through `index.server.ts`. Modules outside a slice may import only from those public entry points, except the explicit `entities/<slice>/@x/<consumer>` public API permitted for unavoidable entity relationships; never import its internal segments.
Do not put feature-specific code in `shared/`.
Keep global configuration in `shared/config` and application bootstrap configuration in `app/config`.

## Dependency Boundaries

```
app → views → widgets → features → entities → shared
```

- A layer may import only layers to its right.
- Do not import another business slice in the same layer, except an explicit `entities/<slice>/@x/<consumer>` public API for unavoidable entity relationships.
- `app` and `shared` are exceptions: their own segments may import one another because they are not split by business domain.

```ts
// Correct: a feature uses an entity and shared code
import { getUser } from "@/entities/user";
import { apiClient } from "@/shared/api";

// Incorrect: one feature imports another feature
import { LoginForm } from "@/features/login";
```

## Next.js Boundaries

- Keep routing, layouts, metadata, providers, and route guards in `src/app`.
- Compose page-level content in `views`; route files should remain thin.
- Keep server-only fetch functions in a slice's `api/` segment and import them through `index.server.ts`.
- Do not move business logic into route files merely because they are Server Components.

## Validation

For a single application rooted at `src/`:

1. Enable these harness items in `.harness/sync.yml` to deploy the files to the project root and `scripts/` directory:

```yaml
overrides:
  nextjs/fsd/steiger-config: true
  nextjs/fsd/dependency-check: true
```

2. Install `steiger` and `@feature-sliced/steiger-plugin` as development dependencies.
3. Add this package script:

```json
"lint:fsd": "node scripts/check-fsd-dependencies.mjs && steiger src --fail-on-warnings"
```

For a Turborepo, pass every app source root to the same checker. See the `nextjs-turborepo-fsd` skill for package boundaries and the complete command.

Steiger validates standard FSD rules. The bundled script additionally validates the non-standard `views` layer, same-layer slice imports, and dynamic `import()` calls.
