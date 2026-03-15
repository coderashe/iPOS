# Implementation Plan: Entity Sync Workers

## Overview

Implement sync workers for all 14 Room entities with `isSynced` fields, using a shared `BaseSyncWorker`, a unified `SyncApiService`, and a FK-safe WorkManager DAG via `SyncManager.scheduleFullSync()`.

## Tasks

- [x] 1. Add `getUnsynced` and `markSynced` to all 14 DAOs
  - [x] 1.1 Add `getUnsynced(limit: Int)` and `markSynced(ids: List<PkType>)` to CustomerDao, ExpenseDao, ShoeBrandDao, ShoeSizeDao, and CurrencyDao
    - Use `SELECT * FROM <table> WHERE isSynced = 0 LIMIT :limit` and `UPDATE <table> SET isSynced = 1 WHERE <pk> IN (:ids)`
    - PK types: Customer/Expense/ShoeBrand/ShoeSize → Long; Currency → String
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

  - [ ]* 1.2 Write property test for CustomerDao getUnsynced / markSynced
    - **Property 5: getUnsynced never returns already-synced rows**
    - **Property 6: markSynced is idempotent and precise**
    - **Validates: Requirements 2.1, 2.2, 2.3, 2.4**
    - Use Room in-memory DB; generate arbitrary lists of Customer rows with random isSynced values

  - [x] 1.3 Add `getUnsynced` and `markSynced` to PaymentDao, CartItemDao, StockTakeDao, TradeReceivablesDao, and DayReportsDao
    - PK types: Payment/CartItem/TradeReceivables/DayReports → Long; StockTake → Int
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

  - [x] 1.4 Add `getUnsynced` and `markSynced` to ProductDayReportDao, CashFlowDao, DiscountsReceivedDao, and PriceListDao
    - PK types: all Long
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_


- [x] 2. Create `SyncApiService.kt` with DTOs, `toSyncDto()` extensions, and `SyncResult`
  - [x] 2.1 Define `SyncResult` data class and all 14 `*SyncDto` data classes in `network/SyncApiService.kt`
    - Map Room entity fields to the backend contract from `backend/Dtos/SyncDtos.cs`
    - Co-locate all DTOs in the same file
    - _Requirements: 3.4, 4.1, 4.3_

  - [x] 2.2 Implement `toSyncDto()` extension functions for all 14 entities
    - Each extension converts a Room entity to its corresponding `*SyncDto`
    - Co-locate in `SyncApiService.kt`
    - _Requirements: 4.2, 4.4_

  - [ ]* 2.3 Write property test for DTO round-trip consistency
    - **Property 7: DTO round-trip preserves entity data**
    - **Validates: Requirements 3.2, 4.2, 4.4**
    - Use kotest-property Arb generators for each entity; serialize DTO to JSON and back, assert field equivalence

  - [x] 2.4 Declare the `SyncApiService` Retrofit interface with all 14 `suspend` POST methods
    - Endpoints: `/api/customers/sync`, `/api/expenses/sync`, `/api/shoebrands/sync`, `/api/shoesizes/sync`, `/api/currencies/sync`, `/api/payments/sync`, `/api/cartitems/sync`, `/api/stocktakes/sync`, `/api/tradereceivables/sync`, `/api/dayreports/sync`, `/api/productdayreports/sync`, `/api/cashflows/sync`, `/api/discountsreceived/sync`, `/api/pricelists/sync`
    - Each method takes `@Body dtos: List<*SyncDto>` and returns `Response<SyncResult>`
    - _Requirements: 3.1, 3.2, 3.5_

- [x] 3. Create `BaseSyncWorker.kt`
  - [x] 3.1 Implement the abstract `BaseSyncWorker` class extending `CoroutineWorker`
    - Declare abstract methods: `fetchUnsynced(limit: Int)`, `syncToApi(entity: Any): Boolean`, `markSynced(entity: Any)`
    - Implement `doWork()`: check connectivity → call `performBatchSync()` → return result
    - Implement `performBatchSync()`: fetch batch → loop sync → mark synced → classify errors (4xx skip, 5xx retry)
    - Use `BATCH_SIZE = 50` constant; inject `ConnectivityHelper` via constructor
    - Annotate with `@HiltWorker` / `@AssistedInject`
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 1.10_

  - [ ]* 3.2 Write property test: no-network always retries
    - **Property 1: No-network always retries**
    - **Validates: Requirements 1.2, 7.1**
    - Use kotest-property; generate arbitrary entity lists; fake ConnectivityHelper returns false; assert doWork() == Result.retry() and fetchUnsynced never called

  - [ ]* 3.3 Write property test: successful batch marks all entities synced
    - **Property 2: Successful batch marks all entities synced**
    - **Validates: Requirements 1.5, 1.8**
    - Generate arbitrary non-empty entity lists; all syncToApi calls return 2xx; assert markSynced called once per entity and doWork() == Result.success()

  - [ ]* 3.4 Write property test: 4xx skips entity but does not abort batch
    - **Property 3: 4xx skips entity but does not abort batch**
    - **Validates: Requirements 1.6, 7.3**
    - Generate batch of N entities; one random entity returns 4xx, rest return 2xx; assert N-1 markSynced calls and doWork() == Result.success()

  - [ ]* 3.5 Write property test: 5xx retries entire batch
    - **Property 4: 5xx retries entire batch**
    - **Validates: Requirements 1.7, 7.4**
    - Generate arbitrary batch; any entity returns 5xx; assert doWork() == Result.retry() and markSynced never called

