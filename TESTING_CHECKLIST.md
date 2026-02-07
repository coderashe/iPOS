# Testing Checklist for Batch Processing Improvements

## Pre-Testing Setup

- [ ] Backup your database before testing
- [ ] Clear app data to test fresh installation
- [ ] Prepare test CSV files (small, medium, large)
- [ ] Enable detailed logging in Android Studio

---

## 1. Brand Insertion Test

### Test Case 1.1: Fresh Installation
- [ ] Install app on clean device/emulator
- [ ] Launch app
- [ ] Check logs for "BrandInsert" tag
- [ ] Verify message: "Inserted X new brands"
- [ ] Expected: ~200 brands inserted in < 200ms
- [ ] Verify brands appear in database

### Test Case 1.2: Existing Brands
- [ ] Restart app (brands already exist)
- [ ] Check logs for "BrandInsert" tag
- [ ] Expected: No new brands inserted (0 inserts)
- [ ] Expected: Completes in < 100ms

### Test Case 1.3: Partial Brands
- [ ] Manually delete 50 brands from database
- [ ] Restart app
- [ ] Expected: Only 50 missing brands inserted
- [ ] Expected: Completes in < 150ms

**✅ Pass Criteria:**
- No duplicate brands created
- Fast execution (< 200ms)
- Logs show batch insert, not individual inserts

---

## 2. CSV Product Upload Test

### Test Case 2.1: Small CSV (10 products)
- [ ] Create CSV with 10 new products
- [ ] Upload via LoadStockScreen
- [ ] Check toast message: "10 new, 0 updated, 0 errors"
- [ ] Expected: Completes in < 1 second
- [ ] Verify products in database
- [ ] Verify price lists created

### Test Case 2.2: Medium CSV (100 products)
- [ ] Create CSV with 100 new products
- [ ] Upload via LoadStockScreen
- [ ] Check toast message: "100 new, 0 updated, 0 errors"
- [ ] Expected: Completes in < 3 seconds
- [ ] Verify all products inserted
- [ ] Verify all price lists created

### Test Case 2.3: Large CSV (1000 products)
- [ ] Create CSV with 1000 new products
- [ ] Upload via LoadStockScreen
- [ ] Check toast message: "1000 new, 0 updated, 0 errors"
- [ ] Expected: Completes in < 10 seconds
- [ ] Verify products inserted (check count)
- [ ] Verify price lists created (check count)

### Test Case 2.4: Update Existing Products
- [ ] Upload same CSV again (all products exist)
- [ ] Check toast message: "0 new, 1000 updated, 0 errors"
- [ ] Expected: Completes in < 10 seconds
- [ ] Verify product quantities updated
- [ ] Verify price lists updated

### Test Case 2.5: Mixed Insert/Update
- [ ] Delete 500 products from database
- [ ] Upload CSV with 1000 products
- [ ] Check toast message: "500 new, 500 updated, 0 errors"
- [ ] Expected: Completes in < 10 seconds
- [ ] Verify correct counts

### Test Case 2.6: Error Handling
- [ ] Create CSV with invalid data (missing fields)
- [ ] Upload CSV
- [ ] Check toast shows error count
- [ ] Verify valid products still inserted
- [ ] Check logs for error details

**✅ Pass Criteria:**
- Correct insert/update counts
- Fast execution (< 10 sec for 1000 items)
- No duplicate products
- Price lists match products
- Errors don't crash app

---

## 3. Invoice CSV Processing Test

### Test Case 3.1: Small Invoice CSV (10 items)
- [ ] Create invoice CSV with 10 items
- [ ] Process CSV
- [ ] Check logs: "Chunk processed with 10 products"
- [ ] Verify invoice created
- [ ] Verify invoice lines created
- [ ] Expected: Completes in < 2 seconds

### Test Case 3.2: Medium Invoice CSV (100 items)
- [ ] Create invoice CSV with 100 items
- [ ] Process CSV
- [ ] Check logs: Multiple chunks processed
- [ ] Verify invoices created
- [ ] Expected: Completes in < 5 seconds

### Test Case 3.3: Large Invoice CSV (500 items)
- [ ] Create invoice CSV with 500 items
- [ ] Process CSV
- [ ] Check logs: ~10 chunks processed (50 items each)
- [ ] Verify all invoices created
- [ ] Expected: Completes in < 15 seconds
- [ ] UI should remain responsive

