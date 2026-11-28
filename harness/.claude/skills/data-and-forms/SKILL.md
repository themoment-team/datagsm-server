---
name: data-and-forms
description: Use when reading or changing server data with TanStack Query, or building forms with Zod and React Hook Form, in a Next.js FSD project — query factories with `queryOptions`, mutation hook names, RSC prefetch with HydrationBoundary, invalidation scope, schema naming, mutation submit, and mapping API errors to form errors.
---

# Data and Forms Guide

Paths such as `src/`, `app/`, and `@/` are relative to a web app: the project root for a single app, or each `apps/<name>/` folder in a Turborepo. The examples use `user`, `edit-profile`, and `/users/:id` as illustrations; they are not existing code. Where the project already has different names for its HTTP wrappers, error class, or query client helper, use the project's names.

## Where things go

| What                             | Where                                                 |
| -------------------------------- | ----------------------------------------------------- |
| response types                   | owning entity `model/`                                |
| URL constants, query factory     | owning entity `api/`                                  |
| server-only read functions       | owning entity `api/`, exported from `index.server.ts` |
| mutation hook for a user action  | the feature that owns the action, `api/`              |
| form schema and request type     | feature `model/`                                      |
| form component                   | feature `ui/`                                         |
| prefetch and `HydrationBoundary` | the page component in `src/_pages/<slice>/ui/`        |

If there is no entity yet and only one feature uses the data, keep it in that feature. Move it to an entity when a second consumer appears.

## HTTP calls

- Before writing calls, inspect the project's HTTP implementation for its real error contract (status, code, message, and the request ID header when the contract includes one) and 401 policy. Find the actual field, header, class, and module names; do not assume the examples match. Where the project refreshes on 401, confirm its existing single-flight coordination (at most one refresh in flight, waiting requests share it) and keep using it; if the project has no coordination mechanism, report it as unverified instead of inventing one. Preserve the project's retry and failure behavior.
- Call the project's typed method wrappers (`get`, `post`, `patch`, `put`, `del`) that return the response body. Do not call the raw Axios instance for ordinary requests; it returns `AxiosResponse`.
- Pass the response type first and the body type second: `post<User, UpdateProfileRequest>(url, body)`.
- Browser code uses the client module (for example `@/shared/api`); server code uses the server-only module (for example `@/shared/api/index.server`).
- Follow the backend's real paths in URL constants. Do not add `/api` or version prefixes without evidence.
- The server HTTP module is shared by every request on the server. Do not keep per-user state (tokens, user IDs) in it or register per-user interceptors on it; pass per-request values such as cookies explicitly.
- Requests made inside the interceptor's 401 handling (such as a token refresh) go through a separate instance without that interceptor. Sent through the same instance, a 401 on the refresh waits on itself and never settles.

## Query factory

Keep one `<resource>Queries` object per resource that holds both query keys and read options. Entries that are actually fetched are built with TanStack Query's `queryOptions`; parent keys used only as invalidation scopes return plain arrays. Every key is a hierarchical array that starts from `all()`.

```ts
// src/entities/user/api/userQueries.ts
import { queryOptions } from '@tanstack/react-query';

import { get } from '@/shared/api';

import type { User, UserListParams } from '../model/types';
import { USER_URL } from './userUrl';

export const userQueries = {
  all: () => ['users'] as const,
  lists: () => [...userQueries.all(), 'list'] as const,
  list: (params: UserListParams) =>
    queryOptions({
      queryKey: [...userQueries.lists(), params],
      queryFn: () => get<User[]>(USER_URL.list, { params }),
    }),
  details: () => [...userQueries.all(), 'detail'] as const,
  detail: (id: string) =>
    queryOptions({
      queryKey: [...userQueries.details(), id],
      queryFn: () => get<User>(USER_URL.detail(id)),
    }),
};
```

- Never write a key as a literal array at the call site; always go through the factory.
- Put filters and pagination in the last element as an object.
- `queryOptions` tags the key with the data type, so `getQueryData(userQueries.detail(id).queryKey)` infers `User | undefined` without a generic.

## Reading data

Components pass the factory options straight to TanStack hooks. Do not wrap them in one-line read hooks (`useUser = (id) => useQuery(...)`): the same options are meant to be reused by `useQuery`, `useSuspenseQuery`, `useQueries`, `prefetchQuery`, `ensureQueryData`, and `getQueryData`.