- [ ] 4. Checkpoint — Ensure all DAO and BaseSyncWorker tests pass
  - Ensure all tests pass, ask the user if questions arise.


- [x] 5. Implement the 14 entity sync workers (Layer 1 — no FK dependencies)
  - [x] 5.1 Create `SyncShoeBrandWorker.kt`, `SyncCurrencyWorker.kt`, and `SyncExpenseWorker.kt`
    - Each extends `BaseSyncWorker`; inject DAO + SyncApiService + ConnectivityHelper
    - Implement `fetchUnsynced`, `syncToApi` (call toSyncDto + matching SyncApiService method), `markSynced`
    - Expose inner `Factory` interface extending `WorkerAssistedFactory`
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

  - [x] 5.2 Create `SyncDayReportsWorker.kt`, `SyncCashFlowWorker.kt`, `SyncDiscountsReceivedWorker.kt`, and `SyncTradeReceivablesWorker.kt`
    - Same pattern as 5.1
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

- [-] 6. Implement the 14 entity sync workers (Layers 2–5)
  - [x] 6.1 Create `SyncShoeSizeWorker.kt` and `SyncCustomerWorker.kt` (Layer 2)
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

  - [x] 6.2 Create `SyncPriceListWorker.kt` (Layer 3)
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

  - [x] 6.3 Create `SyncPaymentWorker.kt`, `SyncCartItemWorker.kt`, `SyncStockTakeWorker.kt`, and `SyncProductDayReportWorker.kt` (Layer 5)
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5_

  - [ ]* 6.4 Write unit tests for a representative entity worker (e.g., SyncCustomerWorker)
    - Test fetchUnsynced delegates to DAO, syncToApi calls correct SyncApiService method, markSynced delegates to DAO
    - _Requirements: 5.2, 5.3, 5.4_

- [x] 7. Wire `SyncApiService` into `NetworkModule` and worker factories into `WorkerModule`
  - [x] 7.1 Add `@Provides @Singleton fun provideSyncApiService(retrofit: Retrofit): SyncApiService` to `NetworkModule.kt`
    - _Requirements: 3.3, 8.1_

  - [x] 7.2 Add `@Binds` entries for all 14 new worker factories in `WorkerModule.kt`
    - Bind each concrete `Factory` to `WorkerAssistedFactory<WorkerType>`
    - _Requirements: 5.6, 8.2, 8.3_

- [x] 8. Implement `SyncManager.scheduleFullSync()`
  - [x] 8.1 Add `scheduleFullSync()` to `SyncManager.kt` using a five-layer WorkManager DAG
    - Build `OneTimeWorkRequest` for each of the 18 workers (14 new + 4 existing) with `NetworkType.CONNECTED` constraint and 15-min exponential backoff
    - Chain layers: `beginUniqueWork("full_sync", KEEP, layer1).then(layer2).then(layer3).then(layer4).then(layer5).enqueue()`
    - Layer assignments per design: L1 = ShoeBrand/Currency/Expense/DayReports/CashFlow/DiscountsReceived/TradeReceivables; L2 = ShoeSize/Customer; L3 = Product/PriceList; L4 = Invoice; L5 = InvoiceLine/Payment/CartItem/StockTake/ProductDayReport
    - Retain all existing scheduling methods unchanged
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 9.3, 9.4_

  - [ ]* 8.2 Write property test: KEEP policy prevents duplicate sync chains
    - **Property 8: KEEP policy prevents duplicate sync chains**
    - **Validates: Requirements 6.2, 7.5**
    - Call scheduleFullSync() N times (N ≥ 2) while chain is active; assert exactly one active "full_sync" chain in WorkManager

  - [ ]* 8.3 Write property test: DAG layer ordering is respected
    - **Property 9: DAG layer ordering is respected**
    - **Validates: Requirements 6.3**
    - For each (parent-layer worker, child-layer worker) pair, assert child's prerequisiteWorkIds contains parent's work ID

- [x] 9. Final checkpoint — Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for a faster MVP
- Each task references specific requirements for traceability
- Property tests use `kotest-property`; integration tests use Room in-memory DB + MockWebServer
- Existing workers (Invoice, InvoiceLine, Product, User) are included in the DAG but their internals are not modified (Requirement 9)
