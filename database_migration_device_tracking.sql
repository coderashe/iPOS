-- ============================================================================
-- Device Tracking Database Migration Script
-- Purpose: Add multi-device tracking capabilities to iPosDb
-- Date: 2026-03-10
-- ============================================================================

USE iPosDb;
GO

-- ============================================================================
-- STEP 1: Add Device Columns to Existing Tables
-- ============================================================================

PRINT 'Adding device tracking columns to existing tables...';

-- Add to Invoices table
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Invoices') AND name = 'deviceId')
BEGIN
    ALTER TABLE Invoices ADD deviceId NVARCHAR(50) NULL;
    PRINT '✓ Added deviceId to Invoices';
END
ELSE
    PRINT '- deviceId already exists in Invoices';

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Invoices') AND name = 'deviceName')
BEGIN
    ALTER TABLE Invoices ADD deviceName NVARCHAR(100) NULL;
    PRINT '✓ Added deviceName to Invoices';
END
ELSE
    PRINT '- deviceName already exists in Invoices';

-- Add to Products table
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Products') AND name = 'deviceId')
BEGIN
    ALTER TABLE Products ADD deviceId NVARCHAR(50) NULL;
    PRINT '✓ Added deviceId to Products';
END
ELSE
    PRINT '- deviceId already exists in Products';

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('Products') AND name = 'deviceName')
BEGIN
    ALTER TABLE Products ADD deviceName NVARCHAR(100) NULL;
    PRINT '✓ Added deviceName to Products';
END
ELSE
    PRINT '- deviceName already exists in Products';

-- Add to InvoiceLines table
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('InvoiceLines') AND name = 'deviceId')
BEGIN
    ALTER TABLE InvoiceLines ADD deviceId NVARCHAR(50) NULL;
    PRINT '✓ Added deviceId to InvoiceLines';
END
ELSE
    PRINT '- deviceId already exists in InvoiceLines';

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('InvoiceLines') AND name = 'deviceName')
BEGIN
    ALTER TABLE InvoiceLines ADD deviceName NVARCHAR(100) NULL;
    PRINT '✓ Added deviceName to InvoiceLines';
END
ELSE
    PRINT '- deviceName already exists in InvoiceLines';

GO

-- ============================================================================
-- STEP 2: Create Devices Table
-- ============================================================================

PRINT 'Creating Devices table...';

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Devices')
BEGIN
    CREATE TABLE Devices (
        deviceId NVARCHAR(50) PRIMARY KEY,
        deviceName NVARCHAR(100) NOT NULL,
        userId BIGINT NULL,
        storeId NVARCHAR(50) NULL,
        lastSyncAt DATETIME NULL,
        registrationDate DATETIME NOT NULL DEFAULT GETDATE(),
        isActive BIT NOT NULL DEFAULT 1,
        androidVersion INT NULL,
        deviceModel NVARCHAR(100) NULL,
        manufacturer NVARCHAR(100) NULL,
        notes NVARCHAR(500) NULL,
        createdAt DATETIME NOT NULL DEFAULT GETDATE(),
        updatedAt DATETIME NOT NULL DEFAULT GETDATE()
    );
    PRINT '✓ Devices table created';
END
ELSE
    PRINT '- Devices table already exists';

GO

-- ============================================================================
-- STEP 3: Create Indexes for Performance
-- ============================================================================

PRINT 'Creating indexes...';

-- Invoices indexes
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Invoices_DeviceId')
BEGIN
    CREATE INDEX IX_Invoices_DeviceId ON Invoices(deviceId);
    PRINT '✓ Created index IX_Invoices_DeviceId';
END
ELSE
    PRINT '- Index IX_Invoices_DeviceId already exists';

-- Products indexes
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Products_DeviceId')
BEGIN
    CREATE INDEX IX_Products_DeviceId ON Products(deviceId);
    PRINT '✓ Created index IX_Products_DeviceId';
