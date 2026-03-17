---
feature: iPOS Dashboard Authentication & Store-Scoped Filtering
type: feature
status: design
created: 2025-01-01
---

# Technical Design: iPOS Dashboard Authentication & Store-Scoped Filtering

## Overview

This feature adds JWT-based authentication and store-scoped data filtering to the existing iPOS Analytics Dashboard. The Angular frontend gains a login page, route guards, and an HTTP interceptor. The ASP.NET Core backend gains an `/api/auth/login` endpoint that validates credentials from `iPosDb`, issues a signed JWT, and enforces store-scoped filtering on all analytics queries.

Only users with `userRole = 'admin'` may access the dashboard. All data returned by the API is filtered to the `deviceIds` belonging to the authenticated user's store.

---

## Architecture

```
┌──────────────────────────────────────────────────────┐
│                  Angular Dashboard                    │
│                                                      │
│  /login → LoginComponent                             │
│  /dashboard, /sales, ... → [AuthGuard] → Features   │
│                                                      │
│  AuthService  ←→  localStorage (JWT)                 │
│  AuthInterceptor  →  adds Bearer token to requests  │
└────────────────────────┬─────────────────────────────┘
                         │ HTTP (Bearer JWT)
                         ▼
┌──────────────────────────────────────────────────────┐
│               ASP.NET Core API                       │
│                                                      │
│  POST /api/auth/login  →  AuthController             │
│  GET  /api/dashboard/*, /api/reports/*, ...          │
│       → [Authorize] → StoreScope middleware          │
│       → resolves deviceIds for storeId in JWT        │
└────────────────────────┬─────────────────────────────┘
                         │ EF Core / ADO.NET
                         ▼
┌──────────────────────────────────────────────────────┐
│                  SQL Server (iPosDb)                 │
│                                                      │
│  Users  →  validate credentials, check userRole     │
│  Stores / Devices  →  resolve deviceIds by storeId  │
│  InvoiceLines, etc.  →  filtered by deviceId        │
└──────────────────────────────────────────────────────┘
```

### Auth Flow (sequence)

```
Browser          Angular              API              DB
  │──── GET /dashboard ──▶│            │               │
  │                       │ AuthGuard: no JWT          │
  │◀── redirect /login ───│            │               │
  │──── POST /login ──────────────────▶│               │
  │                       │            │── SELECT Users│
  │                       │            │◀─ row ────────│
  │                       │            │── validate pw │
  │                       │            │── check role  │
  │◀── 200 { token } ─────────────────│               │
  │                       │ store JWT  │               │
  │──── GET /dashboard ──▶│            │               │
  │                       │ AuthGuard: valid JWT       │
  │                       │──── GET /api/dashboard ──▶│
  │                       │    (Authorization: Bearer) │
  │                       │            │── resolve     │
  │                       │            │   deviceIds   │
  │                       │            │── filter query│
  │◀── dashboard data ────────────────│               │
```

---

## Components and Interfaces

### Angular

#### `LoginComponent` (`features/auth/login/login.component.ts`)
- Standalone component, no auth guard
- Reactive form with `email`, `password`, `storeId` controls
- Calls `AuthService.login()`, handles 401 with generic error message
- On success: navigates to the preserved return URL or `/dashboard`

#### `AuthService` (`core/services/auth.service.ts`)
```typescript
interface LoginRequest {
  email: string;
  password: string;
  storeId: string;
}

interface LoginResponse {
  token: string;
}

interface AuthUser {
  userId: string;
  storeId: string;
  userRole: string;
  exp: number;
}

@Injectable({ providedIn: 'root' })
class AuthService {
  login(req: LoginRequest): Observable<void>
  logout(): void
  isAuthenticated(): boolean        // checks token exists + not expired
  getToken(): string | null
  getUser(): AuthUser | null        // decoded JWT payload
  getStoreId(): string | null
}
```
- Stores JWT in `localStorage` under key `ipos_auth_token`
- Decodes JWT payload client-side (no signature verification needed in browser)
- Expiry check: compare `exp` claim (Unix seconds) against `Date.now() / 1000`

#### `AuthGuard` (`core/guards/auth.guard.ts`)
```typescript
// Functional guard (Angular 15+ style)
export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isAuthenticated()) return true;
  return router.createUrlTree(['/login'], {
    queryParams: { returnUrl: state.url }
  });
};
```
- Preserves `returnUrl` as query param; `LoginComponent` reads it on successful login

#### `AuthInterceptor` (`core/interceptors/auth.interceptor.ts`)
```typescript
// Functional interceptor (Angular 15+ style)
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthService).getToken();
  if (token) {
    req = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) inject(AuthService).logout();
      return throwError(() => err);
    })
  );
};
```
- Attaches Bearer token to every outgoing request
- On 401 response: clears session and redirects to login

