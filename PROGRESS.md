# iPOS Development Progress

---

## 2026-07-23 — Stock Take Overhaul + Reports Hub

### Overview
Full rework of the Stock Take feature from a single-session throw-away screen into a persistent, resumable, session-based workflow. Reports screen rebuilt as a navigation hub. Brand name added to product display throughout stock taking.

---

### 1. StockTake Session System

**Problem:** The old screen buffered all counts in memory and discarded them if the user navigated away. There was no concept of a "session" — every open was a fresh start.

**New entity — `StockTakeSession`**
- File: `data/entities/StockTakeSession.kt`
- Fields: `sessionId`, `startedAt`, `closedAt`, `status` (OPEN / COMPLETED / CANCELLED), `startedBy`, `storeId`, `deviceId`
- A session stays OPEN until explicitly closed — survives app restarts and navigation

**`StockTake` entity updated**
- File: `data/entities/StockTake.kt`
- Added `sessionId: Long` (defaults to `0` for legacy records)

**New DAO — `StockTakeSessionDao`**
- File: `data/data_source/StockTakeSessionDao.kt`
- Queries: `getOpenSession()` (Flow), `getOpenSessionOnce()`, `getCountedProductIds(sessionId)` (Flow<List<Long>>), `getRecordsForSession(sessionId)`

**`StockTakeDao` updated**
- File: `data/data_source/StockTakeDao.kt`
- Added `getRecordForProduct(sessionId, productId)` and `updateSessionId(id, sessionId)`

---

### 2. StockTake Repository

**File:** `data/repositories/StockTakeRepository.kt` — fully rebuilt

New interface methods:
- `getOpenSession(): Flow<StockTakeSession?>`
- `getOpenSessionOnce(): StockTakeSession?`
- `startSession(startedBy): Long`
- `closeSession(sessionId)`
- `cancelSession(sessionId)`
- `getAllSessions(): Flow<List<StockTakeSession>>`
- `getCountedProductIds(sessionId): Flow<Set<Long>>` — DAO returns `List<Long>`, mapped to `Set` via `.map { it.toSet() }`
- `getRecordsForSession(sessionId): Flow<List<StockTake>>`
- `upsertStockTakeRecord(stockTake)` — saves immediately on Check, no buffering

---

### 3. Dedicated StockTakeViewModel

**File:** `viewmodels/StockTakeViewModel.kt` — new file

- Replaces StockTake logic previously bolted onto `InvoiceViewModel`
- Uses `getProductsWithBrand()` so brand name is available for display and search
- Search filters across: brand name, product description, product code
- `allSessions: StateFlow<List<StockTakeSession>>` exposed for the report screen
- Key functions: `startSession()`, `closeSession()`, `cancelSession()`, `recordItem()`, `onSearchQueryChanged()`, `showAddProductDialog()`, `addNewProductAndRecord()`
- `addNewProductAndRecord()` inserts to `Products`, creates `PriceList` entry, and immediately records a StockTake entry in the current session

---

### 4. StockTaking Screen — Full Rebuild

**File:** `screens/StockTakingScreen.kt` — full rewrite

UI changes:
- **No open session:** "Start Stock Take" screen explaining the session persists until closed
- **Progress bar:** `x / total items` counted shown as a linear indicator
- **Sticky search bar:** filters by brand, description, or product code — with clear button
- **Compact item rows** replacing the old tall cards:
  - Colour dot indicator: grey = uncounted, green = matched, amber = variance
  - Product shown as `BrandName - Description` with system qty and code beneath
  - Inline count field + "Check" button per row
  - Variance reason field expands only when needed (animated)
  - Checked items show ✓ and can be tapped to re-open for correction
- **Empty search result:** shows "Add as new product" prompt + FAB
- **AddNewProductDialog:** bottom sheet to create a new product (description, code, costs, qty) and immediately record it in the session
- **Close Session** button in top bar (destructive, marked red)
- Navigation parameter `viewModel: InvoiceViewModel` removed — screen uses `hiltViewModel<StockTakeViewModel>()` directly

---

### 5. Database Migration

**File:** `data/AppDatabase.kt`

- DB version bumped: `10 → 11`
- `StockTakeSession::class` added to `@Database` entities
- `abstract fun stockTakeSessionDao(): StockTakeSessionDao` added
- `MIGRATION_10_11` added:
  - Creates `stock_take_session` table
  - Adds `sessionId INTEGER NOT NULL DEFAULT 0` column to existing `stock_take` table

---

### 6. DI Wiring

**File:** `app/di/AppModule.kt`

- Added `MIGRATION_10_11` to Room builder
- Added `provideStockTakeSessionDao()`
- Updated `provideStockTakeRepository()` to inject `StockTakeSessionDao`

---

### 7. Reports Screen — Hub Rebuild

**File:** `screens/ReportsScreen.kt` — full rewrite

- Replaced the old single-page invoice list with a **2-column card grid**
- Cards: Sales, Multi-Currency, Stock Takes, Business Performance, Expenses, Orders
- Each card has an icon, title, and subtitle
- Tapping navigates to the relevant report screen
- Helper functions (`ThreeDotMenu`, `filterInvoicesByDate`, `InvoiceCard`) retained in this file for shared use

---

### 8. New Screens

**`SalesReportScreen`** — `screens/SalesReportScreen.kt`
- Invoice list extracted from the old ReportsScreen
- Filter menu (Today / This Week / This Month / All) in the top bar
- Share FAB for sending the report
- Back navigation

**`StockTakeReportScreen`** — `screens/StockTakeReportScreen.kt`
- Lists all stock take sessions in reverse chronological order
- Session cards show: session ID, started by, started date, closed date, status badge
- Status badges: In Progress (blue), Completed (green), Cancelled (grey)
- Uses `StockTakeViewModel` via `hiltViewModel()`

---

### 9. Navigation

**File:** `data/Destinations.kt`
- Added `StockTakeReport : Destinations("stockTakeReport")`
- Added `SalesReport : Destinations("salesReport")`

**File:** `app/DefaultScreen.kt`
- `StockTakingScreen` call updated — removed `viewModel` parameter (VM injected internally)
- Added `composable(Destinations.SalesReport.route)` → `SalesReportScreen`
- Added `composable(Destinations.StockTakeReport.route)` → `StockTakeReportScreen`
- Added imports for `SalesReportScreen` and `StockTakeReportScreen`

---

### Files Changed Summary

| File | Action |
|---|---|
| `data/entities/StockTakeSession.kt` | **New** |
| `data/entities/StockTake.kt` | Modified — added `sessionId` field |
| `data/data_source/StockTakeSessionDao.kt` | **New** |
| `data/data_source/StockTakeDao.kt` | Modified — added 2 queries |
| `data/repositories/StockTakeRepository.kt` | Modified — full rebuild |
| `viewmodels/StockTakeViewModel.kt` | **New** |
| `screens/StockTakingScreen.kt` | Modified — full rewrite |
| `screens/StockTakeReportScreen.kt` | **New** |
| `screens/SalesReportScreen.kt` | **New** |
| `screens/ReportsScreen.kt` | Modified — full rewrite |
| `data/AppDatabase.kt` | Modified — version 11, new entity + DAO + migration |
| `app/di/AppModule.kt` | Modified — new DAO provider, updated repo provider |
| `data/Destinations.kt` | Modified — 2 new routes |
| `app/DefaultScreen.kt` | Modified — nav wiring updated |
