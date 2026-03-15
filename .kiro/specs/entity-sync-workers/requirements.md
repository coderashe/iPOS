# Requirements Document

## Introduction

This feature adds WorkManager sync workers for the 14 Room entities that have an `isSynced` field but no corresponding sync worker. It introduces a `BaseSyncWorker` abstract class to eliminate boilerplate, a unified `SyncApiService` Retrofit interface for all 14 endpoints, and replaces `SyncManager.syncAllNow()` with a DAG-ordered `scheduleFullSync()` that respects foreign-key dependencies between entities. The goal is to ensure all local POS data flows up to the backend consistently and reliably.

---

## Glossary

- **BaseSyncWorker**: Abstract `CoroutineWorker` subclass that provides the shared batch-sync loop, connectivity check, and error classification logic.
- **Entity Worker**: A thin `BaseSyncWorker` subclass responsible for syncing one specific Room entity (e.g., `SyncCustomerWorker`).
- **SyncApiService**: Single Retrofit interface declaring all 14 batch-sync HTTP endpoints.
- **SyncManager**: Application-level coordinator that enqueues the WorkManager DAG.
- **DAO**: Room Data Access Object; the database interface for a single entity table.
- **isSynced**: Integer column (`0` = unsynced / dirty, `1` = synced) present on all 14 target entities.
- **Batch**: A list of up to 50 unsynced entity rows fetched in one `getUnsynced(limit)` call.
- **DAG**: Directed Acyclic Graph of WorkManager work chains that enforces FK-safe ordering across sync layers.
- **KEEP policy**: `ExistingWorkPolicy.KEEP` — if a named work chain is already enqueued or running, a new enqueue request is silently dropped.
- **ConnectivityHelper**: Utility that checks whether a network connection is currently available.
- **SyncDto**: Data-transfer object that maps a Room entity's fields to the backend API contract.
- **SyncResult**: Response body returned by each sync endpoint containing `syncedCount` and optional `errors`.
- **Layer**: A set of Entity Workers that can run in parallel because they share no FK dependency on each other.

---

## Requirements

### Requirement 1: BaseSyncWorker Abstract Class

**User Story:** As an Android developer, I want a shared abstract base worker, so that all entity sync workers reuse the same connectivity check, batch loop, and error-handling logic without duplicating code.

#### Acceptance Criteria

1. THE BaseSyncWorker SHALL extend `CoroutineWorker` and declare three abstract methods: `fetchUnsynced(limit: Int)`, `syncToApi(entity: Any)`, and `markSynced(entity: Any)`.
2. WHEN `doWork()` is called and `ConnectivityHelper.isNetworkAvailable()` returns false, THE BaseSyncWorker SHALL return `Result.retry()` without accessing the database or network.
3. WHEN `doWork()` is called and network is available, THE BaseSyncWorker SHALL invoke `performBatchSync()` and return its result.
4. WHEN `fetchUnsynced(limit)` returns an empty list, THE BaseSyncWorker SHALL return `Result.success()` immediately.
5. WHEN `syncToApi(entity)` succeeds (HTTP 2xx), THE BaseSyncWorker SHALL call `markSynced(entity)` for that entity and continue processing the remaining entities in the batch.
6. WHEN `syncToApi(entity)` returns an HTTP 4xx response, THE BaseSyncWorker SHALL log the error, skip that entity, and continue processing the remaining entities in the batch without aborting.
7. WHEN `syncToApi(entity)` returns an HTTP 5xx response or throws an `IOException`, THE BaseSyncWorker SHALL return `Result.retry()` immediately, leaving all unsynced entities with `isSynced = 0`.
8. WHEN all entities in a batch are processed without a 5xx or `IOException`, THE BaseSyncWorker SHALL return `Result.success()`.
9. THE BaseSyncWorker SHALL use a batch size constant of 50 rows per `fetchUnsynced()` call.
10. THE BaseSyncWorker SHALL be injectable via Hilt using `@HiltWorker` and `@AssistedInject`.

---

### Requirement 2: DAO Query Additions

**User Story:** As an Android developer, I want each entity DAO to expose `getUnsynced` and `markSynced` queries, so that the sync workers can fetch dirty rows and mark them clean without loading the entire table.

#### Acceptance Criteria

