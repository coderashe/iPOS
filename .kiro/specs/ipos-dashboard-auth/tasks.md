# Implementation Plan: iPOS Dashboard Authentication & Store-Scoped Filtering

## Overview

Implement JWT-based authentication and store-scoped filtering across the Angular frontend and ASP.NET Core backend. Tasks flow from backend auth API → Angular auth layer → route protection → store-scoped filtering → wiring everything together.

## Tasks

- [x] 1. Backend: Auth data models and database access
  - [x] 1.1 Create `LoginRequest` and `LoginResponse` C# models in `Models/Auth/`
    - `LoginRequest`: `Email`, `Password`, `StoreId`
    - `LoginResponse`: `Token`
    - _Requirements: 3.1_
  - [x] 1.2 Add `User` entity and `Device` entity to EF Core context (or ADO.NET query layer) mapping to `iPosDb` `Users` and `Devices` tables
    - `User`: `Id`, `Email`, `Password`, `StoreId`, `UserRole`
    - `Device`: `DeviceId`, `StoreId`
    - _Requirements: 3.1, 4.2_

- [x] 2. Backend: JWT and auth services
  - [x] 2.1 Implement `JwtService` in `Services/JwtService.cs`
    - `GenerateToken(int userId, string storeId, string userRole)` → signed JWT
    - Claims: `sub`, `storeId`, `role`, `iat`, `exp` (iat + 28800s)
    - Read secret/issuer/audience from `appsettings.json`
    - _Requirements: 3.2, 3.4_
  - [ ]* 2.2 Write property test for `JwtService` — Property 6: JWT contains required claims
    - **Property 6: JWT contains required claims**
    - **Validates: Requirements 3.2**
    - Use FsCheck; generate random valid admin user records; decode token and assert all claims present and match inputs
  - [ ]* 2.3 Write property test for `JwtService` — Property 7: JWT expiry is exactly 8 hours
    - **Property 7: JWT expiry is exactly 8 hours**
    - **Validates: Requirements 3.4**
    - Assert `exp - iat == 28800` for any generated token
  - [x] 2.4 Implement `AuthService` in `Services/AuthService.cs`
    - `AuthenticateAsync(email, password, storeId)` → JWT string or `null`
    - Query `Users` by email, verify BCrypt/PBKDF2 password hash, check `UserRole == "admin"`, check `StoreId` matches
    - Return `null` for any failure (no field-level detail)
    - _Requirements: 3.1, 3.2, 3.3_
  - [ ]* 2.5 Write property test for `AuthService` — Property 3: Non-admin role is rejected
    - **Property 3: Non-admin role is rejected**
    - **Validates: Requirements 1.5, 3.3**
    - Use FsCheck; generate random non-`'admin'` role strings; assert `AuthenticateAsync` returns `null`

- [x] 3. Backend: AuthController and JWT middleware
  - [x] 3.1 Implement `AuthController` in `Controllers/AuthController.cs`
    - `POST /api/auth/login` → calls `IAuthService.AuthenticateAsync`, returns `200 { token }` or `401 { error: "Unauthorized" }`
    - _Requirements: 3.1, 3.2, 3.3_
  - [x] 3.2 Register JWT bearer authentication in `Program.cs` / `Startup.cs`
    - Configure `AddAuthentication().AddJwtBearer(...)` with matching issuer, audience, and signing key
    - Register `IJwtService`, `IAuthService` in DI
    - _Requirements: 3.1, 4.4_
  - [x] 3.3 Add `ClaimsPrincipalExtensions` in `Extensions/ClaimsPrincipalExtensions.cs`
    - `GetStoreId()`, `GetUserId()`, `GetUserRole()` extension methods
    - _Requirements: 4.2_

- [x] 4. Backend: Store-scoped filtering
  - [x] 4.1 Implement `StoreDeviceService` in `Services/StoreDeviceService.cs`
    - `GetDeviceIdsForStoreAsync(storeId)` → `IReadOnlyList<string>` of deviceIds from `Devices` table
    - _Requirements: 4.2_
  - [ ]* 4.2 Write property test for `StoreDeviceService` — Property 8: Analytics scoped to store's devices
    - **Property 8: Analytics results are scoped to the user's store's devices**
    - **Validates: Requirements 4.2, 4.3**
    - Use FsCheck; seed in-memory DB with multi-store device/invoice data; assert all returned records have `deviceId` in the set for the requesting store
  - [ ]* 4.3 Write property test for unauthenticated requests — Property 9: No JWT returns 401
    - **Property 9: Requests without a valid JWT return 401 with no data**
    - **Validates: Requirements 4.4**
    - Use FsCheck; for any analytics endpoint, send request without Authorization header; assert 401 and empty body
  - [x] 4.4 Update existing analytics controllers to inject `IStoreDeviceService` and apply device filter
    - At the start of each action: `var deviceIds = await _storeDeviceService.GetDeviceIdsForStoreAsync(User.GetStoreId());`
    - Add `[Authorize]` attribute to all analytics controllers
    - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 5. Checkpoint — Backend complete
  - Ensure all backend tests pass, ask the user if questions arise.

