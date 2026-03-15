# Multi-Device Tracking Implementation

## Overview

This implementation enables tracking which POS device created or modified data, allowing device-level reporting and analytics on the dashboard.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Android Device                            │
│  ┌────────────────────────────────────────────────────┐    │
│  │           DeviceManager                             │    │
│  │  - deviceId: "abc123xyz789"                         │    │
│  │  - deviceName: "Main Counter"                       │    │
│  │  - isRegistered: true                               │    │
│  └────────────────────────────────────────────────────┘    │
│                         ↓                                    │
│  ┌────────────────────────────────────────────────────┐    │
│  │         Repositories (Invoice/Product)              │    │
│  │  - Attach deviceId & deviceName to all data         │    │
│  └────────────────────────────────────────────────────┘    │
│                         ↓                                    │
│  ┌────────────────────────────────────────────────────┐    │
│  │              Sync Workers                           │    │
│  │  - Include device metadata in DTOs                  │    │
│  └────────────────────────────────────────────────────┘    │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP POST with device metadata
                           ↓
┌─────────────────────────────────────────────────────────────┐
│                  SQL Server (iPosDb)                         │
│  ┌────────────────────────────────────────────────────┐    │
│  │  Invoices Table                                     │    │
│  │  - invoiceId                                        │    │
│  │  - deviceId ← NEW                                   │    │
│  │  - deviceName ← NEW                                 │    │
│  │  - userId                                           │    │
│  │  - storeId                                          │    │
│  │  - totalAmount                                      │    │
│  │  - ...                                              │    │
│  └────────────────────────────────────────────────────┘    │
│  ┌────────────────────────────────────────────────────┐    │
│  │  Products Table                                     │    │
│  │  - productId                                        │    │
│  │  - deviceId ← NEW                                   │    │
│  │  - deviceName ← NEW                                 │    │
│  │  - ...                                              │    │
│  └────────────────────────────────────────────────────┘    │
│  ┌────────────────────────────────────────────────────┐    │
│  │  Devices Table ← NEW                                │    │
│  │  - deviceId (PK)                                    │    │
│  │  - deviceName                                       │    │
│  │  - userId                                           │    │
│  │  - storeId                                          │    │
│  │  - lastSyncAt                                       │    │
│  │  - isActive                                         │    │
│  │  - registrationDate                                 │    │
│  └────────────────────────────────────────────────────┘    │
└──────────────────────────┬──────────────────────────────────┘
                           │ Query with device filter
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              Dashboard (Angular)                             │
│  ┌────────────────────────────────────────────────────┐    │
│  │  Device Filter Dropdown                             │    │
│  │  ☐ All Devices                                      │    │
│  │  ☑ Main Counter                                     │    │
│  │  ☐ Mobile POS 1                                     │    │
│  │  ☐ Checkout 2                                       │    │
│  └────────────────────────────────────────────────────┘    │
│  ┌────────────────────────────────────────────────────┐    │
│  │  Sales Report                                       │    │
│  │  Device: Main Counter                               │    │
│  │  Total Sales: $5,234.50                             │    │
│  │  Transactions: 45                                   │    │
│  └────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

## Components

### 1. DeviceManager (Android)

**Location:** `app/src/main/java/com/example/learncompose303/app/core/DeviceManager.kt`

**Purpose:** Manages device identification and metadata

**Key Methods:**
```kotlin
getDeviceId(): String          // Get unique device ID (Android ANDROID_ID)
getDeviceName(): String        // Get user-friendly name
setDeviceName(name: String)    // Set custom device name
isDeviceRegistered(): Boolean  // Check registration status
markDeviceAsRegistered()       // Mark as registered
getDeviceInfo(): DeviceInfo    // Get complete device info
```

**Device ID Source:**
- Uses `Settings.Secure.ANDROID_ID`
- Unique per device
- Survives app reinstall
- No permissions required
- Resets only on factory reset

**Storage:**
- SharedPreferences (`device_prefs`)
- Keys: `device_id`, `device_name`, `device_registered`, `registration_date`

### 2. Updated DTOs

