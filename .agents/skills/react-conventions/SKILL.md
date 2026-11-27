---
name: react-conventions
description: Use when writing or reviewing React and TypeScript code in a Next.js project — file and type naming, interface vs type, enum replacement, component shape and export style, ordering inside a component, client boundaries, and import order.
---

# React + TypeScript Conventions

These rules apply to `.ts` and `.tsx` files in a Next.js project. If the project's instructions or lint configuration say otherwise, follow the project and report the difference. Treat rules marked **(lint)** as machine-checked only when the target repository's configuration (ESLint, Prettier, or another configured tool) enables that rule for the reviewed files and the relevant command ran. Otherwise review them by hand and explicitly report that they were not machine-checked; a green lint run does not prove coverage for unconfigured rules.

## Naming

| What                    | Rule                                                    | Example                                   |
| ----------------------- | ------------------------------------------------------- | ----------------------------------------- |
| slice folder            | kebab-case                                              | `user-profile/`                           |
| component file          | PascalCase (shadcn primitives keep upstream kebab-case) | `ui/HomePage.tsx`, `shared/ui/button.tsx` |
| hook, util, other files | camelCase                                               | `getQueryClient.ts`, `cn.ts`              |
| props type              | PascalCase + `Props`                                    | `HomePageProps`                           |
| other types             | PascalCase, no kind suffix                              | `User`, `UserResponse`                    |
| request type            | PascalCase + `Request`                                  | `UpdateProfileRequest`                    |
| Zod schema              | PascalCase + `Schema`                                   | `EnvSchema`, `UpdateProfileSchema`        |

## Types

- Use `interface` for object shapes and `type` for unions and combinations.
- Do not use `enum`. Use a literal union with an `as const` object or a `Record`. **(lint)**
- Import types with `import type`. **(lint)**
- Do not add kind suffixes such as `Type` to type names. Add a role word only when a value (class or constant) already uses the name: the constructor input of the `ApiError` class is `ApiErrorInit`.

## Components

- Write components as arrow functions and destructure props.
- Modules under `src` use named exports only, and the owning slice's `index.ts` re-exports them by the same name. Default exports belong only to the files Next.js requires them in (`app/` route files and config files), which re-export the slice component as `default`:

  ```tsx
  // src/_pages/home/ui/HomePage.tsx
  interface HomePageProps {
    title: string;
  }

  export const HomePage = ({ title }: HomePageProps) => <h1>{title}</h1>;

  // src/_pages/home/index.ts
  export { HomePage } from './ui/HomePage';

  // app/page.tsx
  export { HomePage as default } from '@/_pages/home';
  ```

- To enforce this, add an ESLint `no-restricted-syntax` entry for `ExportDefaultDeclaration` and `ExportSpecifier[exported.name='default']` scoped to `src/**`. When that override also sets `no-restricted-syntax`, repeat any existing entries (such as the `enum` ban) because a later block replaces the rule's options.

- Order inside a component: variables and hooks → handlers and other logic → `useEffect` (only when needed) → `return`.
- Do not add state or effects just to follow this order. When React Compiler is enabled, do not add memoization by hand.
- Accessibility: connect a label to every input, use the right element for buttons and links, and give images `alt`.

## Server and client

- Components are Server Components by default. Add `'use client'` only to the smallest component that needs state, events, or browser APIs.
- Client components do not read server environment variables or import server-only modules.

## Imports

- Order: `react` → `next` → external packages → `@/` → relative paths. **(lint)**
- Outside a slice, import only from its public `index.ts` (or `index.server.ts`).

Run the project's lint, type check, and formatter after changes. The `frontend-convention-validator` agent reviews the rules that tools do not check.
