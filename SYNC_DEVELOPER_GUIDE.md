# Automatic Sync - Developer Guide

## Quick Start

### For New Developers
The app now has **automatic synchronization** built into all data repositories. You don't need to manually trigger sync operations - they happen automatically when you create or update data.

## How to Use

### Creating Data (Automatic Sync)

#### Invoices
```kotlin
// Just insert - sync happens automatically
val invoiceId = invoiceRepository.insertInvoice(invoice)
// ✅ Sync triggered immediately in background
```

#### Products
```kotlin
// Single product
val productId = productRepository.insertProduct(product)
// ✅ Sync triggered immediately

// Multiple products (batch optimized)
val ids = productRepository.insertProducts(productList)
// ✅ Batch sync triggered automatically
```

#### Invoice Lines
```kotlin
invoiceLineRepository.insertInvoiceLine(invoiceLine)
// ✅ Sync triggered immediately
```

### Updating Data (Automatic Sync)

```kotlin
// Update invoice
invoiceRepository.updateInvoice(updatedInvoice)
// ✅ Sync triggered automatically

// Update product
productRepository.updateProduct(updatedProduct)
// ✅ Sync triggered automatically

// Update invoice line
invoiceLineRepository.updateInvoiceLine(updatedLine)
// ✅ Sync triggered automatically
```

### Manual Sync (When Needed)

```kotlin
// In ViewModel or Activity
syncManager.syncAllNow()  // Sync everything immediately

// Or specific types
syncManager.scheduleInvoiceBatchSync()
syncManager.scheduleProductBatchSync()
syncManager.scheduleInvoiceLineBatchSync()
```

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    Your Code                             │
│  viewModel.insertInvoice(invoice)                        │
└────────────────────┬────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────┐
│              InvoiceRepository                           │
│  • Saves to database                                     │
│  • Calls syncManager.scheduleSingleInvoiceSync()         │
└────────────────────┬────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────┐
│                 SyncManager                              │
│  • Creates WorkRequest                                   │
│  • Handles batching logic                                │
│  • Enqueues with WorkManager                             │
└────────────────────┬────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────┐
│              WorkManager + Workers                       │
│  • Waits for network                                     │
│  • Executes sync                                         │
│  • Retries on failure                                    │
└─────────────────────────────────────────────────────────┘
```

## Common Scenarios

### Scenario 1: Creating a New Invoice
```kotlin
viewModelScope.launch {
    val invoice = Invoice(
        invoiceDate = System.currentTimeMillis(),
        totalAmount = 100.0,
        status = "PAID",
        currencyCode = "USD"
    )
    
    // Just insert - sync is automatic
    val invoiceId = invoiceRepository.insertInvoice(invoice)
    
    // No need to call sync manually!
    // The repository handles it automatically
}
```

### Scenario 2: Bulk Product Import
```kotlin
viewModelScope.launch {
    val products = csvParser.parseProducts(csvFile)
    
    // Batch insert - optimized sync
    val insertedIds = productRepository.insertProducts(products)
    
    // Sync is triggered automatically
    // Uses batch sync for efficiency
}
```

### Scenario 3: Updating Product Quantity
```kotlin
viewModelScope.launch {
    val product = productRepository.getProductById(productId)
    val updatedProduct = product.copy(productQty = newQuantity)
    
    // Update - sync happens automatically
    productRepository.updateProduct(updatedProduct)
}
```

### Scenario 4: Offline Operation
```kotlin
// User is offline
val invoice = Invoice(...)
invoiceRepository.insertInvoice(invoice)
// ✅ Saved locally, queued for sync

// When network returns
// ✅ Automatically syncs without user action
```

## Best Practices

### ✅ DO

1. **Just use repository methods normally**
   ```kotlin
   invoiceRepository.insertInvoice(invoice)
   // Sync is automatic
   ```

2. **Trust the automatic sync**
   - No need to manually trigger sync after every operation
   - The system handles it intelligently

3. **Use batch operations for multiple items**
   ```kotlin
   productRepository.insertProducts(productList)
   // More efficient than individual inserts
   ```

4. **Check sync status in UI**
   ```kotlin
   // Use existing SyncStatusCard component
   SyncStatusCard(viewModel = viewModel)
   ```

### ❌ DON'T

1. **Don't manually schedule sync after repository operations**
   ```kotlin
   // ❌ BAD - Redundant
   invoiceRepository.insertInvoice(invoice)
   syncManager.scheduleSingleInvoiceSync(invoiceId)  // Already done!
   ```

2. **Don't bypass repositories**
   ```kotlin
   // ❌ BAD - Sync won't trigger
   invoiceDao.insertInvoice(invoice)  // Direct DAO access
   
   // ✅ GOOD - Use repository
   invoiceRepository.insertInvoice(invoice)
   ```

3. **Don't worry about network status**
   ```kotlin
   // ❌ BAD - Unnecessary check
   if (isNetworkAvailable()) {
       invoiceRepository.insertInvoice(invoice)
   }
   
   // ✅ GOOD - WorkManager handles it
   invoiceRepository.insertInvoice(invoice)
   ```

## Debugging Sync Issues

### Check Logs
```kotlin
// Filter by these tags in Logcat
"SyncManager"
"InvoiceRepositoryImpl"
"ProductRepositoryImpl"
"InvoiceLineRepository"
"SyncInvoiceWorker"
"SyncProductWorker"
"SyncInvoiceLineWorker"
```

### Common Log Messages
```
✅ Success:
"Auto-sync scheduled for new invoice: 123"
"Successfully synced invoice 123"