1. THE DAO for each of the 14 entities SHALL expose a `suspend fun getUnsynced(limit: Int): List<Entity>` query that returns at most `limit` rows where `isSynced = 0`.
2. THE DAO for each of the 14 entities SHALL expose a `suspend fun markSynced(ids: List<PkType>)` query that sets `isSynced = 1` for all rows whose primary key is in `ids`.
3. WHEN `getUnsynced(limit)` is called, THE DAO SHALL NOT return any entity that already has `isSynced = 1`.
4. WHEN `markSynced(ids)` is called for a set of IDs, THE DAO SHALL set `isSynced = 1` for exactly those rows and leave all other rows unchanged.
5. THE DAO queries SHALL use the primary key types defined in the design: `Long` for Customer, Expense, ShoeBrand, ShoeSize, Payment, CartItem, TradeReceivables, DayReports, ProductDayReport, CashFlow, DiscountsReceived, and PriceList; `Int` for StockTake; `String` for Currency.

---

### Requirement 3: SyncApiService Retrofit Interface

**User Story:** As an Android developer, I want a single Retrofit interface for all 14 sync endpoints, so that all HTTP sync calls are declared in one place and share the same client configuration.

#### Acceptance Criteria

1. THE SyncApiService SHALL declare one `suspend` POST method per entity, targeting the endpoint paths defined in the design (e.g., `/api/customers/sync`, `/api/expenses/sync`).
2. WHEN a sync method is called, THE SyncApiService SHALL send the entity list as a JSON array in the request body and return a `Response<SyncResult>`.
3. THE SyncApiService SHALL be provided as a singleton via `NetworkModule` using the existing Retrofit instance.
4. THE SyncResult data class SHALL contain a `syncedCount: Int` field and an `errors: List<String>` field defaulting to an empty list.
5. THE SyncApiService SHALL include sync methods for all 14 entities: Customer, Expense, ShoeBrand, ShoeSize, Currency, Payment, CartItem, StockTake, TradeReceivables, DayReports, ProductDayReport, CashFlow, DiscountsReceived, and PriceList.

---

### Requirement 4: Sync DTOs and Entity Mapping

**User Story:** As an Android developer, I want each entity to have a corresponding SyncDto and a `toSyncDto()` extension function, so that Room entity fields are correctly mapped to the backend API contract before transmission.

#### Acceptance Criteria

1. THE system SHALL define a `*SyncDto` data class for each of the 14 entities, mapping Room fields to the backend contract defined in `backend/Dtos/SyncDtos.cs`.
2. THE system SHALL provide a `toSyncDto()` extension function on each Room entity that converts it to its corresponding `*SyncDto`.
3. THE SyncDto classes and `toSyncDto()` extension functions SHALL be co-located in `SyncApiService.kt`.
4. WHEN `toSyncDto()` is called on a valid entity, THE system SHALL return a `*SyncDto` containing all fields required by the backend endpoint.

---

### Requirement 5: Entity Sync Workers (×14)

**User Story:** As an Android developer, I want 14 thin entity workers that extend BaseSyncWorker, so that each entity type is synced to its dedicated backend endpoint with minimal per-entity code.

#### Acceptance Criteria

1. THE system SHALL provide one `BaseSyncWorker` subclass for each of the 14 entities: SyncCustomerWorker, SyncExpenseWorker, SyncShoeBrandWorker, SyncShoeSizeWorker, SyncCurrencyWorker, SyncPaymentWorker, SyncCartItemWorker, SyncStockTakeWorker, SyncTradeReceivablesWorker, SyncDayReportsWorker, SyncProductDayReportWorker, SyncCashFlowWorker, SyncDiscountsReceivedWorker, and SyncPriceListWorker.
2. WHEN `fetchUnsynced(limit)` is called on an Entity Worker, THE Entity Worker SHALL delegate to its injected DAO's `getUnsynced(limit)` method.
3. WHEN `syncToApi(entity)` is called on an Entity Worker, THE Entity Worker SHALL call `entity.toSyncDto()`, pass the result to the corresponding `SyncApiService` method, and return `true` if the response is successful.
4. WHEN `markSynced(entity)` is called on an Entity Worker, THE Entity Worker SHALL delegate to its injected DAO's `markSynced(listOf(entity.primaryKey))` method.
5. THE Entity Workers SHALL be injectable via Hilt using `@HiltWorker` and `@AssistedInject`, with each worker exposing a `Factory` interface extending `WorkerAssistedFactory`.
6. THE Entity Worker factories SHALL be bound in `WorkerModule` via `@Binds` entries.

---

### Requirement 6: WorkManager DAG and Scheduling

**User Story:** As an Android developer, I want `SyncManager.scheduleFullSync()` to enqueue all workers in a FK-safe DAG, so that dependent entities are never synced before the entities they reference.

#### Acceptance Criteria

