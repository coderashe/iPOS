-- Drop and recreate Invoices table without IDENTITY on InvoiceId
DROP TABLE IF EXISTS [Invoices];

CREATE TABLE [Invoices] (
    [InvoiceId] bigint NOT NULL,
    [CustomerId] bigint NOT NULL,
    [TotalAmount] float NOT NULL,
    [InvoiceDate] bigint NOT NULL,
    [DueDate] bigint NULL,
    [TheStatus] nvarchar(max) NOT NULL,
    [CurrencyCode] nvarchar(max) NOT NULL,
    [LastUpdated] bigint NULL,
    [IsArchived] bit NOT NULL,
    [DeviceId] nvarchar(max) NULL,
    [DeviceName] nvarchar(max) NULL,
    CONSTRAINT [PK_Invoices] PRIMARY KEY ([InvoiceId])
);