- [x] 6. Angular: Auth models and AuthService
  - [x] 6.1 Create `core/models/auth.models.ts` with `LoginRequest`, `LoginResponse`, `AuthUser` interfaces
    - _Requirements: 1.2, 1.3_
  - [x] 6.2 Implement `AuthService` in `core/services/auth.service.ts`
    - `login(req)`, `logout()`, `isAuthenticated()`, `getToken()`, `getUser()`, `getStoreId()`
    - Store JWT in `localStorage` under key `ipos_auth_token`
    - Decode JWT payload client-side; check `exp` vs `Date.now() / 1000`
    - `logout()` removes token and navigates to `/login`
    - _Requirements: 1.3, 1.5, 5.1_
  - [ ]* 6.3 Write property test for `AuthService` — Property 1: Valid login stores JWT
    - **Property 1: Valid login stores JWT**
    - **Validates: Requirements 1.3**
    - Use fast-check; generate random valid user objects; mock API to return token; assert `localStorage` has token and `isAuthenticated()` is true
  - [ ]* 6.4 Write property test for `AuthService` — Property 10: Logout clears JWT
    - **Property 10: Logout clears JWT**
    - **Validates: Requirements 5.1**
    - Use fast-check; for any stored token string, call `logout()`; assert `getToken()` is null and `isAuthenticated()` is false

- [x] 7. Angular: AuthGuard and AuthInterceptor
  - [x] 7.1 Implement `authGuard` functional guard in `core/guards/auth.guard.ts`
    - Redirect to `/login?returnUrl=<state.url>` when `isAuthenticated()` is false or token is expired
    - _Requirements: 2.1, 2.2_
  - [ ]* 7.2 Write property test for `authGuard` — Property 4: Unauthenticated/expired requests redirect
    - **Property 4: Unauthenticated and expired requests redirect to login**
    - **Validates: Requirements 2.1, 2.2**
    - Use fast-check; generate random protected route URLs; assert guard returns `UrlTree` to `/login` when no token or expired token
  - [x] 7.3 Implement `authInterceptor` functional interceptor in `core/interceptors/auth.interceptor.ts`
    - Attach `Authorization: Bearer <token>` to every outgoing request
    - On 401 response: call `AuthService.logout()`
    - _Requirements: 4.1_

- [x] 8. Angular: LoginComponent
  - [x] 8.1 Create `LoginComponent` in `features/auth/login/login.component.ts`
    - Standalone component with reactive form: `email`, `password`, `storeId` fields
    - On submit: call `AuthService.login()`; on success navigate to `returnUrl` or `/dashboard`
    - On 401: display generic "Invalid credentials" message (no field-level detail)
    - On non-admin 401: display "Access denied" message
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_
  - [ ]* 8.2 Write property test for `LoginComponent` — Property 2: Invalid credentials produce a generic error
    - **Property 2: Invalid credentials produce a generic error**
    - **Validates: Requirements 1.4**
    - Use fast-check; for any 401 response, assert displayed message does not reference a specific field
  - [ ]* 8.3 Write property test for `LoginComponent` — Property 5: Return URL is preserved through login
    - **Property 5: Return URL is preserved through login**
    - **Validates: Requirements 2.3**
    - Use fast-check; generate random protected route URLs; assert successful login navigates to that URL

- [x] 9. Angular: Route configuration
  - [x] 9.1 Update `app.routes.ts` to add `/login` route (no guard) and apply `authGuard` to all existing dashboard routes (`/dashboard`, `/customers`, `/inventory`, `/sales`, `/financial`)
    - Default `''` redirects to `/login`; wildcard `**` redirects to `/login`
    - _Requirements: 1.1, 2.1_
  - [x] 9.2 Register `authInterceptor` in `app.config.ts` via `provideHttpClient(withInterceptors([authInterceptor]))`
    - _Requirements: 4.1_

- [x] 10. Final checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for a faster MVP
- Property tests use fast-check (Angular/TypeScript) and FsCheck (C#/xUnit)
- Each property test tag format: `// Feature: ipos-dashboard-auth, Property {N}: {text}`
- Backend 401 responses never expose field-level detail to prevent user enumeration
