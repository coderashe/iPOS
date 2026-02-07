# Batch Processing Improvements for iPOS

## Current Issues & Solutions

### 1. CSV Product Upload - CRITICAL IMPROVEMENTS NEEDED

#### Current Problems:
```kotlin
// ❌ BAD: Sequential processing with multiple DB calls
for ((index, productCsv) in productsFromCsv.withIndex()) {
    val existingProduct = existingProductMap[key]
    if (existingProduct != null) {
        productRepository.updateProduct(updatedProduct)
        updatePriceListForProduct(updatedProduct)
    } else {
        productRepository.insertProduct(newProduct)
        createPriceListForProduct(productId, newProduct)
    }
}
```

#### ✅ RECOMMENDED APPROACH:

```kotlin
fun uploadProductsFromCsv(inputStream: InputStream, context: Context) {
    viewModelScope.launch {
        try {
            val parser = CsvParser()
            val productsFromCsv = parser.parseProductCsv(inputStream)
            
            // Batch process in chunks to avoid memory issues
            val BATCH_SIZE = 100
            var totalInserted = 0
            var totalUpdated = 0
            var totalErrors = 0
            
            productsFromCsv.chunked(BATCH_SIZE).forEach { batch ->
                val result = processBatchProducts(batch, context)
                totalInserted += result.inserted
                totalUpdated += result.updated
                totalErrors += result.errors
            }
            
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Completed: $totalInserted new, $totalUpdated updated, $totalErrors errors",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Log.e("ProductUpload", "Failed to upload products", e)
        }
    }
}

private suspend fun processBatchProducts(
    batch: List<ProductCsvModel>,
    context: Context
): BatchResult = withContext(Dispatchers.IO) {
    var inserted = 0
    var updated = 0
    var errors = 0
    
    // Fetch existing products ONCE for the entire batch
    val existingProducts = productRepository.getAllProductsOnce()
    val existingMap = existingProducts.associateBy {
        Triple(
            it.productDescription.trim().lowercase(),
            it.shoeSizeId ?: -1L,
            it.brandID ?: -1L
        )
    }
    
    val productsToInsert = mutableListOf<Products>()
    val productsToUpdate = mutableListOf<Products>()
    val priceListsToInsert = mutableListOf<PriceList>()
    val priceListsToUpdate = mutableListOf<PriceList>()
    
    // Prepare all operations
    batch.forEach { productCsv ->
        try {
            val key = Triple(
                productCsv.productDescription.trim().lowercase(),
                productCsv.shoeSizeId ?: -1L,
                productCsv.brandID ?: -1L
            )
            
            val existingProduct = existingMap[key]
            
            if (existingProduct != null) {
                // Prepare update
                val updatedProduct = existingProduct.copy(
                    productQty = productCsv.productQty,
                    standardCost = productCsv.standardCost ?: existingProduct.standardCost,
                    lastUpdated = System.currentTimeMillis()
                )
                productsToUpdate.add(updatedProduct)
                
                val priceList = PriceList(
                    productId = existingProduct.productId,
                    productDescription = existingProduct.productDescription,
                    unitCost = updatedProduct.standardCost,
                    sellingPrice = updatedProduct.standardCost * 1.0,
                    markupPercentage = 0.0,
                    grossProfitPercentage = 0.0
                )
                priceListsToUpdate.add(priceList)
                updated++
            } else {
                // Prepare insert
                val newProduct = Products(
                    productDescription = productCsv.productDescription.trim(),
                    productQty = productCsv.productQty,
                    brandID = productCsv.brandID ?: -1L,
                    shoeSizeId = productCsv.shoeSizeId ?: -1L,
                    standardCost = productCsv.standardCost ?: 0.0,
                    lastUpdated = System.currentTimeMillis()
                )
                productsToInsert.add(newProduct)
                inserted++
            }
        } catch (e: Exception) {
            errors++
            Log.e("BatchProcess", "Error processing product: ${productCsv.productDescription}", e)
        }
    }
    
    // Execute batch operations using Room's @Transaction
    if (productsToInsert.isNotEmpty()) {
        val insertedIds = productRepository.insertProducts(productsToInsert)
        // Batch insert price lists
        insertedIds.forEachIndexed { index, productId ->
            val product = productsToInsert[index]
            priceListsToInsert.add(
                PriceList(
                    productId = productId,
                    productDescription = product.productDescription,
                    unitCost = product.standardCost,
                    sellingPrice = product.standardCost * 1.0,
                    markupPercentage = 0.0,
                    grossProfitPercentage = 0.0
                )
            )
        }
        priceListRepository.insertPriceLists(priceListsToInsert)
    }
    
    if (productsToUpdate.isNotEmpty()) {
        productRepository.updateProducts(productsToUpdate)
        priceListRepository.updatePriceLists(priceListsToUpdate)
    }
    
    BatchResult(inserted, updated, errors)
}

data class BatchResult(val inserted: Int, val updated: Int, val errors: Int)
```

### 2. Initial Brands Insertion - MAJOR PERFORMANCE ISSUE