1. THE SyncManager SHALL provide a `scheduleFullSync()` method that enqueues all 14 new entity workers plus the 4 existing workers (Invoice, InvoiceLine, Product, User) in a five-layer DAG using `WorkManager.beginUniqueWork`.
2. WHEN `scheduleFullSync()` is called, THE SyncManager SHALL use `ExistingWorkPolicy.KEEP` so that a second call while a sync chain is already running does not enqueue a duplicate chain.
3. THE DAG SHALL enforce the following layer ordering:
   - Layer 1 (no FK dependencies): ShoeBrand, Currency, Expense, DayReports, CashFlow, DiscountsReceived, TradeReceivables
   - Layer 2 (after Layer 1): ShoeSize, Customer
   - Layer 3 (after Layer 2): Product, PriceList
   - Layer 4 (after Layer 3): Invoice
   - Layer 5 (after Layer 4): InvoiceLine, Payment, CartItem, StockTake, ProductDayReport
4. WHEN `scheduleFullSync()` is called, THE SyncManager SHALL apply a `Constraints` object requiring `NetworkType.CONNECTED` to every worker request.
5. WHEN `scheduleFullSync()` is called, THE SyncManager SHALL configure exponential backoff with an initial delay of 15 minutes on every worker request.
6. THE SyncManager SHALL retain all existing scheduling methods (`scheduleSingleInvoiceSync`, `scheduleSingleProductSync`, `scheduleInvoiceLineSync`, `scheduleInvoiceBatchSync`, `scheduleProductBatchSync`, `scheduleInvoiceLineBatchSync`) unchanged.
7. WHEN all workers in a layer complete successfully, THE WorkManager SHALL start the workers in the next layer.
8. IF a worker in a layer fails permanently, THE WorkManager SHALL mark dependent layers as blocked and not execute them, while sibling workers in the same layer continue unaffected.

---

### Requirement 7: Error Handling and Retry Behaviour

**User Story:** As a mobile developer, I want sync workers to handle transient and permanent errors gracefully, so that temporary network or server issues trigger automatic retries while data errors are logged and skipped.

#### Acceptance Criteria

1. WHEN a worker starts and `ConnectivityHelper.isNetworkAvailable()` returns false, THE BaseSyncWorker SHALL return `Result.retry()` and WorkManager SHALL reschedule the worker with exponential backoff.
2. WHEN an `UnknownHostException`, `SocketTimeoutException`, or `IOException` is thrown during a sync call, THE BaseSyncWorker SHALL return `Result.retry()`.
3. WHEN the API returns an HTTP 4xx response for an entity, THE BaseSyncWorker SHALL log the error, skip that entity (leaving `isSynced = 0`), and continue processing the rest of the batch.
4. WHEN the API returns an HTTP 5xx response, THE BaseSyncWorker SHALL return `Result.retry()` for the entire batch without marking any entity as synced.
5. WHEN `scheduleFullSync()` is called while a `full_sync` chain is already active, THE SyncManager SHALL not enqueue a new chain, and the existing chain SHALL continue to completion.

---

### Requirement 8: Dependency Injection Wiring

**User Story:** As an Android developer, I want all new workers and services wired into the Hilt dependency graph, so that they can be instantiated by WorkManager without manual factory management.

#### Acceptance Criteria

1. THE NetworkModule SHALL provide a `@Singleton`-scoped `SyncApiService` instance using the existing Retrofit builder.
2. THE WorkerModule SHALL contain a `@Binds` entry for each of the 14 new Entity Worker factories, binding the concrete `Factory` implementation to the `WorkerAssistedFactory` interface.
3. WHEN WorkManager instantiates an Entity Worker, THE Hilt worker factory SHALL inject the correct DAO and `SyncApiService` into the worker without requiring manual factory lookup.

---

### Requirement 9: Backward Compatibility with Existing Workers

**User Story:** As an Android developer, I want the existing Invoice, InvoiceLine, Product, and User sync workers to remain structurally unchanged, so that introducing the new base class and DAG does not break existing sync behaviour.

#### Acceptance Criteria

1. THE existing sync workers (Invoice, InvoiceLine, Product, User) SHALL continue to use `lastUpdated IS NULL` as their unsynced marker and SHALL NOT be migrated to `isSynced = 0`.
2. THE existing sync workers SHALL NOT be required to extend `BaseSyncWorker`.
3. WHEN `scheduleFullSync()` is called, THE SyncManager SHALL include the existing workers in the DAG at their correct layers (Product in Layer 3, Invoice in Layer 4, InvoiceLine in Layer 5) without modifying their internal logic.
4. THE existing `SyncManager` scheduling methods SHALL continue to function correctly after `scheduleFullSync()` is added.
