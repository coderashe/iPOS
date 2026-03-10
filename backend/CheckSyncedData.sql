-- Check synced InvoiceLines
SELECT TOP 10 * FROM InvoiceLines ORDER BY Id DESC;

-- Check synced Invoices with DeviceID
SELECT * FROM Invoices ORDER BY InvoiceId DESC;

-- Check synced Products
SELECT TOP 10 * FROM Products ORDER BY Id DESC;

-- Check synced Users
SELECT * FROM Users;

-- Check synced BusinessPerformances
SELECT * FROM BusinessPerformances ORDER BY Id DESC;

-- Count records in each table
SELECT 'InvoiceLines' AS TableName, COUNT(*) AS RecordCount FROM InvoiceLines
UNION ALL
SELECT 'Invoices', COUNT(*) FROM Invoices
UNION ALL
SELECT 'Products', COUNT(*) FROM Products
UNION ALL
SELECT 'Users', COUNT(*) FROM Users
UNION ALL
SELECT 'BusinessPerformances', COUNT(*) FROM BusinessPerformances;