```tsx
// src/entities/user/ui/UserDetails.tsx
'use client';

import { useQuery } from '@tanstack/react-query';

import { userQueries } from '../api/userQueries';

export const UserDetails = ({ userId }: { userId: string }) => {
  const { data: user } = useQuery(userQueries.detail(userId));

  return <p>{user?.nickname}</p>;
};
```

- Add per-screen options by spreading: `useQuery({ ...userQueries.detail(id), select: (user) => user.nickname, enabled: Boolean(id) })`.
- Write a custom read hook only when it carries logic: combining queries, a derived value that always comes with the data, or wiring to other hooks such as the router or auth state. Use the factory options inside it.
- To fetch ahead without rendering the data (for example on link hover), call `queryClient.prefetchQuery(userQueries.detail(id))` from the event handler instead of mounting `useQuery`.

## RSC prefetch and HydrationBoundary

Fetch first-screen data on the server and put it in the client cache.

1. Export a server-only read function from the entity's `index.server.ts`. It uses the server-only HTTP module, which carries the server's base URL and must never reach the client bundle.

   ```ts
   // src/entities/user/api/getUserOnServer.ts
   import { get } from '@/shared/api/index.server';

   import type { User } from '../model/types';
   import { USER_URL } from './userUrl';

   export const getUserOnServer = (id: string) => get<User>(USER_URL.detail(id));
   ```

2. In the async Server Component in `src/_pages/<slice>/ui/`, create a QueryClient with the project's `getQueryClient()`, prefetch by **spreading the factory options and replacing only `queryFn` with the server function**, and wrap the client UI in `HydrationBoundary`. The key and the other options must stay identical to the client's so the client reads the hydrated cache. The route file only re-exports the page.

   ```tsx
   // src/_pages/user/ui/UserPage.tsx
   import { dehydrate, HydrationBoundary } from '@tanstack/react-query';

   import { UserDetails, userQueries } from '@/entities/user';
   import { getUserOnServer } from '@/entities/user/index.server';
   import { getQueryClient } from '@/shared/api';

   export const UserPage = async ({ params }: PageProps<'/users/[id]'>) => {
     const { id } = await params;
     const queryClient = getQueryClient();

     await queryClient.prefetchQuery({
       ...userQueries.detail(id),
       queryFn: () => getUserOnServer(id),
     });

     return (
       <HydrationBoundary state={dehydrate(queryClient)}>
         <UserDetails userId={id} />
       </HydrationBoundary>
     );
   };

   // src/_pages/user/index.ts
   export { UserPage } from './ui/UserPage';

   // app/users/[id]/page.tsx
   export { UserPage as default } from '@/_pages/user';
   ```

3. Client components keep using `useQuery(userQueries.detail(id))`. While the cached data is within `staleTime`, it is not requested again. Set the QueryClient's default `staleTime` above 0; with the default of 0 the client refetches right after hydration and the prefetch is wasted.

- `await` the prefetch unless the project's QueryClient is configured to dehydrate pending queries.
- `getQueryClient()` returns a new instance per call on the server. Call it once per page component and use that instance for both prefetch and dehydrate. On the browser it returns a singleton.
- `prefetchQuery` does not throw. On failure the client hook requests again and exposes the error through `error`.
- Why `queryFn` is replaced: browser and server use different transports and authentication contexts (for example, a relative proxy path in the browser versus an absolute base URL and per-request cookies on the server). Use the project's actual transport configuration. A Server Component cannot write refreshed cookies, so the server prefetch must not refresh tokens; its HTTP instance may still share error normalization with the browser instance. A server prefetch that fails with 401 is not dehydrated, so the client retries through the browser instance and its 401 handling.
- `getQueryClient` is shared by server and client code, so the client API module must not import `server-only` code or server environment variables.

## Mutations and invalidation scope

Mutations carry logic (invalidation, navigation after success), so keep them in custom hooks. Name them by the action, not the HTTP method: `use<Action><Target>` (`useUpdateProfile`, `useCreatePost`, `useDeletePost`, `useFollowUser`). The name survives the backend switching `PATCH` to `PUT`.

A successful mutation invalidates **the narrowest key that changed**.

