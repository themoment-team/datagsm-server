---
name: tanstack-query-zod
description: Use when implementing client data fetching, mutations, query keys, or form validation with TanStack Query, React Hook Form, and Zod.
---

# TanStack Query + Zod Guide

## API and Queries

- Call the project's typed HTTP method wrappers, not the raw Axios instance.
- Keep URL factories in the owning entity or feature `api/` segment.
- Name hooks `useGet<Resource>`, `usePost<Resource>`, `usePatch<Resource>`, `usePut<Resource>`, or `useDelete<Resource>`.
- Build query keys hierarchically with an `all()` root so mutations can invalidate the narrowest applicable key.

```ts
export const jobQueryKeys = {
  all: () => ["jobs"] as const,
  getJobs: () => ["jobs", "list"] as const,
  getJob: (jobId?: number) => ["jobs", "detail", jobId] as const,
} as const;
```

## Zod Types

- Name schemas in PascalCase with the `Schema` suffix.
- Infer request types from schemas with the `ReqType` suffix.
- Prefer explicit unions and `Record<Union, Metadata>` constants over `enum`.

```ts
export const JobRegistrationSchema = z.object({
  title: z.string().trim().min(1, "제목을 입력해주세요"),
});

export type JobRegistrationReqType = z.infer<typeof JobRegistrationSchema>;
```