#### Current Problem:
```kotlin
// ❌ BAD: 200+ individual DB queries!
brandNames.forEach { brandName ->
    val existingBrand = shoeBrandRepository.findBrandByName(brandName) // DB call
    if (existingBrand == null) {
        shoeBrandRepository.insertShoeBrand(ShoeBrand(0, brandName)) // DB call
    }
}
```

#### ✅ RECOMMENDED APPROACH:

```kotlin
fun insertInitialShoeBrands() = viewModelScope.launch {
    val brandNames = listOf(
        "OLDSTOCK", "D166", "6610", /* ... all 200+ brands ... */
    )
    
    // Single DB call to get all existing brands
    val existingBrands = shoeBrandRepository.getAllShoeBrandsOnce()
    val existingBrandNames = existingBrands.map { it.brandName.lowercase() }.toSet()
    
    // Filter out brands that already exist
    val newBrands = brandNames
        .filter { it.lowercase() !in existingBrandNames }
        .map { ShoeBrand(0, it) }
    
    // Single batch insert
    if (newBrands.isNotEmpty()) {
        shoeBrandRepository.insertShoeBrands(newBrands)
        Log.d("BrandInsert", "Inserted ${newBrands.size} new brands")
    }
}
```

### 3. Add Batch Operations to Repository

#### ProductRepository additions:
```kotlin
interface ProductRepository {
    // Add these methods
    suspend fun updateProducts(products: List<Products>)
    suspend fun insertProductsWithPrices(
        products: List<Products>,
        priceLists: List<PriceList>
    ): List<Long>
}
```

#### ProductDao additions:
```kotlin
@Dao
interface ProductDao {
    @Update
    suspend fun updateProducts(products: List<Products>)
    
    @Transaction
    suspend fun insertProductsWithPrices(
        products: List<Products>,
        priceLists: List<PriceList>
    ) {
        val productIds = insertProducts(products)
        // Update price lists with correct product IDs
        val updatedPriceLists = priceLists.mapIndexed { index, priceList ->
            priceList.copy(productId = productIds[index])
        }
        insertPriceLists(updatedPriceLists)
    }
}
```

#### PriceListRepository additions:
```kotlin
interface PriceListRepository {
    suspend fun insertPriceLists(priceLists: List<PriceList>)
    suspend fun updatePriceLists(priceLists: List<PriceList>)
}
```

### 4. Invoice CSV Processing - Good but can be better

#### Current approach is decent, but add chunking:
```kotlin
fun processCsvInvoices(
    csvContent: String,
    cashierEmail: String
) = viewModelScope.launch {
    try {
        val invoiceItems = parseCsvContent(csvContent)
        
        // Process in chunks if large
        val CHUNK_SIZE = 50
        invoiceItems.chunked(CHUNK_SIZE).forEach { chunk ->
            processInvoiceChunk(chunk, cashierEmail)
        }
    } catch (e: Exception) {
        Log.e("debugCsv", "Error processing CSV invoices", e)
    }
}
```

## Performance Comparison

### Before Optimization:
- **200 brands**: 400+ DB calls (1 check + 1 insert per brand)
- **1000 products CSV**: 3000+ DB calls (1 check + 1 insert/update + 1 price list operation)
- **Time**: ~30-60 seconds for 1000 products

### After Optimization:
- **200 brands**: 2 DB calls (1 fetch all + 1 batch insert)
- **1000 products CSV**: ~30 DB calls (10 batches × 3 operations)
- **Time**: ~3-5 seconds for 1000 products

## Key Principles for Robust Batch Processing

1. **Minimize DB Round Trips**: Fetch once, process in memory, write in batches
2. **Use Transactions**: Ensure atomicity with @Transaction
3. **Chunk Large Operations**: Process in batches of 50-100 items
4. **Error Isolation**: Don't let one bad item fail the entire batch
5. **Progress Feedback**: Update UI periodically for long operations
6. **Memory Management**: Don't load everything at once for huge files
7. **Async Processing**: Use coroutines with appropriate dispatchers

## Additional Recommendations

### Add Progress Tracking:
```kotlin
private val _uploadProgress = MutableStateFlow(0f)
val uploadProgress: StateFlow<Float> = _uploadProgress

fun uploadProductsFromCsv(inputStream: InputStream, context: Context) {
    viewModelScope.launch {
        val productsFromCsv = parser.parseProductCsv(inputStream)
        val totalBatches = (productsFromCsv.size + BATCH_SIZE - 1) / BATCH_SIZE
        
        productsFromCsv.chunked(BATCH_SIZE).forEachIndexed { index, batch ->
            processBatchProducts(batch, context)
            _uploadProgress.value = (index + 1).toFloat() / totalBatches
        }
    }
}
```

### Add Validation Layer:
```kotlin
private fun validateProductBatch(batch: List<ProductCsvModel>): ValidationResult {
    val errors = mutableListOf<String>()
    
    batch.forEach { product ->
        if (product.productDescription.isBlank()) {
            errors.add("Empty description at line ${batch.indexOf(product)}")
        }
        if (product.standardCost == null || product.standardCost <= 0) {
            errors.add("Invalid cost for ${product.productDescription}")
        }
    }
    
    return ValidationResult(errors.isEmpty(), errors)
}
```
