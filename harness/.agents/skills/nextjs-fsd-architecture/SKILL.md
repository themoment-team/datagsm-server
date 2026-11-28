---
name: nextjs-fsd-architecture
description: Use when placing, structuring, or reviewing code in a Next.js App Router project that follows the official Feature-Sliced Design layout (`src/_app`, `src/_pages`), including error, not-found, and global-error boundaries, environment access and API-mocking entry points, and Turborepo workspaces that share FSD lower layers through `packages/core`.
---

# Next.js + Feature-Sliced Design Guide

This guide follows the official FSD guide for Next.js. Paths such as `src/`, `app/`, and `@/` are relative to a web app: the project root for a single app, or each `apps/<name>/` folder in a Turborepo. If the project's instructions or existing code use different names, follow the project and report the difference.

## Layout

```text
app/                 Next.js routes only (outside FSD; Steiger does not check it)
src/
├── _app/            app layer: providers, global styles, bootstrap config
├── _pages/          pages layer: one screen per route
├── widgets/         large self-contained blocks shared by screens
├── features/        one user action (a form or button and its mutation)
├── entities/        domain concepts (user, post) and how they are read
└── shared/          domain-agnostic code (api, config, lib, ui)
```

- Next.js treats `src/app` and `src/pages` as router folders, so the FSD `app` and `pages` layers carry a leading underscore. Steiger strips the underscore and recognizes them as layers.
- The root `app/` is for routing only. From the FSD point of view it sits above `_app` and composes everything.
- Import direction is `_app → _pages → widgets → features → entities → shared`. A layer imports only layers to its right, never another slice in the same layer, and never a slice's internal path. `_app` and `shared` have no slices, so their segments may import one another.

## Choosing a layer

Ask from the top and use the first layer that fits.

| Question                                                         | Layer      |
| ---------------------------------------------------------------- | ---------- |
| Does it apply once to the whole app? (providers, global CSS, global config) | `_app` |
| Is it the whole screen for one route?                            | `_pages`   |
| Is it a large block used by several screens, combining features? | `widgets`  |
| Is it one thing the user does? (edit profile, like, log out)     | `features` |
| Is it a domain concept and how it is shown or read?              | `entities` |
| Does it know nothing about the domain?                           | `shared`   |

- Starting inside `_pages` is fine. Move code down a layer when a second screen needs it; do not split in advance.
- Steiger's `fsd/insignificant-slice` reports a slice with a single consumer. Consider merging it into that consumer first.
- When a feature needs another feature, compose them in a widget or page.
- Do not put feature-specific code in `shared/`. Domain-agnostic settings such as environment validation go in `shared/config`; app-wide configuration (providers setup, bootstrap) goes in `_app/config`.

## Slices and segments

- Slice names are kebab-case domain names (`user`, `edit-profile`).
- Name segments by purpose, not by kind. Do not use `components`, `hooks`, `utils`, or `types` (Steiger `fsd/segments-by-purpose`).

| Segment   | Contents                                                          |
| --------- | ----------------------------------------------------------------- |
| `ui/`     | components (variants with `cva`)                                  |
| `model/`  | types, schemas, constants, state hooks                            |
| `api/`    | URL constants, request functions, query factories, mutation hooks |
| `lib/`    | utilities used only by this slice                                 |
| `config/` | configuration values and feature flags                            |

Create a segment only when it has code.

## Public API

- Each slice has an `index.ts` that exports, by name, only what other slices use. Code outside the slice imports only from it (Steiger `fsd/no-public-api-sidestep`).
- Server-only code (anything importing `server-only` or the server HTTP client) is exported from a separate `index.server.ts`. Mixing it into `index.ts` breaks the build of any client component that imports the slice.
- Add `import 'server-only';` to server-only implementations and to the server HTTP module, so a client import fails at build time instead of leaking server code.
- Inside a slice, use relative imports.
- Do not put one `index.ts` in `shared/ui` or `shared/lib` that re-exports everything. Give each component or library its own folder and `index.ts` (`shared/ui/button/index.ts`, imported as `@/shared/ui/button`). One large barrel makes the dev server compile every component for each import and mixes `'use client'` components with server components behind one entry point.

```ts
// src/entities/user/index.ts
export { userQueries } from './api/userQueries';
export { USER_URL } from './api/userUrl';
export type { User } from './model/types';
export { UserDetails } from './ui/UserDetails';

// src/entities/user/index.server.ts
export { getUserOnServer } from './api/getUserOnServer';
```

