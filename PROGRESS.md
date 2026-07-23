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

---

## 2026-07-23 (Session 2) — Stock Take Detail Screen + PDF Export

### Overview
Extended the Stock Take History screen with clickable session cards that navigate to a full detail/summary screen. Added a PDF export that generates a structured, multi-page A4 report and shares it via the system share sheet.

---

### 1. StockTakeViewModel — Detail Logic

**File:** `viewmodels/StockTakeViewModel.kt`

New state classes added at the bottom of the file:

- `VarianceRow` — holds displayName (`BrandName - Description`), systemQty, countedQty, variance, reason
- `StockTakeSuggestion` — title, body, and `SuggestionType` (OK / INFO / WARNING)
- `StockTakeDetail` — full summary: session meta, KPI counts, variance rows, uncounted products, suggestions
- `StockTakeDetailState` — sealed class: `Loading`, `Ready(detail)`, `Error(message)`

New ViewModel members:
- `_detailState: MutableStateFlow<StockTakeDetailState>` + `detailState: StateFlow`
- `fun loadSessionDetail(sessionId: Long)` — fetches session, all StockTake records for it, and all ProductWithBrand; computes KPIs, sorts variances by magnitude, builds uncounted gap list, generates suggestions
- `fun buildSuggestions(...)` — rule-based insights:
  - Coverage < 50%: warning to delay decisions
  - Coverage 50–80%: info to complete the count
  - Coverage ≥ 80%: OK confirmation
  - Shortage rate > 10%: warning about shrinkage/theft
  - Any shortages: info to cross-check invoices
  - Any surpluses: info to check unrecorded deliveries
  - Zero variances: perfect match OK message

---

### 2. StockTakeDetailScreen (new)

**File:** `screens/StockTakeDetailScreen.kt`

Full detail screen for a single stock-take session:

- **Session header card** — session ID, status badge (colour-coded), started by, start/close dates
- **KPI grid (2 rows × 3 cards)** — Counted, Total Catalog, Coverage %, Variances, Shortages, Surpluses. Coverage card colour-codes by threshold (green ≥ 80%, amber ≥ 50%, red < 50%)
- **Business Insights section** — suggestion cards with icons: ✓ green (OK), ℹ blue (INFO), ⚠ amber (WARNING)
- **Variance Details table** — sorted by magnitude descending. Columns: Product, System Qty, Counted, Variance. Red for shortages, green for surpluses. Variance reason shown beneath row if present
- **Uncounted Products list** — gap analysis of products in catalog not recorded in this session
- **Export PDF FAB** — `ExtendedFloatingActionButton` calling `StockTakePdfGenerator.generateAndShare()`
- Triggered via `LaunchedEffect(sessionId)` → `viewModel.loadSessionDetail(sessionId)`

---

### 3. StockTakePdfGenerator (new)

**File:** `app/utils/StockTakePdfGenerator.kt`

Multi-page A4 PDF generator using Android's native `android.graphics.pdf.PdfDocument` (no external library).

**Page management pattern:**
- `startNewPage()` — finishes the current open page before starting a new one, eliminating double-finish bugs
- `ensureSpace(needed)` — calls `startNewPage()` if remaining vertical space is insufficient
- **Critical fix applied**: `doc.writeTo(file)` is called **before** `doc.close()` — previous bug was closing the document first, causing "document is closed" error

**PDF content sections:**
1. Title + session metadata header
2. KPI grid (2 rows × 3 cols) with coloured boxes
3. Business Insights — boxed suggestions with [!] / [OK] / [i] prefix
4. Variance Details table with alternating row tints
5. Uncounted Products list
6. Footer with generation timestamp on every last page

**Text wrapping** — `wrapText()` utility splits long suggestion bodies to fit within page margins.

**Output:** `StockTake_Session{id}_{yyyy-MM-dd}.pdf` saved to app's Documents folder, shared via `ACTION_SEND` intent through the existing FileProvider.

---

### 4. StockTakeReportScreen — Cards Made Clickable

**File:** `screens/StockTakeReportScreen.kt`

- `StockTakeSessionCard` now accepts `onClick: () -> Unit` parameter
- Card has `.clickable { onClick() }` modifier
- Click navigates to `Destinations.StockTakeDetail.createRoute(session.sessionId)`
- Removed unused `uiState` collection (detail screen has its own state)
- Added `import com.example.learncompose303.data.Destinations`

---

### 5. Navigation

**File:** `data/Destinations.kt`
- Added `StockTakeDetail : Destinations("stockTakeDetail/{sessionId}")` with `createRoute(sessionId: Long)`

**File:** `app/DefaultScreen.kt`
- Added import for `StockTakeDetailScreen`
- Added `composable(Destinations.StockTakeDetail.route)` with `navArgument("sessionId", NavType.LongType)`

---

### Bug Fixed — PDF "document is closed" Error

**Root cause 1:** `doc.close()` was called before `doc.writeTo()`. PdfDocument becomes unreadable after close.

**Root cause 2:** The old page list approach called `doc.finishPage(pages.last())` inside `ensureSpace()` AND again at the end — double-finishing the last page caused the crash.

**Fix:** Rewrote page management with a single `currentPage` reference. `startNewPage()` always finishes the current open page before starting a new one. Only one `finishPage` call per page, ever.

---

### Files Changed Summary

| File | Action |
|---|---|
| `viewmodels/StockTakeViewModel.kt` | Modified — detail state, `loadSessionDetail()`, `buildSuggestions()`, data models |
| `screens/StockTakeDetailScreen.kt` | **New** |
| `app/utils/StockTakePdfGenerator.kt` | **New** |
| `screens/StockTakeReportScreen.kt` | Modified — cards clickable, navigates to detail |
| `data/Destinations.kt` | Modified — added `StockTakeDetail` route |
| `app/DefaultScreen.kt` | Modified — wired `StockTakeDetailScreen` composable |
| `PROGRESS.md` | Updated |
