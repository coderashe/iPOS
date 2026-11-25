package com.ipos.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ipos.data.data_source.AppSettingsDao
import com.ipos.data.data_source.CartItemDao
import com.ipos.data.data_source.CashFlowDao
import com.ipos.data.data_source.CurrencyDao
import com.ipos.data.data_source.CustomerDao
import com.ipos.data.data_source.DayReportsDao
import com.ipos.data.data_source.DiscountsReceivedDao
import com.ipos.data.data_source.ExpenseDao
import com.ipos.data.data_source.FailedInvoiceDao
import com.ipos.data.data_source.InvoiceDao
import com.ipos.data.data_source.InvoiceItemDao
import com.ipos.data.data_source.InvoiceLineDao
import com.ipos.data.data_source.ShoeBrandDao
import com.ipos.data.data_source.OrderDao
import com.ipos.data.data_source.PaymentDao
import com.ipos.data.data_source.PriceListDao
import com.ipos.data.data_source.ProductDao
import com.ipos.data.data_source.ProductDayReportDao
import com.ipos.data.data_source.ShoeSizeDao
import com.ipos.data.data_source.StockTakeDao
import com.ipos.data.data_source.StockTakeSessionDao
import com.ipos.data.data_source.TradeReceivablesDao
import com.ipos.data.data_source.DeviceInfoDao
import com.ipos.data.entities.DeviceInfo
import com.ipos.data.data_source.UserDao
import com.ipos.data.entities.AppSettings
import com.ipos.data.entities.CartItem
import com.ipos.data.entities.Currency
import com.ipos.data.entities.Customer
import com.ipos.data.entities.DayReports
import com.ipos.data.entities.DiscountsReceived
import com.ipos.data.entities.Expense
import com.ipos.data.entities.FailedInvoiceItem
import com.ipos.data.entities.Invoice
import com.ipos.data.entities.InvoiceItem
import com.ipos.data.entities.InvoiceLine
import com.ipos.data.entities.Order
import com.ipos.data.entities.Payment
import com.ipos.data.entities.PriceList
import com.ipos.data.entities.Products
import com.ipos.data.entities.ProductDayReport
import com.ipos.data.entities.ShoeBrand
import com.ipos.data.entities.ShoeSize
import com.ipos.data.entities.StockTake
import com.ipos.data.entities.StockTakeSession
import com.ipos.data.entities.CashFlow
import com.ipos.data.entities.TradeReceivables
import com.ipos.data.entities.User