⚠️ Retry:
"Failed to sync invoice: 500"
"Retrying sync with exponential backoff"

❌ Failure:
"Invalid invoice ID provided for sync"
"Invoice 123 has null/empty status, skipping sync"
```

### Verify Sync Status
```kotlin
// Check unsynced count
viewModelScope.launch {
    val unsyncedCount = invoiceRepository.getUnsyncedCount().first()
    Log.d("SyncDebug", "Unsynced invoices: $unsyncedCount")
}
```

### Monitor WorkManager
```kotlin
// Check work status
workManager.getWorkInfosForUniqueWork("sync_invoice_123")
    .get()
    .forEach { workInfo ->
        Log.d("SyncDebug", "Work state: ${workInfo.state}")
    }
```

## Configuration

### Adjust Batch Threshold
```kotlin
// In SyncManager.kt
private const val BATCH_SYNC_THRESHOLD = 10  // Change this value
```

### Adjust Retry Delay
```kotlin
// In SyncManager.kt
private const val SYNC_BACKOFF_DELAY = 15L  // Minutes
```

### Network Constraints
```kotlin
// In SyncManager.kt - setConstraints()
.setRequiredNetworkType(NetworkType.CONNECTED)  // Any network
// or
.setRequiredNetworkType(NetworkType.UNMETERED)  // WiFi only
```

## Testing

### Unit Tests
```kotlin
@Test
fun `insertInvoice triggers sync`() = runTest {
    // Arrange
    val invoice = createTestInvoice()
    val syncManager = mockk<SyncManager>(relaxed = true)
    val repository = InvoiceRepositoryImpl(dao, currencyDao, api, syncManager)
    
    // Act
    repository.insertInvoice(invoice)
    
    // Assert
    verify { syncManager.scheduleSingleInvoiceSync(any()) }
}
```

### Integration Tests
```kotlin
@Test
fun `offline insert syncs when network returns`() = runTest {
    // 1. Turn off network
    // 2. Insert invoice
    // 3. Verify saved locally
    // 4. Turn on network
    // 5. Wait for sync
    // 6. Verify synced to server
}
```

## Troubleshooting

### Issue: Items not syncing
**Check:**
1. Network connectivity
2. API endpoint availability
3. Item validation (required fields)
4. WorkManager logs

### Issue: Duplicate sync requests
**Cause:** Manually triggering sync after repository operations
**Solution:** Remove manual sync calls, let repositories handle it

### Issue: Slow sync
**Check:**
1. Network speed
2. Batch size (too large?)
3. API response time
4. Number of pending items

### Issue: Sync fails repeatedly
**Check:**
1. API error responses
2. Data validation errors
3. Authentication issues
4. Server availability

## Migration from Manual Sync

### Old Code
```kotlin
// ❌ Old way
val invoiceId = invoiceRepository.insertInvoice(invoice)
viewModel.scheduleSingleInvoiceSync(invoice)  // Manual
```

### New Code
```kotlin
// ✅ New way
val invoiceId = invoiceRepository.insertInvoice(invoice)
// That's it! Sync is automatic
```

## FAQ

**Q: Do I need to call sync after every insert?**
A: No! Sync is automatic. Just use repository methods normally.

**Q: What if the user is offline?**
A: Data is saved locally and automatically syncs when network returns.

**Q: How do I know if sync succeeded?**
A: Check logs or observe the `SyncStatusCard` UI component.

**Q: Can I disable automatic sync?**
A: Not recommended, but you could modify repositories to skip `syncManager` calls.

**Q: What happens if sync fails?**
A: WorkManager retries with exponential backoff (15 min intervals).

**Q: How do I force immediate sync?**
A: Call `syncManager.syncAllNow()` in your ViewModel.

**Q: Does this drain battery?**
A: No - WorkManager respects network constraints and system battery optimization.

## Support

For issues or questions:
1. Check logs with appropriate tags
2. Review this guide
3. Check `SYNC_IMPLEMENTATION_SUMMARY.md` for architecture details
4. Contact the development team

---

**Remember:** Sync is now automatic. Just use repositories normally and let the system handle synchronization! 🚀