**InvoiceDto:**
```kotlin
data class InvoiceDto(
    // ... existing fields
    val deviceId: String? = null,
    val deviceName: String? = null
)
```

**ProductDto:**
```kotlin
data class ProductDto(
    // ... existing fields
    val deviceId: String? = null,
    val deviceName: String? = null
)
```

**Extension Functions:**
```kotlin
fun Invoice.toDto(deviceId: String? = null, deviceName: String? = null): InvoiceDto
fun Products.toDto(deviceId: String? = null, deviceName: String? = null): ProductDto
```

### 3. Updated Workers

**SyncInvoiceWorker:**
```kotlin
@HiltWorker
class SyncInvoiceWorker @AssistedInject constructor(
    // ... existing dependencies
    private val deviceManager: DeviceManager
) {
    private suspend fun syncSingleInvoice(invoiceId: Long) {
        val invoiceDto = invoice.toDto(
            deviceId = deviceManager.getDeviceId(),
            deviceName = deviceManager.getDeviceName()
        )
        // Sync to server
    }
}
```

**SyncProductWorker:**
```kotlin
@HiltWorker
class SyncProductWorker @AssistedInject constructor(
    // ... existing dependencies
    private val deviceManager: DeviceManager
) {
    private suspend fun syncSingleProduct(productId: Long) {
        val productDto = product.toDto(
            deviceId = deviceManager.getDeviceId(),
            deviceName = deviceManager.getDeviceName()
        )
        // Sync to server
    }
}
```

### 4. Device Registration Screen

**Location:** `app/src/main/java/com/example/learncompose303/screens/DeviceRegistrationScreen.kt`

**Purpose:** First-time device setup

**Features:**
- Shows device ID
- Allows user to set friendly name
- Suggestions: "Main Counter", "Mobile POS 1", etc.
- Marks device as registered

**Usage:**
```kotlin
if (!deviceManager.isDeviceRegistered()) {
    DeviceRegistrationScreen(
        deviceManager = deviceManager,
        onRegistrationComplete = { /* Navigate to main screen */ }
    )
}
```

## Database Changes

### SQL Server Schema Updates

#### 1. Add Columns to Existing Tables

```sql
-- Add device tracking to Invoices
ALTER TABLE Invoices 
ADD deviceId NVARCHAR(50) NULL,
    deviceName NVARCHAR(100) NULL;

-- Add device tracking to Products
ALTER TABLE Products 
ADD deviceId NVARCHAR(50) NULL,
    deviceName NVARCHAR(100) NULL;

-- Add device tracking to InvoiceLines
ALTER TABLE InvoiceLines 
ADD deviceId NVARCHAR(50) NULL,
    deviceName NVARCHAR(100) NULL;

-- Add indexes for performance
CREATE INDEX IX_Invoices_DeviceId ON Invoices(deviceId);
CREATE INDEX IX_Products_DeviceId ON Products(deviceId);
CREATE INDEX IX_InvoiceLines_DeviceId ON InvoiceLines(deviceId);
```

#### 2. Create Devices Table

```sql
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
    
    CONSTRAINT FK_Devices_Users FOREIGN KEY (userId) REFERENCES Users(userId),
    CONSTRAINT FK_Devices_Stores FOREIGN KEY (storeId) REFERENCES Stores(storeId)
);

-- Index for queries
CREATE INDEX IX_Devices_StoreId ON Devices(storeId);
CREATE INDEX IX_Devices_UserId ON Devices(userId);
CREATE INDEX IX_Devices_IsActive ON Devices(isActive);
```

#### 3. Device Registration Stored Procedure

```sql
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
    
    -- Insert or update device
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
            isActive = 1
    WHEN NOT MATCHED THEN
        INSERT (deviceId, deviceName, userId, storeId, lastSyncAt, 
                registrationDate, androidVersion, deviceModel, manufacturer)
        VALUES (@deviceId, @deviceName, @userId, @storeId, GETDATE(), 
                GETDATE(), @androidVersion, @deviceModel, @manufacturer);
    
    SELECT * FROM Devices WHERE deviceId = @deviceId;
END;
```

## Backend API Updates