#### Route Configuration (updated `app.routes.ts`)
```typescript
export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  // ... other protected routes with canActivate: [authGuard]
  { path: '**', redirectTo: '/login' }
];
```

---

### Backend (ASP.NET Core)

#### `AuthController` (`Controllers/AuthController.cs`)
```csharp
[ApiController]
[Route("api/auth")]
public class AuthController : ControllerBase
{
    [HttpPost("login")]
    public async Task<IActionResult> Login([FromBody] LoginRequest request)
    // Returns: 200 { token: string } | 401
}
```

#### `AuthService` (`Services/AuthService.cs`)
```csharp
public interface IAuthService
{
    Task<string?> AuthenticateAsync(string email, string password, string storeId);
    // Returns signed JWT string, or null if invalid/not admin
}
```

#### `JwtService` (`Services/JwtService.cs`)
```csharp
public interface IJwtService
{
    string GenerateToken(int userId, string storeId, string userRole);
    // exp = DateTime.UtcNow + 8 hours
}
```

#### `StoreDeviceService` (`Services/StoreDeviceService.cs`)
```csharp
public interface IStoreDeviceService
{
    Task<IReadOnlyList<string>> GetDeviceIdsForStoreAsync(string storeId);
    // Used by analytics controllers to scope queries
}
```

#### JWT Claims Extension (`Extensions/ClaimsPrincipalExtensions.cs`)
```csharp
public static class ClaimsPrincipalExtensions
{
    public static string GetStoreId(this ClaimsPrincipal user)
    public static int GetUserId(this ClaimsPrincipal user)
    public static string GetUserRole(this ClaimsPrincipal user)
}
```

#### Middleware / Filter: `StoreScope`
All analytics controllers inject `IStoreDeviceService` and call `GetDeviceIdsForStoreAsync(User.GetStoreId())` at the start of each action to obtain the device filter list. This is not a middleware but a per-action call to keep it explicit and testable.

---

## Data Models

### Database Tables (iPosDb)

#### `Users`
| Column     | Type          | Notes                        |
|------------|---------------|------------------------------|
| Id         | int (PK)      | Auto-increment               |
| Email      | nvarchar(256) | Unique, used for login       |
| Password   | nvarchar(512) | Hashed (BCrypt or PBKDF2)    |
| StoreId    | nvarchar(50)  | FK to store                  |
| UserRole   | nvarchar(50)  | `'admin'` required for access|

#### `Devices` (or equivalent terminal table)
| Column   | Type         | Notes                    |
|----------|--------------|--------------------------|
| DeviceId | nvarchar(50) | PK, unique terminal ID   |
| StoreId  | nvarchar(50) | FK — links device to store|

#### `InvoiceLines` (existing, filtered by DeviceId)
| Column   | Type         | Notes                         |
|----------|--------------|-------------------------------|
| DeviceId | nvarchar(50) | Used to scope analytics queries|
| ...      | ...          | Existing columns unchanged    |

### JWT Claims Structure
```json
{
  "sub":    "42",
  "storeId": "STORE-001",
  "role":   "admin",
  "iat":    1700000000,
  "exp":    1700028800,
  "iss":    "iPOS-API",
  "aud":    "iPOS-Dashboard"
}
```
- `sub`: userId (string representation of int PK)
- `storeId`: custom claim, used for device resolution
- `role`: maps to `userRole` column; validated as `'admin'` before issuing

### Angular Auth Models (`core/models/auth.models.ts`)
```typescript
export interface LoginRequest {
  email: string;
  password: string;
  storeId: string;
}

export interface LoginResponse {
  token: string;
}

export interface AuthUser {
  sub: string;
  storeId: string;
  role: string;
  exp: number;
}
```

---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Valid login stores JWT

*For any* valid admin user credential set, calling `AuthService.login()` should result in a non-null JWT being stored in `localStorage` under `ipos_auth_token`, and `isAuthenticated()` returning `true`.

**Validates: Requirements 1.3**

---

### Property 2: Invalid credentials produce a generic error

*For any* credential combination that results in a 401 response from the API, the error message displayed to the user should not indicate which specific field (email, password, or storeId) was incorrect.

**Validates: Requirements 1.4**

---

### Property 3: Non-admin role is rejected

*For any* user whose `userRole` is not `'admin'`, the login attempt should fail with an "Access denied" message and no JWT should be stored.

**Validates: Requirements 1.5, 3.3**

---

### Property 4: Unauthenticated and expired requests redirect to login

*For any* protected route, if the current session has no JWT or the JWT's `exp` claim is in the past, `AuthGuard` should redirect to `/login` rather than activating the route.

**Validates: Requirements 2.1, 2.2**

---

