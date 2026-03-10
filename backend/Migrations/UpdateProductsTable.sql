-- Drop foreign key constraints first
IF EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_OrderItems_Products_ProductId')
    ALTER TABLE [OrderItems] DROP CONSTRAINT [FK_OrderItems_Products_ProductId];

-- Drop and recreate Products table with updated schema
DROP TABLE IF EXISTS [Products];

CREATE TABLE [Products] (
    [Id] bigint NOT NULL,
    [Name] nvarchar(max) NOT NULL,
    [ProductCode] nvarchar(max) NOT NULL,
    [Price] decimal(18,2) NOT NULL,
    [CurrentStock] float NULL,
    [BrandId] bigint NULL,
    [ShoeSizeId] bigint NULL,
    [Saleable] bit NOT NULL,
    [Purchasable] bit NOT NULL,
    [AverageCost] float NOT NULL,
    [StandardCost] float NOT NULL,
    [SalesTaxCode] nvarchar(max) NULL,
    [InclusiveTaxed] bit NOT NULL,
    [ImageUri] nvarchar(max) NULL,
    [UnitCost] float NOT NULL,
    [MarkupPercentage] float NOT NULL,
    [LastUpdated] bigint NULL,
    [DeviceId] nvarchar(max) NULL,
    [DeviceName] nvarchar(max) NULL,
    CONSTRAINT [PK_Products] PRIMARY KEY ([Id])
);