```ts
// src/features/edit-profile/api/useUpdateProfile.ts
import { useMutation, useQueryClient } from '@tanstack/react-query';

import { type User, USER_URL, userQueries } from '@/entities/user';
import { patch } from '@/shared/api';

import type { EditProfileRequest } from '../model/editProfileSchema';

export const useUpdateProfile = (userId: string) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (body: EditProfileRequest) =>
      patch<User, EditProfileRequest>(USER_URL.detail(userId), body),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: userQueries.detail(userId).queryKey }),
  });
};
```

| Change                     | Invalidate                                                     |
| -------------------------- | -------------------------------------------------------------- |
| one field of one item      | `detail(id).queryKey`, plus `lists()` if lists show that field |
| create or delete           | `lists()`                                                      |
| affects the whole resource | `all()`                                                        |

- Return the `invalidateQueries` promise from `onSuccess` so the mutation stays pending until the refetch finishes.
- When the response already contains the new data for a single key, `setQueryData(userQueries.detail(id).queryKey, user)` is fine; the key checks the data type.
- Do not call `queryClient.invalidateQueries()` without a key unless you can justify it.

## Forms

The Zod schema defines both validation and types.

```ts
// src/features/edit-profile/model/editProfileSchema.ts
import { z } from 'zod';

export const EditProfileSchema = z.object({
  nickname: z.string().trim().min(2, '닉네임은 2자 이상이어야 합니다.').max(20),
});

export type EditProfileRequest = z.infer<typeof EditProfileSchema>;
```

- Name schemas `<Action/Resource>Schema` and inferred request types `<Action/Resource>Request`. Do not rewrite a type the schema already defines.
- Write error messages in the schema, in the language users see.
- Prefer literal unions and `Record<Union, Metadata>` constants over `enum`.

Connect the schema with `zodResolver` and submit through the mutation.

```tsx
// src/features/edit-profile/ui/EditProfileForm.tsx
'use client';

import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';

import { ApiError } from '@/shared/api';

import { useUpdateProfile } from '../api/useUpdateProfile';
import { type EditProfileRequest, EditProfileSchema } from '../model/editProfileSchema';

interface EditProfileFormProps {
  userId: string;
  defaultValues: EditProfileRequest;
}

// Map server error codes to fields. Unknown codes become a form-level error.
const FIELD_BY_ERROR_CODE: Partial<Record<string, keyof EditProfileRequest>> = {
  NICKNAME_TAKEN: 'nickname',
};

export const EditProfileForm = ({ userId, defaultValues }: EditProfileFormProps) => {
  const { mutateAsync, isPending } = useUpdateProfile(userId);
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(EditProfileSchema),
    defaultValues,
  });

  const onSubmit = async (values: EditProfileRequest) => {
    try {
      await mutateAsync(values);
    } catch (error) {
      if (!(error instanceof ApiError)) {
        throw error;
      }
      const field = FIELD_BY_ERROR_CODE[error.code];
      setError(field ?? 'root.server', { message: error.message });
    }
  };

  return (
    <form onSubmit={(event) => void handleSubmit(onSubmit)(event)}>
      <label htmlFor="nickname">닉네임</label>
      <input id="nickname" {...register('nickname')} aria-invalid={Boolean(errors.nickname)} />
      {errors.nickname && <p role="alert">{errors.nickname.message}</p>}
      {errors.root?.server && <p role="alert">{errors.root.server.message}</p>}
      <button type="submit" disabled={isPending}>
        저장
      </button>
    </form>
  );
};
```

- Do not pass generics to `useForm`; `zodResolver(Schema)` infers input and output types.
- `handleSubmit(onSubmit)` returns a promise, which trips ESLint `no-misused-promises`. Wrap it with `void` as above.
- Rethrow errors that are not the project's API error class; never swallow them.
- If the API error carries only a code, keep a `code → field` map next to the form. If the API returns per-field errors, agree with the user before widening the error class.
- Leave 401 handling to the HTTP interceptor; forms do not redirect to login themselves.
- Accessibility: connect a `label` to every input and announce errors with `aria-invalid` and `role="alert"`.

## Tests

- Query factory keys and schemas are pure; unit-test boundary values (empty string, max length).
- Test form components with Testing Library by typing and submitting, and replace the mutation with a module mock. Do not depend on the network.