### Cross-entity references (`@x`)

Entities do not import each other. Only when one entity's type is unavoidable in another, the providing entity adds `@x/<consumer>.ts`.

```ts
// src/entities/user/@x/post.ts — public API only for the post entity
export type { User } from '../model/types';

// src/entities/post/model/types.ts
import type { User } from '@/entities/user/@x/post';
```

A growing number of `@x` files means the entity boundary is wrong: merge the entities or move the composition up a layer.

## Route files

Files under the root `app/` only do what Next.js requires.

| File                         | Responsibility                                                  |
| ---------------------------- | --------------------------------------------------------------- |
| `layout.tsx`                 | `<html>`/`<body>`, `metadata`, providers and global CSS import  |
| `page.tsx`                   | re-export the page component from `_pages`                      |
| `loading.tsx`                | render a component from `_pages` or `widgets`                   |
| `error.tsx`, `not-found.tsx` | re-export the error or 404 screen from `_pages`                 |
| `global-error.tsx`           | re-export the standalone root-layout error boundary from `_app` |
| `route.ts`                   | Route Handler                                                   |

```tsx
// app/page.tsx
export { HomePage as default } from '@/_pages/home';

// app/users/[id]/page.tsx — params are handled by the async page component in _pages
export { UserPage as default } from '@/_pages/user';
```

- `PageProps<'/route'>` and `LayoutProps` are global types generated by `next typegen` (also run by `next dev` and `next build`); they need no import. Make the type-check script `next typegen && tsc --noEmit` so it works on a clean checkout and in CI.
- Data prefetching and `HydrationBoundary` belong to the async Server Component in `src/_pages/<slice>/ui/`, not to the route file. The steps are in the `data-and-forms` skill.
- Keep data shaping, branching, state, event handlers, route guards, and styled markup out of route files. **Steiger does not check the root `app/`**, so review it by hand.
- Error and 404 UI lives in the pages layer; route files only re-export it. Error boundaries are Client Components: put `'use client'` in `error.tsx` and `global-error.tsx` re-export files and their boundary implementations. Show a safe user-facing message and the error digest when available, never `error.message` or a stack trace. Before wiring retry, inspect the installed Next.js version's bundled documentation (`node_modules/next/dist/docs/`) for the retry prop name and signature; if that folder is absent, read the installed version's type definitions instead. Do not assume a name from another version.
- `global-error.tsx` replaces the root layout. Implement its boundary in the app layer (for example `src/_app/global-error/`, re-exported by `app/global-error.tsx`), render its own `<html>` and `<body>`, and supply standalone styles; it must not depend on providers or CSS supplied by the root layout.

## Server and client