### 1. Update DTOs (C#)

```csharp
// InvoiceDto.cs
public class InvoiceDto
{
    // ... existing properties
    public string? DeviceId { get; set; }
    public string? DeviceName { get; set; }
}

// ProductDto.cs
public class ProductDto
{
    // ... existing properties
    public string? DeviceId { get; set; }
    public string? DeviceName { get; set; }
}
```

### 2. Update Controllers

```csharp
// InvoicesController.cs
[HttpPost]
public async Task<ActionResult<InvoiceDto>> CreateInvoice(InvoiceDto invoiceDto)
{
    // Auto-register device if not exists
    if (!string.IsNullOrEmpty(invoiceDto.DeviceId))
    {
        await _deviceService.RegisterOrUpdateDevice(
            invoiceDto.DeviceId, 
            invoiceDto.DeviceName
        );
    }
    
    // Save invoice with device info
    var invoice = await _invoiceService.CreateInvoice(invoiceDto);
    return Ok(invoice);
}
```

### 3. Device Service

```csharp
// IDeviceService.cs
public interface IDeviceService
{
    Task<Device> RegisterOrUpdateDevice(string deviceId, string deviceName);
    Task<List<Device>> GetActiveDevices(string storeId);
    Task<Device> GetDeviceById(string deviceId);
    Task DeactivateDevice(string deviceId);
}

// DeviceService.cs
public class DeviceService : IDeviceService
{
    private readonly IDbConnection _db;
    
    public async Task<Device> RegisterOrUpdateDevice(string deviceId, string deviceName)
    {
        return await _db.QueryFirstOrDefaultAsync<Device>(
            "sp_RegisterDevice",
            new { deviceId, deviceName },
            commandType: CommandType.StoredProcedure
        );
    }
    
    public async Task<List<Device>> GetActiveDevices(string storeId)
    {
        return (await _db.QueryAsync<Device>(
            "SELECT * FROM Devices WHERE storeId = @storeId AND isActive = 1",
            new { storeId }
        )).ToList();
    }
}
```

## Dashboard Integration

### 1. Device Filter Component (Angular)

```typescript
// device-filter.component.ts
export class DeviceFilterComponent implements OnInit {
  devices: Device[] = [];
  selectedDeviceId: string | null = null;
  
  constructor(private deviceService: DeviceService) {}
  
  ngOnInit() {
    this.loadDevices();
  }
  
  loadDevices() {
    this.deviceService.getActiveDevices().subscribe(devices => {
      this.devices = devices;
    });
  }
  
  onDeviceChange(deviceId: string) {
    this.selectedDeviceId = deviceId;
    // Emit event to parent component
  }
}
```

```html
<!-- device-filter.component.html -->
<mat-form-field>
  <mat-label>Filter by Device</mat-label>
  <mat-select [(value)]="selectedDeviceId" (selectionChange)="onDeviceChange($event.value)">
    <mat-option [value]="null">All Devices</mat-option>
    <mat-option *ngFor="let device of devices" [value]="device.deviceId">
      {{ device.deviceName }}
    </mat-option>
  </mat-select>
</mat-form-field>
```

### 2. Updated Report Queries

```typescript
// sales-report.service.ts
getSalesReport(params: {
  startDate: Date;
  endDate: Date;
  deviceId?: string;
  storeId?: string;
}): Observable<SalesReport> {
  return this.http.get<SalesReport>('/api/reports/sales', { params });
}
```

```sql
-- Backend query with device filter
SELECT 
    i.deviceId,
    i.deviceName,
    COUNT(*) as transactionCount,
    SUM(i.totalAmount) as totalSales,
    AVG(i.totalAmount) as averageSale
FROM Invoices i
WHERE i.invoiceDate BETWEEN @startDate AND @endDate
    AND (@deviceId IS NULL OR i.deviceId = @deviceId)
    AND (@storeId IS NULL OR i.storeId = @storeId)
GROUP BY i.deviceId, i.deviceName
ORDER BY totalSales DESC;
```

### 3. Device Performance Dashboard

