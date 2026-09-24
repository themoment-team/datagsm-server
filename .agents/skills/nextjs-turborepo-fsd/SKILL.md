---
name: nextjs-turborepo-fsd
description: Use when implementing or reviewing a Turborepo with multiple Next.js App Router applications and shared FSD packages.
---

# Next.js Turborepo + FSD Guide

Use `apps/*/src` for app-specific upper layers and `packages/core/src` only for shared lower layers.

## Package Boundaries

```
apps/*/src: app → views → widgets → features → entities → shared
packages/core/src: entities → shared
packages/ui: shared/ui design system
```

- Never import one app from another app.
- Apps may import shared packages only through their public `@scope/core` and `@scope/ui` entry points.
- Keep an entity in an app until at least two apps use it; then promote it to `packages/core/entities`.
- Keep shadcn primitives in `packages/ui`; keep domain UI in the owning entity or feature slice.

## Build Boundaries

- Source-consumed packages that export Server Component directives need each app's `transpilePackages` entry.
- Prebuilt UI packages publish JavaScript, CSS, and declarations; do not add them to `transpilePackages`.
- Keep server-only exports in `index.server.ts` and expose them through a dedicated package subpath.

## FSD Validation

Enable the shared FSD files in `.harness/sync.yml`:

```yaml
overrides:
  nextjs/fsd/steiger-config: true
  nextjs/fsd/dependency-check: true
```

Use this root script, replacing package names with the workspace's own names:

```json
"lint:fsd": "node scripts/check-fsd-dependencies.mjs apps/client/src apps/admin/src && steiger apps/client/src --fail-on-warnings && steiger apps/admin/src --fail-on-warnings && steiger packages/core/src --fail-on-warnings"
```