- Components are Server Components by default. Add `'use client'` only to the smallest component that needs state, events, or browser APIs (usually in a feature's `ui/`).
- Validate server environment variables in one schema module and read them only through it; do not read `process.env` elsewhere except for the narrow public build-time flag and runtime guard below. When adding a variable, update the schema, `.env.example`, and CI together.
- Only if the project already has a public build-time flag that turns API mocking on and off, allow direct `process.env.NEXT_PUBLIC_*` access in that mocking's entry modules only, using the project's actual flag name and a literal comparison so the bundler can inline it. Do not use computed keys or dynamic env lookup, and never use this exception for a flag that does not control mocking. Secrets never qualify for this exception.
- Keep this exception scoped to the project's mocking entry points: instrumentation combines the literal flag comparison with `process.env.NEXT_RUNTIME === 'nodejs'`; the mock provider combines it with `typeof window !== 'undefined'` before importing browser mocks. Inspect the project's `next.config` to preserve the flag's build-time value/default and disabled branch elimination; never expose secrets through its `env` option. Public values and rewrites are fixed at build time: changing runtime environment variables does not change them, so rebuild with the intended values. Discover the actual entry points, flag, and enabled value in the repository; do not add mocking to projects without it.
- Only in projects that already mock APIs with a service worker, hold browser API requests until the worker is ready; do not mount request-producing children or start their queries before worker startup resolves. Test that a browser API response came from the worker (for example, response provenance plus the expected mocked payload), not merely that page text appeared. Test server-side mocking separately. With the mocking switch off, confirm that no service worker is registered and that the production client JavaScript bundles contain no mocking code (for example by searching the emitted chunks for the worker file name or MSW markers). The worker script under `public/` is a static asset that is still copied into the build; it is harmless while unregistered, so do not report its presence as a violation.
- Read validated server environment variables only on the server. Never import the server env module from a client component.
- Browser requests use the client HTTP module; server requests use the server-only module exported from `index.server.ts`.

## Steiger

1. Enable the harness item in `.harness/sync.yml` to deploy `steiger.config.mjs` to the project root:

   ```yaml
   overrides:
     nextjs/fsd/steiger-config: true
   ```

2. Install `steiger` and `@feature-sliced/steiger-plugin` as development dependencies.
3. Add `"lint:fsd": "steiger ./src --fail-on-warnings"`. Warnings count as failures. In a Turborepo, install both packages and add the script in the root `package.json` instead, one Steiger run per source root (see [Turborepo with several apps](#turborepo-with-several-apps)).

The deployed config (`references/steiger.config.mjs`) adds three exceptions to the recommended rules. The `./apps/*/src` and `./packages/core/src` patterns apply only in a Turborepo and match nothing in a single app:

```js
// steiger.config.mjs
import fsd from '@feature-sliced/steiger-plugin';
import { defineConfig } from 'steiger';

// Steiger config for the official FSD layout in Next.js (`src/_app`, `src/_pages`).
// The same file works for a single app (`src/`) and for a Turborepo checked from the root (`apps/*/src`, `packages/core/src`).
export default defineConfig([
  ...fsd.configs.recommended,
  {
    // Underscored layer folders are recognized as layers, but the typo rule compares raw folder names.
    files: ['./src/_app/**', './src/_pages/**', './apps/*/src/_app/**', './apps/*/src/_pages/**'],
    rules: {
      'fsd/typo-in-layer-name': 'off',
    },
  },
  {
    // `providers` is the conventional segment name in the app layer.
    files: ['./src/_app/providers/**', './apps/*/src/_app/providers/**'],
    rules: {
      'fsd/segments-by-purpose': 'off',
    },
  },
  {
    // Apps consume core slices through package exports, which are outside core's source root,
    // so Steiger would report every promoted slice as unused.
    files: [
      './packages/core/src/entities/**',
      './packages/core/src/features/**',
      './packages/core/src/widgets/**',
    ],
    rules: {
      'fsd/insignificant-slice': 'off',
    },
  },
]);
```

Add another exception only for a real violation where restructuring would be worse, with the narrowest `files` glob and a comment explaining why.

| Rule                         | Meaning                                       | Usual fix                                |
| ---------------------------- | --------------------------------------------- | ---------------------------------------- |
| `fsd/forbidden-imports`      | imports an upper layer or a sibling slice     | compose one layer up, or use `@x`        |
| `fsd/no-public-api-sidestep` | imports a slice's internal path               | export from `index.ts` and import that   |
| `fsd/insignificant-slice`    | slice with a single consumer                  | merge into the consumer                  |
| `fsd/segments-by-purpose`    | segment named by kind (`hooks`, `components`) | rename by purpose (`model`, `lib`, `ui`) |

## Turborepo with several apps

```text
apps/<name>/src:     _app → _pages → widgets → features → entities → shared
packages/core/src:   widgets → features → entities → shared
```

- Each app owns its framework configuration (for example, `next.config`). Apply shared security-header and, where mocking already exists, mocking-switch policies to every app that needs them, through each app's configuration. During review, enumerate the applicable apps and check each configuration so no app is missed; a policy applied to one app does not cover the others.
- Apps never import each other. Sharing goes through `packages/*`, and a package is imported only through the entry points in its `package.json` `exports`.
- Keep a slice in the first app that uses it. Move it to the same layer in `packages/core/src` only when a second app needs the same data and behavior, and change imports from `@/<layer>/<slice>` to `@scope/core/<layer>/<slice>`. Core cannot import apps, so a core slice uses only core's lower layers: promote bottom-up (entity, then feature, then widget).
- Something that only looks the same across several web apps (a primitive such as a button, a layout frame, a shell with slots) has no domain, so it belongs to the Shared layer: core's `shared/ui`, one folder per component (`@scope/core/shared/ui/<component>`), with each app passing its content through props and children. Core's `entities`, `features`, and `widgets` are for slices whose data and behavior are identical. If the repository already has a separate UI package (for example a shadcn monorepo setup), keep using it instead of moving its components; do not create a new UI package only to share components between apps.
- `packages/core` has no root barrel. It is exposed in the same units as slice public APIs, so the import path keeps the layer visible:

  ```json
  {
    "exports": {
      "./shared/*": "./src/shared/*/index.ts",
      "./shared/*/server": "./src/shared/*/index.server.ts",
      "./entities/*": "./src/entities/*/index.ts",
      "./entities/*/server": "./src/entities/*/index.server.ts",
      "./features/*": "./src/features/*/index.ts",
      "./features/*/server": "./src/features/*/index.server.ts",
      "./widgets/*": "./src/widgets/*/index.ts",
      "./widgets/*/server": "./src/widgets/*/index.server.ts"
    }
  }
  ```

- New segments and slices need no `exports` change. Do not import paths inside a public file (`@scope/core/entities/user/model`); the wildcard also matches nested folders, so an `index.ts` in a subfolder becomes importable.
- An app and core share one layer system in two places. An app layer imports only core's **lower** layers. The same layer is a different slice and is forbidden too: an app feature cannot import a core feature (compose them in a widget or above), and an app widget cannot import a core widget (compose them in `_pages` or above).
- The one exception is an entity reference: a core entity publishes `entities/<slice>/@x/<consumer>/index.ts`, and the app entity imports `@scope/core/entities/<slice>/@x/<consumer>`. Use the folder form, not `@x/<consumer>.ts`: a package `exports` pattern takes one wildcard, so `./entities/*` resolves only to an `index.ts`.
- Steiger checks each source root separately and cannot see package imports. Block the cross-package violations it misses with ESLint in the apps' config, one block per app layer:

  ```js
  const core = (layer) => `^@scope/core/${layer}(?:/|$)`;
  const restrict = (files, patterns) => ({
    files,
    rules: { 'no-restricted-imports': ['error', { patterns }] },
  });

  export default [
    // ...
    restrict(['src/shared/**'], [
      { regex: core('(?:entities|features|widgets)'), message: 'shared cannot import upper layers.' },
    ]),
    restrict(['src/entities/**'], [
      {
        regex: '^@scope/core/entities(?!/[^/]+/@x/[^/]+$)(?:/|$)',
        message: 'Import a core entity only through its @x/<consumer> public API.',
      },
      { regex: core('(?:features|widgets)'), message: 'entities cannot import upper layers.' },
    ]),
    restrict(['src/features/**'], [
      { regex: core('(?:features|widgets)'), message: 'Compose core features in a widget or above.' },
    ]),
    restrict(['src/widgets/**'], [
      { regex: core('widgets'), message: 'Compose core widgets in _pages or above.' },
    ]),
  ];
  ```

- Run Steiger from the repository root so the single deployed `steiger.config.mjs` applies, once per source root: `steiger apps/client/src --fail-on-warnings`, one per app, and `steiger packages/core/src --fail-on-warnings`. Running `steiger ./src` inside a package does not pick up the root config. The config turns off `fsd/insignificant-slice` for core's `entities`, `features`, and `widgets`: the apps that consume those slices are outside core's source root, so every promoted slice looks unused.
- Packages do not read environment variables. Each app validates its own env and passes values (such as a base URL) into package factories.
- Each app's `@/shared/api` builds its HTTP methods from the core factory and re-exports them under the same names (`get`, `post`, `getQueryClient`, …), so slice code imports `@/shared/api` the same way as in a single app.
- Lint React code in packages with the same React, hooks, and accessibility rules as the apps, minus Next.js-only rules and app-only FSD rules. Include `.tsx` in the package `tsconfig.json` and set `"jsx": "react-jsx"` before adding components; otherwise ESLint cannot parse them. A package that holds components also needs the same DOM test environment as the apps (for example Vitest with jsdom and Testing Library); a Node-only test setup fails component tests with `document is not defined`.
- Put a source-consumed workspace package (one whose TypeScript the apps compile) in each app's `transpilePackages`. A prebuilt UI package that publishes JavaScript, CSS, and declarations is not transpiled again.
- Keep package, lint, and Tailwind configuration out of the application runtime bundle. Each app keeps its own `public/`, environment files, and deployment configuration.

## Adding a feature (example: edit profile)

1. If there is no screen yet, create `src/_pages/<slice>` and `app/<route>/page.tsx`.
2. Put domain data the screen reads in `src/entities/<slice>`: types, URL, query factory (`<slice>Queries`).
3. Put the user action in `src/features/<slice>`: schema (`model/`), mutation hook (`api/`), form (`ui/`).
4. Export from each slice's `index.ts`, and `index.server.ts` when needed.
5. Compose in `_pages` and keep the route file a re-export.
6. Run the project's FSD lint, type check, and related tests.