```typescript
// device-performance.component.ts
export class DevicePerformanceComponent {
  deviceMetrics: DeviceMetric[] = [];
  
  loadDeviceMetrics() {
    this.reportService.getDevicePerformance().subscribe(metrics => {
      this.deviceMetrics = metrics;
    });
  }
}

interface DeviceMetric {
  deviceId: string;
  deviceName: string;
  totalSales: number;
  transactionCount: number;
  averageSale: number;
  lastSyncAt: Date;
}
```

## Usage Flow

### First-Time Setup

1. User installs app
2. App generates device ID (ANDROID_ID)
3. Device registration screen appears
4. User enters device name (e.g., "Main Counter")
5. Device info saved locally and marked as registered
6. On first sync, device info sent to server
7. Server creates device record in Devices table

### Normal Operation

1. User creates invoice/product
2. Repository attaches device ID & name
3. Sync worker includes device metadata in DTO
4. Server receives and stores with device info
5. Dashboard can filter reports by device

### Dashboard Reporting

1. Admin opens dashboard
2. Selects device from dropdown
3. Reports filtered by selected device
4. Shows device-specific metrics

## Testing

### Android App Testing

```kotlin
// Test device ID generation
@Test
fun testDeviceIdGeneration() {
    val deviceId = deviceManager.getDeviceId()
    assertNotNull(deviceId)
    assertTrue(deviceId.isNotEmpty())
}

// Test device name persistence
@Test
fun testDeviceNamePersistence() {
    deviceManager.setDeviceName("Test Device")
    assertEquals("Test Device", deviceManager.getDeviceName())
}

// Test device registration
@Test
fun testDeviceRegistration() {
    assertFalse(deviceManager.isDeviceRegistered())
    deviceManager.markDeviceAsRegistered()
    assertTrue(deviceManager.isDeviceRegistered())
}
```

### API Testing

```bash
# Test device registration
curl -X POST http://localhost:5249/api/devices/register \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": "abc123xyz789",
    "deviceName": "Main Counter",
    "storeId": "STORE001"
  }'

# Test invoice creation with device info
curl -X POST http://localhost:5249/api/invoices \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "totalAmount": 100.00,
    "deviceId": "abc123xyz789",
    "deviceName": "Main Counter"
  }'

# Test device-filtered report
curl "http://localhost:5249/api/reports/sales?deviceId=abc123xyz789&startDate=2024-01-01&endDate=2024-12-31"
```

## Benefits

### For Business Owners
- Track performance by device/location
- Identify high-performing devices
- Monitor device usage patterns
- Detect inactive devices

### For Managers
- Compare device performance
- Optimize device placement
- Track employee productivity (if tied to user)
- Identify training needs

### For IT/Support
- Track device health
- Monitor sync status
- Identify problematic devices
- Plan device upgrades

## Security Considerations

### Device ID
- Not personally identifiable
- Cannot be used to track users across apps
- Compliant with GDPR/CCPA
- No special permissions required

### Data Privacy
- Device name is user-controlled
- No sensitive hardware info collected
- Device ID changes on factory reset
- Can be anonymized in reports

## Troubleshooting

### Device ID is null
- Fallback ID generated automatically
- Check logs for "ANDROID_ID unavailable"
- Verify device is not rooted/modified

### Device not appearing in dashboard
- Check if device has synced data
- Verify device registration API call
- Check Devices table in database

### Multiple devices with same name
- Device ID is unique identifier
- Device name is just for display
- Dashboard should show both name and ID

## Future Enhancements

1. **Device Groups**: Group devices by location/type
2. **Device Alerts**: Alert when device hasn't synced
3. **Device Analytics**: Track device uptime, sync frequency
4. **Device Transfer**: Transfer device to different user/store
5. **Device Deactivation**: Soft-delete for lost/stolen devices

## Summary

This implementation provides:
- ✅ Unique device identification
- ✅ User-friendly device naming
- ✅ Automatic device registration
- ✅ Device-level reporting
- ✅ Privacy-compliant tracking
- ✅ No permissions required
- ✅ Survives app reinstall
- ✅ Easy dashboard integration

**Status:** ✅ Fully Implemented - Ready for Testing
