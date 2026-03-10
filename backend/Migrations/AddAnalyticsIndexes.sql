-- Analytics Dashboard Performance Indexes
-- Created: 2026-03-10
-- Purpose: Optimize query performance for analytics endpoints

-- InvoiceLines table indexes for date-based queries
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_InvoiceLines_InvoiceDate' AND object_id = OBJECT_ID('InvoiceLines'))
BEGIN
    CREATE INDEX IX_InvoiceLines_InvoiceDate ON InvoiceLines(InvoiceDate);
END
GO

-- InvoiceLines table index for product-based queries
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_InvoiceLines_ProductID' AND object_id = OBJECT_ID('InvoiceLines'))
BEGIN
    CREATE INDEX IX_InvoiceLines_ProductID ON InvoiceLines(ProductID);
END
GO

-- InvoiceLines table index for customer-based queries
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_InvoiceLines_CustomerID' AND object_id = OBJECT_ID('InvoiceLines'))
BEGIN
    CREATE INDEX IX_InvoiceLines_CustomerID ON InvoiceLines(CustomerID);
END
GO

-- InvoiceLines table index for cashier performance queries
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_InvoiceLines_Cashier' AND object_id = OBJECT_ID('InvoiceLines'))
BEGIN
    CREATE INDEX IX_InvoiceLines_Cashier ON InvoiceLines(Cashier);
END
GO

-- Products table index for stock level queries
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Products_CurrentStock' AND object_id = OBJECT_ID('Products'))
BEGIN
    CREATE INDEX IX_Products_CurrentStock ON Products(CurrentStock);
END
GO

-- Composite index for common date + status queries
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_InvoiceLines_Date_Status' AND object_id = OBJECT_ID('InvoiceLines'))
BEGIN
    CREATE INDEX IX_InvoiceLines_Date_Status ON InvoiceLines(InvoiceDate, InvoiceStatus);
END
GO

PRINT 'Analytics indexes created successfully';