END
ELSE
    PRINT '- Index IX_Products_DeviceId already exists';

-- InvoiceLines indexes
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_InvoiceLines_DeviceId')
BEGIN
    CREATE INDEX IX_InvoiceLines_DeviceId ON InvoiceLines(deviceId);
    PRINT '✓ Created index IX_InvoiceLines_DeviceId';
END
ELSE
    PRINT '- Index IX_InvoiceLines_DeviceId already exists';

-- Devices indexes
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Devices_StoreId')
BEGIN
    CREATE INDEX IX_Devices_StoreId ON Devices(storeId);
    PRINT '✓ Created index IX_Devices_StoreId';
END
ELSE
    PRINT '- Index IX_Devices_StoreId already exists';

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Devices_UserId')
BEGIN
    CREATE INDEX IX_Devices_UserId ON Devices(userId);
    PRINT '✓ Created index IX_Devices_UserId';
END
ELSE
    PRINT '- Index IX_Devices_UserId already exists';

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_Devices_IsActive')
BEGIN
    CREATE INDEX IX_Devices_IsActive ON Devices(isActive);
    PRINT '✓ Created index IX_Devices_IsActive';
END
ELSE
    PRINT '- Index IX_Devices_IsActive already exists';

GO

-- ============================================================================
-- STEP 4: Create Stored Procedures
-- ============================================================================

PRINT 'Creating stored procedures...';

-- Register or Update Device
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_RegisterDevice')
    DROP PROCEDURE sp_RegisterDevice;
GO

CREATE PROCEDURE sp_RegisterDevice
    @deviceId NVARCHAR(50),
    @deviceName NVARCHAR(100),
    @userId BIGINT = NULL,
    @storeId NVARCHAR(50) = NULL,
    @androidVersion INT = NULL,
    @deviceModel NVARCHAR(100) = NULL,
    @manufacturer NVARCHAR(100) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    MERGE Devices AS target
    USING (SELECT @deviceId AS deviceId) AS source
    ON target.deviceId = source.deviceId
    WHEN MATCHED THEN
        UPDATE SET 
            deviceName = @deviceName,
            userId = COALESCE(@userId, userId),
            storeId = COALESCE(@storeId, storeId),
            lastSyncAt = GETDATE(),
            androidVersion = COALESCE(@androidVersion, androidVersion),
            deviceModel = COALESCE(@deviceModel, deviceModel),
            manufacturer = COALESCE(@manufacturer, manufacturer),
            isActive = 1,
            updatedAt = GETDATE()
    WHEN NOT MATCHED THEN
        INSERT (deviceId, deviceName, userId, storeId, lastSyncAt, 
                registrationDate, androidVersion, deviceModel, manufacturer)
        VALUES (@deviceId, @deviceName, @userId, @storeId, GETDATE(), 
                GETDATE(), @androidVersion, @deviceModel, @manufacturer);
    
    SELECT * FROM Devices WHERE deviceId = @deviceId;
END;
GO

PRINT '✓ Created sp_RegisterDevice';

-- Get Active Devices by Store
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_GetActiveDevicesByStore')
    DROP PROCEDURE sp_GetActiveDevicesByStore;
GO

CREATE PROCEDURE sp_GetActiveDevicesByStore
    @storeId NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        deviceId,
        deviceName,
        userId,
        storeId,
        lastSyncAt,
        registrationDate,
        androidVersion,
        deviceModel,
        manufacturer
    FROM Devices
    WHERE storeId = @storeId 
        AND isActive = 1
    ORDER BY deviceName;
END;
GO

PRINT '✓ Created sp_GetActiveDevicesByStore';

-- Get Device Performance Report
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'sp_GetDevicePerformance')
    DROP PROCEDURE sp_GetDevicePerformance;
GO

