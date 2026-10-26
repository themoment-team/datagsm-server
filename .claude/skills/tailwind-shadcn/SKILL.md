---
name: tailwind-shadcn
description: Use when building or reviewing Tailwind CSS and shadcn/Base UI components in a Next.js application or shared UI package.
---

# Tailwind + shadcn Guide

## Class Names

- Use a plain string or template literal for static classes.
- Use `cn()` only for conditional classes or when merging an external `className` prop.
- Keep related classes in one string. Extract repeated class sets to `ui/styles.ts` in the owning slice.

```tsx
<div className="flex items-center gap-2" />
<div className={cn("flex gap-2", isActive && "bg-primary")} />
<Button className={cn("rounded-lg px-4", className)} />
```

## Ownership

- Keep design tokens, `@theme`, and base layers in the shared Tailwind configuration.
- Keep app fonts in the app root; do not set the font again in feature components.
- Keep shadcn primitives in the UI package with their upstream kebab-case filenames and named exports.
- Keep domain components such as cards and forms in their FSD slice, not in the UI package.

Run the project's formatter and lint command after modifying classes or UI components.