**✅ Pass Criteria:**
- Chunking works (50 items per chunk)
- All invoices created correctly
- UI doesn't freeze
- Memory usage stable

---

## 4. Performance Monitoring

### Metrics to Track
- [ ] Log execution times for each operation
- [ ] Monitor memory usage during batch operations
- [ ] Check database size before/after
- [ ] Verify no memory leaks

### Expected Performance
```
Brand Insert (200 items):     < 200ms
Product Upload (100 items):   < 3 seconds
Product Upload (1000 items):  < 10 seconds
Invoice Process (100 items):  < 5 seconds
```

### Logging Template
```kotlin
val startTime = System.currentTimeMillis()
// ... operation ...
val duration = System.currentTimeMillis() - startTime
Log.d("Performance", "Operation completed in ${duration}ms")
```

---

## 5. Database Integrity Checks

### After Each Test
- [ ] Check for duplicate products
- [ ] Verify foreign key relationships
- [ ] Ensure price lists match products
- [ ] Verify invoice totals are correct
- [ ] Check for orphaned records

### SQL Queries for Verification
```sql
-- Check for duplicate products
SELECT productDescription, shoeSizeId, brandID, COUNT(*) 
FROM products 
GROUP BY productDescription, shoeSizeId, brandID 
HAVING COUNT(*) > 1;

-- Check for products without price lists
SELECT p.productId, p.productDescription 
FROM products p 
LEFT JOIN price_list pl ON p.productId = pl.productId 
WHERE pl.productId IS NULL;

-- Check for orphaned price lists
SELECT pl.productId 
FROM price_list pl 
LEFT JOIN products p ON pl.productId = p.productId 
WHERE p.productId IS NULL;
```

---

## 6. Edge Cases

### Test Case 6.1: Empty CSV
- [ ] Upload empty CSV file
- [ ] Expected: No crash, appropriate message

### Test Case 6.2: Malformed CSV
- [ ] Upload CSV with wrong format
- [ ] Expected: Error message, no crash

### Test Case 6.3: Very Large File (10,000+ items)
- [ ] Upload CSV with 10,000 products
- [ ] Monitor memory usage
- [ ] Expected: Chunking prevents memory issues
- [ ] Expected: Completes without crash

### Test Case 6.4: Concurrent Operations
- [ ] Start CSV upload
- [ ] Navigate to other screens
- [ ] Expected: Operation continues in background
- [ ] Expected: No UI freezing

---

## 7. Regression Testing

### Verify Existing Functionality Still Works
- [ ] Manual product addition (LoadStockScreen)
- [ ] Single product update
- [ ] Product deletion
- [ ] Invoice creation (normal flow)
- [ ] Reports generation
- [ ] Search functionality

---

## 8. Log Analysis

### What to Look For
- [ ] "BrandInsert: Inserted X new brands" (not individual inserts)
- [ ] "ProductUpload: Upload completed - X inserted, Y updated, Z errors"
- [ ] "debugCsv: Chunk processed with X products"
- [ ] No "Error" or "Exception" logs (unless expected)
- [ ] Batch operations complete in expected time

### Red Flags
- ❌ Individual insert/update logs in loops
- ❌ Execution times > 2x expected
- ❌ OutOfMemoryError
- ❌ Database locked errors
- ❌ Duplicate key violations

---

## 9. Final Verification

- [ ] All tests passed
- [ ] Performance meets expectations
- [ ] No crashes or errors
- [ ] Database integrity maintained
- [ ] UI remains responsive
- [ ] Memory usage acceptable

---

## 10. Rollback Plan (If Issues Found)

If critical issues are discovered:
1. Revert to previous version
2. Document the issue
3. Fix in development branch
4. Re-test before deploying

---

## Test Results Template

```
Date: ___________
Tester: ___________

Brand Insertion:
- Fresh install: ⬜ Pass ⬜ Fail (Time: ___ms)
- Existing brands: ⬜ Pass ⬜ Fail (Time: ___ms)

CSV Upload:
- 10 products: ⬜ Pass ⬜ Fail (Time: ___s)
- 100 products: ⬜ Pass ⬜ Fail (Time: ___s)
- 1000 products: ⬜ Pass ⬜ Fail (Time: ___s)

Invoice Processing:
- 10 items: ⬜ Pass ⬜ Fail (Time: ___s)
- 100 items: ⬜ Pass ⬜ Fail (Time: ___s)

Issues Found:
_________________________________
_________________________________

Overall Result: ⬜ PASS ⬜ FAIL
```
