---
feature: iPOS Dashboard Authentication & Store-Scoped Filtering
type: feature
status: requirements
created: 2025-01-01
---

# Requirements Document

## Introduction

Add authentication and store-scoped data filtering to the iPOS Analytics Dashboard. Users must log in with email, password, and store selection before accessing any dashboard content. Only users with `userRole = 'admin'` may access the dashboard. Once authenticated, all dashboard data is filtered to show only data from devices belonging to the logged-in user's store.

## Glossary

- **Auth_Service**: The Angular service responsible for login, logout, and session management
- **Auth_Guard**: The Angular route guard that protects dashboard routes from unauthenticated access
- **Dashboard**: The iPOS Analytics Dashboard Angular frontend
- **iPosDb**: The SQL Server database containing user and store data
- **Store**: A retail location, identified by a store ID, that owns one or more devices
- **Device**: A POS terminal identified by a `deviceId`, belonging to a specific store
- **User**: A person with credentials stored in iPosDb, having an email, password, store association, and userRole
- **Admin**: A user whose `userRole` field equals `'admin'`
- **JWT**: JSON Web Token used to maintain authenticated sessions
- **API**: The existing ASP.NET Core backend

## Requirements

### Requirement 1: Login Page

**User Story:** As a user, I want a login page, so that I can authenticate before accessing the dashboard.

#### Acceptance Criteria

1. THE Dashboard SHALL display a login page as the default route when no authenticated session exists
2. THE Login_Page SHALL present input fields for email, password, and store selection
3. WHEN a user submits valid credentials, THE Auth_Service SHALL store the returned JWT and redirect the user to the dashboard
4. IF login credentials are invalid, THEN THE Login_Page SHALL display an error message without revealing which field is incorrect
5. IF the authenticated user's `userRole` is not `'admin'`, THEN THE Auth_Service SHALL reject the session and display an "Access denied" message

### Requirement 2: Route Protection

**User Story:** As a system owner, I want all dashboard routes protected, so that unauthenticated users cannot access any data.

#### Acceptance Criteria

1. THE Auth_Guard SHALL redirect unauthenticated requests for any dashboard route to the login page
2. WHEN an authenticated session expires, THE Auth_Guard SHALL redirect the user to the login page
3. THE Dashboard SHALL preserve the originally requested route and redirect the user there after successful login

### Requirement 3: Authentication API

**User Story:** As a developer, I want a login API endpoint, so that the Angular frontend can authenticate users against iPosDb.

#### Acceptance Criteria

1. WHEN a POST request is made to `/api/auth/login` with email, password, and storeId, THE API SHALL validate credentials against the `Users` table in iPosDb
2. IF credentials are valid and `userRole = 'admin'`, THEN THE API SHALL return a signed JWT containing userId, storeId, and userRole
3. IF credentials are invalid or `userRole` is not `'admin'`, THEN THE API SHALL return HTTP 401
4. THE JWT SHALL have an expiry of 8 hours

### Requirement 4: Store-Scoped Data Filtering

**User Story:** As an admin user, I want the dashboard to show only my store's data, so that I don't see data from other stores.

#### Acceptance Criteria

1. WHILE a user is authenticated, THE Dashboard SHALL include the user's storeId in all API data requests
2. THE API SHALL resolve the set of `deviceIds` that belong to the authenticated user's store
3. THE API SHALL filter all analytics queries to include only records associated with those `deviceIds`
4. IF a request is made without a valid JWT, THEN THE API SHALL return HTTP 401 and no data

### Requirement 5: Logout

**User Story:** As a user, I want to log out, so that my session is terminated and my data is protected.

#### Acceptance Criteria

1. WHEN a user triggers logout, THE Auth_Service SHALL remove the stored JWT and redirect to the login page
2. AFTER logout, THE Auth_Guard SHALL prevent access to dashboard routes without re-authentication