### Property 5: Return URL is preserved through login

*For any* protected route URL that triggers an unauthenticated redirect, successfully logging in should navigate the user to that original URL rather than the default `/dashboard`.

**Validates: Requirements 2.3**

---

### Property 6: JWT contains required claims

*For any* successful admin login, the JWT returned by the API should contain `sub` (userId), `storeId`, `role`, `iat`, and `exp` claims with correct values matching the authenticated user's record.

**Validates: Requirements 3.2**

---

### Property 7: JWT expiry is exactly 8 hours

*For any* JWT issued by the API, the difference between the `exp` claim and the `iat` claim should equal exactly 28800 seconds (8 hours).

**Validates: Requirements 3.4**

---

### Property 8: Analytics results are scoped to the user's store's devices

*For any* authenticated user with storeId S, all records returned by any analytics endpoint should have a `deviceId` that belongs to store S, and no records from devices belonging to other stores should appear.

**Validates: Requirements 4.2, 4.3**

---

### Property 9: Requests without a valid JWT return 401 with no data

*For any* analytics API endpoint, a request made without an `Authorization` header (or with an invalid/expired token) should return HTTP 401 and an empty response body.

**Validates: Requirements 4.4**

---

### Property 10: Logout clears the JWT

*For any* authenticated session, calling `AuthService.logout()` should remove the JWT from `localStorage` such that `getToken()` returns `null` and `isAuthenticated()` returns `false`.

**Validates: Requirements 5.1**

---

## Error Handling

| Scenario | Backend response | Frontend behavior |
|---|---|---|
| Wrong email/password | 401 | Generic "Invalid credentials" message |
| Valid credentials, non-admin role | 401 | "Access denied" message |
| Expired JWT on API call | 401 | Interceptor calls `logout()`, redirect to `/login` |
| Network error during login | N/A | Show "Unable to connect" message |
| Missing storeId in request | 400 | Show validation error on form |
| DB unavailable | 500 | Generic server error message |

Backend error responses use a consistent shape:
```json
{ "error": "Unauthorized" }
```
No field-level detail is exposed in 401 responses to prevent user enumeration.

---

## Testing Strategy

### Unit Tests

**Angular (Jest or Karma/Jasmine):**
- `AuthService`: mock `HttpClient`; verify JWT storage, `isAuthenticated()` logic, expiry check, logout behavior
- `AuthGuard`: verify redirect when no token, redirect when expired, pass-through when valid
- `AuthInterceptor`: verify `Authorization` header is attached, verify 401 triggers logout
- `LoginComponent`: verify form validation, error message display, navigation on success

**Backend (xUnit):**
- `AuthService.AuthenticateAsync`: mock DB; test valid admin, valid non-admin, wrong password, unknown email
- `JwtService.GenerateToken`: verify all claims present, verify expiry is exactly 8 hours
- `StoreDeviceService.GetDeviceIdsForStoreAsync`: mock DB; verify correct deviceIds returned per storeId
- `AuthController`: integration test with in-memory DB; verify 200/401 responses

### Property-Based Tests

Use **fast-check** (TypeScript/Angular) and **FsCheck** (C#/xUnit).

Each property test runs a minimum of **100 iterations**.

Tag format: `// Feature: ipos-dashboard-auth, Property {N}: {property_text}`

**P1 — Valid login stores JWT** (fast-check, Angular)
Generate random valid user objects; mock API to return a token; assert `localStorage` contains token and `isAuthenticated()` is true.

**P3 — Non-admin role is rejected** (FsCheck, C#)
Generate random `userRole` strings that are not `'admin'`; assert `AuthenticateAsync` returns `null` and API returns 401.

**P4 — Unauthenticated/expired requests redirect** (fast-check, Angular)
Generate random route URLs from the protected route list; for each, assert `authGuard` returns a `UrlTree` pointing to `/login` when no token or expired token is present.

**P6 — JWT contains required claims** (FsCheck, C#)
Generate random valid admin user records; call `JwtService.GenerateToken`; decode and assert `sub`, `storeId`, `role`, `iat`, `exp` are all present and match inputs.

**P7 — JWT expiry is exactly 8 hours** (FsCheck, C#)
For any generated token, assert `exp - iat == 28800`.

**P8 — Analytics scoped to store's devices** (FsCheck, C#)
Generate random sets of stores, devices, and invoice records; seed in-memory DB; call analytics endpoints with a JWT for store S; assert all returned records have `deviceId` in the set for store S.

**P9 — No JWT returns 401** (FsCheck, C#)
For any analytics endpoint, send request without Authorization header; assert 401 and empty body.

**P10 — Logout clears JWT** (fast-check, Angular)
For any stored token string, call `logout()`; assert `getToken()` is null and `isAuthenticated()` is false.