CREATE PROCEDURE sp_GetDevicePerformance
    @startDate DATETIME,
    @endDate DATETIME,
    @storeId NVARCHAR(50) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        d.deviceId,
        d.deviceName,
        COUNT(DISTINCT i.invoiceId) AS transactionCount,
        SUM(i.totalAmount) AS totalSales,
        AVG(i.totalAmount) AS averageSale,
        MIN(i.invoiceDate) AS firstTransaction,
        MAX(i.invoiceDate) AS lastTransaction,
        d.lastSyncAt
    FROM Devices d
    LEFT JOIN Invoices i ON d.deviceId = i.deviceId
        AND i.invoiceDate BETWEEN @startDate AND @endDate
    WHERE d.isActive = 1
        AND (@storeId IS NULL OR d.storeId = @storeId)
    GROUP BY d.deviceId, d.deviceName, d.lastSyncAt
    ORDER BY totalSales DESC;
END;
GO

PRINT '✓ Created sp_GetDevicePerformance';

GO

-- ============================================================================
-- STEP 5: Create Views for Reporting
-- ============================================================================

PRINT 'Creating views...';

-- Device Sales Summary View
IF EXISTS (SELECT * FROM sys.views WHERE name = 'vw_DeviceSalesSummary')
    DROP VIEW vw_DeviceSalesSummary;
GO

CREATE VIEW vw_DeviceSalesSummary
AS
SELECT 
    d.deviceId,
    d.deviceName,
    d.storeId,
    COUNT(DISTINCT i.invoiceId) AS totalTransactions,
    SUM(i.totalAmount) AS totalSales,
    AVG(i.totalAmount) AS averageSale,
    MAX(i.invoiceDate) AS lastSaleDate,
    d.lastSyncAt
FROM Devices d
LEFT JOIN Invoices i ON d.deviceId = i.deviceId
WHERE d.isActive = 1
GROUP BY d.deviceId, d.deviceName, d.storeId, d.lastSyncAt;
GO

PRINT '✓ Created vw_DeviceSalesSummary';

GO

-- ============================================================================
-- STEP 6: Verification Queries
-- ============================================================================

PRINT '';
PRINT '============================================================================';
PRINT 'Migration Complete! Running verification...';
PRINT '============================================================================';
PRINT '';

-- Check columns
PRINT 'Checking columns...';
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS
WHERE COLUMN_NAME IN ('deviceId', 'deviceName')
    AND TABLE_NAME IN ('Invoices', 'Products', 'InvoiceLines')
ORDER BY TABLE_NAME, COLUMN_NAME;

PRINT '';
PRINT 'Checking Devices table...';
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    IS_NULLABLE,
    COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'Devices'
ORDER BY ORDINAL_POSITION;

PRINT '';
PRINT 'Checking indexes...';
SELECT 
    t.name AS TableName,
    i.name AS IndexName,
    i.type_desc AS IndexType
FROM sys.indexes i
INNER JOIN sys.tables t ON i.object_id = t.object_id
WHERE i.name LIKE '%Device%'
ORDER BY t.name, i.name;

PRINT '';
PRINT 'Checking stored procedures...';
SELECT 
    name AS ProcedureName,
    create_date AS CreatedDate,
    modify_date AS ModifiedDate
FROM sys.procedures
WHERE name LIKE 'sp_%Device%'
ORDER BY name;

PRINT '';
PRINT '============================================================================';
PRINT 'Migration Summary:';
PRINT '✓ Device columns added to Invoices, Products, InvoiceLines';
PRINT '✓ Devices table created';
PRINT '✓ Indexes created for performance';
PRINT '✓ Stored procedures created';
PRINT '✓ Views created for reporting';
PRINT '';
PRINT 'Next Steps:';
PRINT '1. Update backend API to accept deviceId and deviceName';
PRINT '2. Test device registration from Android app';
PRINT '3. Verify device data appears in database';
PRINT '4. Add device filter to dashboard';
PRINT '============================================================================';
PRINT '';

GO
