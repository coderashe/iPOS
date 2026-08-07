-- ============================================================
-- Backfill StoreId for all tables where it is NULL
-- Sets StoreId = DeviceId for legacy rows (safe to re-run)
-- ============================================================

-- Invoices (already had DeviceId)
UPDATE Invoices SET StoreId = DeviceId WHERE StoreId IS NULL AND DeviceId IS NOT NULL;

-- InvoiceLines (new DeviceId column — will be NULL for legacy rows; skip for now)
UPDATE InvoiceLines SET StoreId = DeviceId WHERE StoreId IS NULL AND DeviceId IS NOT NULL;

-- Products (already had DeviceId)
UPDATE Products SET StoreId = DeviceId WHERE StoreId IS NULL AND DeviceId IS NOT NULL;

-- Payments
UPDATE Payments SET StoreId = DeviceId WHERE StoreId IS NULL AND DeviceId IS NOT NULL;

-- Expenses
UPDATE Expenses SET StoreId = DeviceId WHERE StoreId IS NULL AND DeviceId IS NOT NULL;

-- CashFlows
UPDATE CashFlows SET StoreId = DeviceId WHERE StoreId IS NULL AND DeviceId IS NOT NULL;

-- DayReports
UPDATE DayReports SET StoreId = DeviceId WHERE StoreId IS NULL AND DeviceId IS NOT NULL;

-- TradeReceivables
UPDATE TradeReceivables SET StoreId = DeviceId WHERE StoreId IS NULL AND DeviceId IS NOT NULL;

-- DiscountsReceived
UPDATE DiscountsReceived SET StoreId = DeviceId WHERE StoreId IS NULL AND DeviceId IS NOT NULL;

-- CartItems
UPDATE CartItems SET StoreId = DeviceId WHERE StoreId IS NULL AND DeviceId IS NOT NULL;

-- StockTakes
UPDATE StockTakes SET StoreId = DeviceId WHERE StoreId IS NULL AND DeviceId IS NOT NULL;

-- Report counts for verification
SELECT 'Invoices'         AS [Table], COUNT(*) AS [NullStoreId] FROM Invoices         WHERE StoreId IS NULL;
SELECT 'InvoiceLines'     AS [Table], COUNT(*) AS [NullStoreId] FROM InvoiceLines     WHERE StoreId IS NULL;
SELECT 'Products'         AS [Table], COUNT(*) AS [NullStoreId] FROM Products         WHERE StoreId IS NULL;
SELECT 'Payments'         AS [Table], COUNT(*) AS [NullStoreId] FROM Payments         WHERE StoreId IS NULL;
SELECT 'Expenses'         AS [Table], COUNT(*) AS [NullStoreId] FROM Expenses         WHERE StoreId IS NULL;
SELECT 'CashFlows'        AS [Table], COUNT(*) AS [NullStoreId] FROM CashFlows        WHERE StoreId IS NULL;
SELECT 'DayReports'       AS [Table], COUNT(*) AS [NullStoreId] FROM DayReports       WHERE StoreId IS NULL;
SELECT 'TradeReceivables' AS [Table], COUNT(*) AS [NullStoreId] FROM TradeReceivables WHERE StoreId IS NULL;
SELECT 'DiscountsReceived'AS [Table], COUNT(*) AS [NullStoreId] FROM DiscountsReceived WHERE StoreId IS NULL;
SELECT 'CartItems'        AS [Table], COUNT(*) AS [NullStoreId] FROM CartItems        WHERE StoreId IS NULL;
SELECT 'StockTakes'       AS [Table], COUNT(*) AS [NullStoreId] FROM StockTakes       WHERE StoreId IS NULL;