@Database(
    entities = [
        Customer::class,
        Invoice::class, InvoiceItem::class, InvoiceLine::class,
        Payment::class, Products::class, CartItem::class,
        Order::class, PriceList::class, ProductDayReport::class, DayReports::class,
        User::class, Expense::class,
        TradeReceivables::class, DiscountsReceived::class, Currency::class,
        CashFlow::class, ShoeBrand::class, ShoeSize::class, StockTake::class, StockTakeSession::class,
        FailedInvoiceItem::class,
        DeviceInfo::class,
        AppSettings::class
    ],
    version = 14,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAOs
    abstract fun invoiceDao(): InvoiceDao
    abstract fun customerDao(): CustomerDao
    abstract fun invoiceItemDao(): InvoiceItemDao
    abstract fun paymentDao(): PaymentDao
    abstract fun productDao(): ProductDao
    abstract fun cartItemDao(): CartItemDao
    abstract fun invoiceLineDao(): InvoiceLineDao
    abstract fun orderDao(): OrderDao
    abstract fun pricelistDao(): PriceListDao
    abstract fun productDayReportDao(): ProductDayReportDao
    abstract fun dayReportsDao(): DayReportsDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun userDao(): UserDao
    abstract fun shoeBrandDao(): ShoeBrandDao
    abstract fun shoeSizeDao(): ShoeSizeDao
    abstract fun stockTakeDao(): StockTakeDao
    abstract fun stockTakeSessionDao(): StockTakeSessionDao
    abstract fun failedInvoiceDao(): FailedInvoiceDao

    // New DAOs
    abstract fun tradeReceivablesDao(): TradeReceivablesDao
    abstract fun discountsReceivedDao(): DiscountsReceivedDao
    abstract fun cashFlowDao(): CashFlowDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun deviceInfoDao(): DeviceInfoDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        const val DATABASE_NAME = "ipos_db"
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `stock_take` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `productId` INTEGER NOT NULL,
                `recordedStock` INTEGER NOT NULL,
                `countedStock` REAL NOT NULL,
                `variance` REAL NOT NULL,
                `varianceReason` TEXT,
                `dateRecorded` INTEGER NOT NULL,
                `recordedBy` TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE Products ADD COLUMN lastUpdated INTEGER")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE invoice_lines ADD COLUMN synced INTEGER NOT NULL DEFAULT 0")
    }
}
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `failed_invoices` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `rowIndex` INTEGER NOT NULL,
                `brand` TEXT NOT NULL,
                `size` REAL NOT NULL,
                `description` TEXT NOT NULL,
                `quantity` INTEGER NOT NULL,
                `amountTendered` REAL NOT NULL,
                `customerId` INTEGER NOT NULL,
                `failureReason` TEXT NOT NULL,
                `timestamp` INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add suggestedMatches column to failed_invoices table
        db.execSQL("ALTER TABLE failed_invoices ADD COLUMN suggestedMatches TEXT")
    }
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add deviceId and deviceName columns to invoice table
        db.execSQL("ALTER TABLE invoice ADD COLUMN deviceId TEXT")
        db.execSQL("ALTER TABLE invoice ADD COLUMN deviceName TEXT")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Add isSynced to every entity that needs it
        db.execSQL("ALTER TABLE invoice ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE Products ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE user ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE customer ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE payment ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE cart_items ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE invoiceitem ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE price_list ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE expenses ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE day_reports ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE product_day_reports ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE currencies ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE cashflow ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE tradereceivables ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE discountsreceived ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE shoeBrand ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE shoe_sizes ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE stock_take ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE failed_invoices ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `device_info` (
                `deviceId` TEXT NOT NULL PRIMARY KEY,
                `deviceName` TEXT NOT NULL,
                `storeId` TEXT NOT NULL,
                `lastSeen` INTEGER NOT NULL,
                `isSynced` INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        listOf(
            "orders", "expenses", "cashflow", "stock_take",
            "day_reports", "payment", "tradereceivables", "discountsreceived"
        ).forEach { table ->
            db.execSQL("ALTER TABLE `$table` ADD COLUMN deviceId TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `$table` ADD COLUMN storeId TEXT NOT NULL DEFAULT ''")
        }
        // invoice already has deviceId — just add storeId
        db.execSQL("ALTER TABLE `invoice` ADD COLUMN storeId TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create the stock_take_session table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `stock_take_session` (
                `sessionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `startedAt` INTEGER NOT NULL,
                `closedAt` INTEGER,
                `status` TEXT NOT NULL DEFAULT 'OPEN',
                `startedBy` TEXT NOT NULL,
                `storeId` TEXT NOT NULL DEFAULT '',
                `deviceId` TEXT NOT NULL DEFAULT ''
            )
            """.trimIndent()
        )
        // Add sessionId column to existing stock_take table (0 = legacy, no session)
        db.execSQL("ALTER TABLE `stock_take` ADD COLUMN sessionId INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `invoice_lines` ADD COLUMN deviceId TEXT")
        db.execSQL("ALTER TABLE `invoice_lines` ADD COLUMN storeId TEXT")
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS `Item`")
        db.execSQL("DROP TABLE IF EXISTS `Note`")
        db.execSQL("DROP TABLE IF EXISTS `student`")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1. Recreate products table with nullable brandID and shoeSizeId
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `products_new` (
                `productId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `productDescription` TEXT NOT NULL,
                `productQty` INTEGER NOT NULL,
                `brandID` INTEGER,
                `shoeSizeId` INTEGER,
                `saleable` INTEGER NOT NULL,
                `purchasable` INTEGER NOT NULL,
                `averageCost` REAL NOT NULL,
                `standardCost` REAL NOT NULL,
                `salesTaxCode` TEXT,
                `productCode` TEXT NOT NULL,
                `inclusiveTaxed` INTEGER NOT NULL,
                `imageUri` TEXT,
                `lastUpdated` INTEGER,
                `isSynced` INTEGER NOT NULL DEFAULT 0,
                `category` TEXT,
                `unit` TEXT,
                `stockUnit` TEXT,
                FOREIGN KEY(`brandID`) REFERENCES `shoeBrand`(`brandID`) ON DELETE SET NULL,
                FOREIGN KEY(`shoeSizeId`) REFERENCES `shoe_sizes`(`shoeSizeId`) ON DELETE SET NULL
            )
            """.trimIndent()
        )

        // 2. Copy existing data
        db.execSQL(
            """
            INSERT INTO `products_new` (
                `productId`, `productDescription`, `productQty`, `brandID`, `shoeSizeId`,
                `saleable`, `purchasable`, `averageCost`, `standardCost`, `salesTaxCode`,
                `productCode`, `inclusiveTaxed`, `imageUri`, `lastUpdated`, `isSynced`
            )
            SELECT
                `productId`, `productDescription`, `productQty`, `brandID`, `shoeSizeId`,
                `saleable`, `purchasable`, `averageCost`, `standardCost`, `salesTaxCode`,
                `productCode`, `inclusiveTaxed`, `imageUri`, `lastUpdated`, `isSynced`
            FROM `products`
            """.trimIndent()
        )

        // 3. Swap tables
        db.execSQL("DROP TABLE `products`")
        db.execSQL("ALTER TABLE `products_new` RENAME TO `products`")

        // 4. Recreate indexes
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_brandID` ON `products` (`brandID`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_products_shoeSizeId` ON `products` (`shoeSizeId`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_products_productCode` ON `products` (`productCode`)")

        // 5. Create app_settings table
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `app_settings` (
                `key` TEXT NOT NULL PRIMARY KEY,
                `value` TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}
