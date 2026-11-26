package com.ipos.app.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.ipos.data.AppDatabase
import com.ipos.data.MIGRATION_1_2
import com.ipos.data.MIGRATION_2_3
import com.ipos.data.MIGRATION_3_4
import com.ipos.data.MIGRATION_4_5
import com.ipos.data.MIGRATION_5_6
import com.ipos.data.MIGRATION_6_7
import com.ipos.data.MIGRATION_7_8
import com.ipos.data.MIGRATION_8_9
import com.ipos.data.MIGRATION_9_10
import com.ipos.data.MIGRATION_10_11
import com.ipos.data.MIGRATION_11_12
import com.ipos.data.MIGRATION_12_13
import com.ipos.data.MIGRATION_13_14
import com.ipos.data.MIGRATION_14_15
import com.ipos.app.core.DeviceSession
import com.ipos.data.data_source.DeviceInfoDao
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
import com.ipos.data.data_source.UserDao
import com.ipos.data.repositories.AuthRepository
import com.ipos.data.repositories.AuthRepositoryImpl
import com.ipos.data.repositories.BusinessRepository
import com.ipos.data.repositories.BusinessRepositoryImpl
import com.ipos.data.repositories.CartItemRepository
import com.ipos.data.repositories.CartItemRepositoryImpl
import com.ipos.data.repositories.CurrencyRepository
import com.ipos.data.repositories.CurrencyRepositoryImpl
import com.ipos.data.repositories.CustomerRepository
import com.ipos.data.repositories.CustomerRepositoryImpl
import com.ipos.data.repositories.DayReportsRepository
import com.ipos.data.repositories.DayReportsRepositoryImpl
import com.ipos.data.repositories.ExpenseRepository
import com.ipos.data.repositories.ExpenseRepositoryImpl
import com.ipos.data.repositories.FailedInvoiceRepository
import com.ipos.data.repositories.InvoiceItemRepository
import com.ipos.data.repositories.InvoiceItemRepositoryImpl
import com.ipos.data.repositories.InvoiceLineRepository
import com.ipos.data.repositories.InvoiceLineRepositoryImp
import com.ipos.data.repositories.InvoiceRepository
import com.ipos.data.repositories.InvoiceRepositoryImpl
import com.ipos.data.repositories.OrderRepository
import com.ipos.data.repositories.OrderRepositoryImpl
import com.ipos.data.repositories.PaymentRepository
import com.ipos.data.repositories.PaymentRepositoryImpl
import com.ipos.data.repositories.PriceListRepository
import com.ipos.data.repositories.PriceListRepositoryImpl
import com.ipos.data.repositories.ProductDayReportRepository
import com.ipos.data.repositories.ProductDayReportRepositoryImpl
import com.ipos.data.repositories.ProductRepository
import com.ipos.data.repositories.ProductRepositoryImpl
import com.ipos.data.repositories.ShoeBrandRepository
import com.ipos.data.repositories.ShoeBrandRepositoryImpl
import com.ipos.data.repositories.ShoeSizeRepository
import com.ipos.data.repositories.ShoeSizeRepositoryImpl
import com.ipos.data.repositories.StockTakeRepository
import com.ipos.data.repositories.StockTakeRepositoryImpl
import com.ipos.network.BusinessApiService
import com.ipos.network.ErrorHandler
import com.ipos.network.InvoiceApiService
import com.ipos.network.UserApiService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Provide the AppDatabase instance as a Singleton
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(
                MIGRATION_1_2,
                MIGRATION_2_3,
                MIGRATION_3_4,
                MIGRATION_4_5,
                MIGRATION_5_6,
                MIGRATION_6_7,
                MIGRATION_7_8,
                MIGRATION_8_9,
                MIGRATION_9_10,
                MIGRATION_10_11,
                MIGRATION_11_12,
                MIGRATION_12_13,
                MIGRATION_13_14,
                MIGRATION_14_15
            )
            .build()
    }

    // Provide WorkManager instance
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    // Provide SyncManager instance
    @Provides
    @Singleton
    fun provideSyncManager(workManager: WorkManager): com.ipos.network.SyncManager {
        return com.ipos.network.SyncManager(workManager)
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideErrorHandler(gson: Gson): ErrorHandler = ErrorHandler(gson)

    @Provides
    fun provideInvoiceDao(appDatabase: AppDatabase): InvoiceDao = appDatabase.invoiceDao()

    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao =
        appDatabase.userDao()

    @Provides
    @Singleton
    fun provideAuthRepository(
        appDatabase: AppDatabase,
        userApiService: UserApiService
    ): AuthRepository {
        return AuthRepositoryImpl(appDatabase.userDao(), userApiService)
    }

//    Provide daoS here

    @Provides
    fun provideFailedInvoiceDao(appDatabase: AppDatabase): FailedInvoiceDao = 
        appDatabase.failedInvoiceDao()

    @Provides
    @Singleton
    fun provideFailedInvoiceRepository(failedInvoiceDao: FailedInvoiceDao): FailedInvoiceRepository {
        return FailedInvoiceRepository(failedInvoiceDao)
    }

    @Provides
    fun provideStockTakeDao(appDatabase: AppDatabase): StockTakeDao = appDatabase.stockTakeDao()

    @Provides
    fun provideStockTakeSessionDao(appDatabase: AppDatabase): StockTakeSessionDao =
        appDatabase.stockTakeSessionDao()

    @Provides
    @Singleton
    fun provideStockTakeRepository(
        stockTakeDao: StockTakeDao,
        sessionDao: StockTakeSessionDao,
        deviceSession: DeviceSession
    ): StockTakeRepository {
        return StockTakeRepositoryImpl(stockTakeDao, sessionDao, deviceSession)
    }


    @Provides
    fun provideShoeBrandDao(appDatabase: AppDatabase): ShoeBrandDao = appDatabase.shoeBrandDao()

    @Provides
    fun provideShoeSizeDao(appDatabase: AppDatabase): ShoeSizeDao =
        appDatabase.shoeSizeDao()

    @Provides
    fun provideCustomerDao(database: AppDatabase): CustomerDao = database.customerDao()

    @Provides
    fun provideInvoiceItemDao(database: AppDatabase): InvoiceItemDao = database.invoiceItemDao()

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()

    @Provides
    fun providePaymentDao(database: AppDatabase): PaymentDao = database.paymentDao()

    @Provides
    fun provideCartItemDao(database: AppDatabase): CartItemDao = database.cartItemDao()

    @Provides
    fun provideProductDayReportDao(appDatabase: AppDatabase): ProductDayReportDao =
        appDatabase.productDayReportDao()

    @Provides
    @Singleton
    fun provideProductDayReportRepository(appDatabase: AppDatabase): ProductDayReportRepository {
        return ProductDayReportRepositoryImpl(appDatabase.productDayReportDao())
    }

    @Provides
    fun provideExpenseDao(appDatabase: AppDatabase): ExpenseDao =
        appDatabase.expenseDao()

    @Provides
    @Singleton
    fun provideExpenseRepository(appDatabase: AppDatabase, deviceSession: DeviceSession): ExpenseRepository {
        return ExpenseRepositoryImpl(appDatabase.expenseDao(), deviceSession)
    }

    //
    @Provides
    @Singleton
    fun provideBusinessRepository(
        appDatabase: AppDatabase,
        apiService: BusinessApiService
    ): BusinessRepository {
        return BusinessRepositoryImpl(
            appDatabase.expenseDao(),
            appDatabase.invoiceDao(),
            appDatabase.invoiceLineDao(),
            appDatabase.currencyDao(),
            apiService
        )
    }
//

    @Provides
    fun provideDayReportsDao(appDatabase: AppDatabase): DayReportsDao =
        appDatabase.dayReportsDao()

//     repositories

    @Provides
    @Singleton
    fun provideShoeBrandRepository(appDatabase: AppDatabase): ShoeBrandRepository {
        return ShoeBrandRepositoryImpl(appDatabase.shoeBrandDao())
    }

    @Provides
    @Singleton
    fun provideShoeSizeRepository(appDatabase: AppDatabase): ShoeSizeRepository {
        return ShoeSizeRepositoryImpl(appDatabase.shoeSizeDao())
    }

    @Provides
    @Singleton
    fun provideDayReportsRepository(appDatabase: AppDatabase, deviceSession: DeviceSession): DayReportsRepository {
        return DayReportsRepositoryImpl(appDatabase.dayReportsDao(), deviceSession)
    }

    //    Provide repositories here
    @Provides
    @Singleton
    fun providePaymentRepository(appDatabase: AppDatabase, deviceSession: DeviceSession): PaymentRepository {
        return PaymentRepositoryImpl(appDatabase.paymentDao(), deviceSession)
    }

    @Provides
    @Singleton
    fun provideInvoiceRepository(
        appDatabase: AppDatabase,
        invoiceApiService: InvoiceApiService,
        syncManager: com.ipos.network.SyncManager,
        deviceSession: DeviceSession
    ): InvoiceRepository {
        return InvoiceRepositoryImpl(
            appDatabase.invoiceDao(),
            appDatabase.currencyDao(),
            invoiceApiService,
            syncManager,
            deviceSession
        )
    }

    //    currencies

    @Provides
    fun provideCurrencyDao(database: AppDatabase): CurrencyDao = database.currencyDao()

    @Provides
    @Singleton
    fun provideCurrencyRepository(appDatabase: AppDatabase): CurrencyRepository {
        return CurrencyRepositoryImpl(appDatabase.currencyDao())
    }


    @Provides
    @Singleton
    fun provideCustomerRepository(appDatabase: AppDatabase): CustomerRepository {
        return CustomerRepositoryImpl(appDatabase.customerDao())
    }

    @Provides
    @Singleton
    fun provideCartItenRepository(appDatabase: AppDatabase): CartItemRepository {
        return CartItemRepositoryImpl(appDatabase.cartItemDao())
    }

    @Provides
    fun provideInvoiceLineDao(database: AppDatabase): InvoiceLineDao {
        return database.invoiceLineDao()
    }

    @Provides
    fun provideInvoiceLineRepository(
        invoiceLineDao: InvoiceLineDao,
        syncManager: com.ipos.network.SyncManager
    ): InvoiceLineRepository {
        return InvoiceLineRepositoryImp(invoiceLineDao, syncManager)
    }

    @Provides
    fun provideOrderDao(database: AppDatabase): OrderDao {
        return database.orderDao()
    }

    @Provides
    fun provideOrderRepository(orderDao: OrderDao, deviceSession: DeviceSession): OrderRepository {
        return OrderRepositoryImpl(orderDao, deviceSession)
    }

    //
    @Provides
    fun providePriceListDao(database: AppDatabase): PriceListDao {
        return database.pricelistDao()
    }

    @Provides
    fun providePriceListRepository(priceListDao: PriceListDao): PriceListRepository {
        return PriceListRepositoryImpl(priceListDao)
    }

//

    @Provides
    @Singleton
    fun provideInvoiceItemRepository(appDatabase: AppDatabase): InvoiceItemRepository {
        return InvoiceItemRepositoryImpl(appDatabase.invoiceItemDao())
    }

    @Provides
    @Singleton
    fun provideProductRepository(
        appDatabase: AppDatabase,
        syncManager: com.ipos.network.SyncManager,
        deviceSession: DeviceSession
    ): ProductRepository {
        return ProductRepositoryImpl(appDatabase.productDao(), syncManager, deviceSession)
    }

    @Provides
    fun provideCashFlowDao(appDatabase: AppDatabase): CashFlowDao = appDatabase.cashFlowDao()

    @Provides
    fun provideTradeReceivablesDao(appDatabase: AppDatabase): TradeReceivablesDao = appDatabase.tradeReceivablesDao()

    @Provides
    fun provideDiscountsReceivedDao(appDatabase: AppDatabase): DiscountsReceivedDao = appDatabase.discountsReceivedDao()

    @Provides
    fun provideDeviceInfoDao(appDatabase: AppDatabase): DeviceInfoDao = appDatabase.deviceInfoDao()

    @Provides
    fun provideAppSettingsDao(appDatabase: AppDatabase): AppSettingsDao = appDatabase.appSettingsDao()

    @Provides
    @Singleton
    fun provideBackfillRepository(
        invoiceDao: InvoiceDao,
        invoiceLineDao: InvoiceLineDao,
        expenseDao: ExpenseDao,
        paymentDao: PaymentDao,
        cashFlowDao: CashFlowDao,
        stockTakeDao: StockTakeDao,
        dayReportsDao: DayReportsDao,
        tradeReceivablesDao: TradeReceivablesDao,
        discountsReceivedDao: DiscountsReceivedDao,
        orderDao: OrderDao,
        userDao: UserDao,
        productDao: ProductDao,
        customerDao: CustomerDao,
        currencyDao: com.ipos.data.data_source.CurrencyDao
    ): com.ipos.data.repositories.BackfillRepository {
        return com.ipos.data.repositories.BackfillRepository(
            invoiceDao, invoiceLineDao, expenseDao, paymentDao, cashFlowDao,
            stockTakeDao, dayReportsDao, tradeReceivablesDao, discountsReceivedDao, orderDao,
            userDao, productDao, customerDao, currencyDao
        )
    }

    @Provides
    @Singleton
    fun provideCashFlowRepository(
        appDatabase: AppDatabase,
        deviceSession: DeviceSession
    ): com.ipos.data.repositories.CashFlowRepository {
        return com.ipos.data.repositories.CashFlowRepositoryImpl(
            appDatabase.cashFlowDao(), deviceSession
        )
    }

    @Provides
    @Singleton
    fun provideTradeReceivablesRepository(
        appDatabase: AppDatabase,
        deviceSession: DeviceSession
    ): com.ipos.data.repositories.TradeReceivablesRepository {
        return com.ipos.data.repositories.TradeReceivablesRepositoryImpl(
            appDatabase.tradeReceivablesDao(), deviceSession
        )
    }

    @Provides
    @Singleton
    fun provideDiscountsReceivedRepository(
        appDatabase: AppDatabase,
        deviceSession: DeviceSession
    ): com.ipos.data.repositories.DiscountsReceivedRepository {
        return com.ipos.data.repositories.DiscountsReceivedRepositoryImpl(
            appDatabase.discountsReceivedDao(), deviceSession
        )
    }
}

