package com.example.learncompose303.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.learncompose303.daos.ItemDao
import com.example.learncompose303.data.data_source.CartItemDao
import com.example.learncompose303.data.data_source.CashFlowDao
import com.example.learncompose303.data.data_source.CurrencyDao
import com.example.learncompose303.data.data_source.CustomerDao
import com.example.learncompose303.data.data_source.DayReportsDao
import com.example.learncompose303.data.data_source.DiscountsReceivedDao
import com.example.learncompose303.data.data_source.ExpenseDao
import com.example.learncompose303.data.data_source.FailedInvoiceDao
import com.example.learncompose303.data.data_source.InvoiceDao
import com.example.learncompose303.data.data_source.InvoiceItemDao
import com.example.learncompose303.data.data_source.InvoiceLineDao
import com.example.learncompose303.data.data_source.ShoeBrandDao
import com.example.learncompose303.data.data_source.NoteDao
import com.example.learncompose303.data.data_source.OrderDao
import com.example.learncompose303.data.data_source.PaymentDao
import com.example.learncompose303.data.data_source.PriceListDao
import com.example.learncompose303.data.data_source.ProductDao
import com.example.learncompose303.data.data_source.ProductDayReportDao
import com.example.learncompose303.data.data_source.ShoeSizeDao
import com.example.learncompose303.data.data_source.StockTakeDao
import com.example.learncompose303.data.data_source.StudentDao
import com.example.learncompose303.data.data_source.TradeReceivablesDao
import com.example.learncompose303.data.data_source.DeviceInfoDao
import com.example.learncompose303.data.entities.DeviceInfo
import com.example.learncompose303.data.data_source.UserDao
import com.example.learncompose303.data.domain.model.Note
import com.example.learncompose303.data.entities.CartItem
import com.example.learncompose303.data.entities.Currency
import com.example.learncompose303.data.entities.Customer
import com.example.learncompose303.data.entities.DayReports
import com.example.learncompose303.data.entities.DiscountsReceived
import com.example.learncompose303.data.entities.Expense
import com.example.learncompose303.data.entities.FailedInvoiceItem
import com.example.learncompose303.data.entities.Invoice
import com.example.learncompose303.data.entities.InvoiceItem
import com.example.learncompose303.data.entities.InvoiceLine
import com.example.learncompose303.data.entities.Order
import com.example.learncompose303.data.entities.Payment
import com.example.learncompose303.data.entities.PriceList
import com.example.learncompose303.data.entities.Products
import com.example.learncompose303.data.entities.ProductDayReport
import com.example.learncompose303.data.entities.ShoeBrand
import com.example.learncompose303.data.entities.ShoeSize
import com.example.learncompose303.data.entities.StockTake
import com.example.learncompose303.data.entities.CashFlow
import com.example.learncompose303.data.entities.TradeReceivables
import com.example.learncompose303.data.entities.User

@Database(
    entities = [
        Item::class, Note::class,
        StudentEntity::class, Customer::class,
        Invoice::class, InvoiceItem::class, InvoiceLine::class,
        Payment::class, Products::class, CartItem::class,
        Order::class, PriceList::class, ProductDayReport::class, DayReports::class,
        User::class, Expense::class,
        TradeReceivables::class, DiscountsReceived::class, Currency::class,
        CashFlow::class, ShoeBrand::class, ShoeSize::class, StockTake::class,
        FailedInvoiceItem::class,
        DeviceInfo::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // DAOs
    abstract fun itemDao(): ItemDao
    abstract fun noteDao(): NoteDao
    abstract fun studentDao(): StudentDao
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
    abstract fun failedInvoiceDao(): FailedInvoiceDao

    // New DAOs
    abstract fun tradeReceivablesDao(): TradeReceivablesDao
    abstract fun discountsReceivedDao(): DiscountsReceivedDao
    abstract fun cashFlowDao(): CashFlowDao
    abstract fun currencyDao(): CurrencyDao
    abstract fun deviceInfoDao(): DeviceInfoDao

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

// ── Downgrade safety migrations ───────────────────────────────────────────────
// These are no-ops. SQLite allows columns to exist that the old app doesn't know
// about — it simply ignores them. So downgrading just means telling Room "yes,
// this schema is acceptable" without dropping any data.
// Only needed if you accidentally deploy an older build over a newer DB.

val MIGRATION_10_9 = object : Migration(10, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // No destructive changes — the extra columns added in 9→10 are ignored by the
        // older app. Room just needs a registered path to accept the DB.
    }
}

val MIGRATION_9_8 = object : Migration(9, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // device_info table added in 8→9 is ignored by older app code.
    }
}

val MIGRATION_8_7 = object : Migration(8, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // isSynced columns added in 7→8 are ignored by older app code.
    }
}
