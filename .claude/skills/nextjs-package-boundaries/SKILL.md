---
name: nextjs-package-boundaries
description: Use when structuring a Next.js workspace package, especially when separating server-only modules, source-consumed packages, and prebuilt UI packages.
---

# Next.js Package Boundaries Guide

- Expose package APIs through narrow barrel files; keep server-only APIs in `index.server.ts` and a dedicated export subpath.
- Do not export `server-only` modules from a client-safe barrel.
- Use `transpilePackages` only for source-consumed workspace packages that need Next.js to preserve RSC directives.
- Prebuilt UI packages should publish JavaScript, CSS, and declarations; do not transpile them again in each app.
- Keep package configuration, lint configuration, and Tailwind configuration out of the application runtime bundle.
- Use each app's own `public/`, environment files, deployment configuration, and route boundary.
