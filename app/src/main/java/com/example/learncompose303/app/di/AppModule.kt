package com.example.learncompose303.app.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.learncompose303.daos.ItemDao
import com.example.learncompose303.data.AppDatabase
import com.example.learncompose303.data.MIGRATION_1_2
import com.example.learncompose303.data.MIGRATION_2_3
import com.example.learncompose303.data.MIGRATION_3_4
import com.example.learncompose303.data.MIGRATION_4_5
import com.example.learncompose303.data.MIGRATION_5_6
import com.example.learncompose303.data.MIGRATION_6_7
import com.example.learncompose303.data.MIGRATION_7_8
import com.example.learncompose303.data.MIGRATION_8_9
import com.example.learncompose303.data.MIGRATION_9_10
import com.example.learncompose303.app.core.DeviceSession
import com.example.learncompose303.data.data_source.DeviceInfoDao
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
import com.example.learncompose303.data.data_source.UserDao
import com.example.learncompose303.data.domain.repository.NoteRepository
import com.example.learncompose303.data.domain.use_case.AddNoteuseCase
import com.example.learncompose303.data.domain.use_case.DeleteNoteUseCase
import com.example.learncompose303.data.domain.use_case.GetNotesUseCase
import com.example.learncompose303.data.domain.use_case.NoteUseCase
import com.example.learncompose303.data.repositories.AuthRepository
import com.example.learncompose303.data.repositories.AuthRepositoryImpl
import com.example.learncompose303.data.repositories.BusinessRepository
import com.example.learncompose303.data.repositories.BusinessRepositoryImpl
import com.example.learncompose303.data.repositories.CartItemRepository
import com.example.learncompose303.data.repositories.CartItemRepositoryImpl
import com.example.learncompose303.data.repositories.CurrencyRepository
import com.example.learncompose303.data.repositories.CurrencyRepositoryImpl
import com.example.learncompose303.data.repositories.CustomerRepository
import com.example.learncompose303.data.repositories.CustomerRepositoryImpl
import com.example.learncompose303.data.repositories.DayReportsRepository
import com.example.learncompose303.data.repositories.DayReportsRepositoryImpl
import com.example.learncompose303.data.repositories.ExpenseRepository
import com.example.learncompose303.data.repositories.ExpenseRepositoryImpl
import com.example.learncompose303.data.repositories.FailedInvoiceRepository
import com.example.learncompose303.data.repositories.InvoiceItemRepository
import com.example.learncompose303.data.repositories.InvoiceItemRepositoryImpl
import com.example.learncompose303.data.repositories.InvoiceLineRepository
import com.example.learncompose303.data.repositories.InvoiceLineRepositoryImp
import com.example.learncompose303.data.repositories.InvoiceRepository
import com.example.learncompose303.data.repositories.InvoiceRepositoryImpl
import com.example.learncompose303.data.repositories.ItemRepository
import com.example.learncompose303.data.repositories.NoteRepositoryImpl
import com.example.learncompose303.data.repositories.OrderRepository
import com.example.learncompose303.data.repositories.OrderRepositoryImpl
import com.example.learncompose303.data.repositories.PaymentRepository
import com.example.learncompose303.data.repositories.PaymentRepositoryImpl
import com.example.learncompose303.data.repositories.PriceListRepository
import com.example.learncompose303.data.repositories.PriceListRepositoryImpl
import com.example.learncompose303.data.repositories.ProductDayReportRepository
import com.example.learncompose303.data.repositories.ProductDayReportRepositoryImpl
import com.example.learncompose303.data.repositories.ProductRepository
import com.example.learncompose303.data.repositories.ProductRepositoryImpl
import com.example.learncompose303.data.repositories.RepositoryImpl
import com.example.learncompose303.data.repositories.ShoeBrandRepository
import com.example.learncompose303.data.repositories.ShoeBrandRepositoryImpl
import com.example.learncompose303.data.repositories.ShoeSizeRepository
import com.example.learncompose303.data.repositories.ShoeSizeRepositoryImpl
import com.example.learncompose303.data.repositories.StockTakeRepository
import com.example.learncompose303.data.repositories.StockTakeRepositoryImpl
import com.example.learncompose303.data.repositories.StudentRepository
import com.example.learncompose303.network.BusinessApiService
import com.example.learncompose303.network.ErrorHandler
import com.example.learncompose303.network.InvoiceApiService
import com.example.learncompose303.network.UserApiService
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
                MIGRATION_9_10
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
    fun provideSyncManager(workManager: WorkManager): com.example.learncompose303.network.SyncManager {
        return com.example.learncompose303.network.SyncManager(workManager)
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
    @Singleton
    fun provideStockTakeRepository(appDatabase: AppDatabase, deviceSession: DeviceSession): StockTakeRepository {
        return StockTakeRepositoryImpl(appDatabase.stockTakeDao(), deviceSession)
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


    // Provide ItemDao from AppDatabase
    @Provides
    fun provideItemDao(appDatabase: AppDatabase): ItemDao = appDatabase.itemDao()

    // Provide NoteDao from AppDatabase
    @Provides
    fun provideNoteDao(appDatabase: AppDatabase): NoteDao = appDatabase.noteDao()

    // Provide StudentDao from AppDatabase
    @Provides
    fun provideStudentDao(appDatabase: AppDatabase): StudentDao = appDatabase.studentDao()

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
        syncManager: com.example.learncompose303.network.SyncManager,
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
        syncManager: com.example.learncompose303.network.SyncManager
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
        syncManager: com.example.learncompose303.network.SyncManager
    ): ProductRepository {
        return ProductRepositoryImpl(appDatabase.productDao(), syncManager)
    }

    // Provide ItemRepository, assuming ItemRepository takes an ItemDao
    @Provides
    @Singleton
    fun provideItemRepository(itemDao: ItemDao): ItemRepository = ItemRepository(itemDao)

    // Provide StudentRepository, assuming StudentRepository takes a StudentDao
    @Provides
    @Singleton
    fun provideStudentRepository(appDatabase: AppDatabase): StudentRepository {
        return RepositoryImpl(appDatabase.studentDao())
    }

    // Provide NoteRepository, assuming NoteRepository takes a NoteDao
    @Provides
    @Singleton
    fun provideNoteRepository(noteDao: NoteDao): NoteRepository {
        return NoteRepositoryImpl(noteDao)
    }

    // Provide NoteUseCase for handling note-related operations
    @Provides
    @Singleton
    fun provideNoteUseCase(repository: NoteRepository): NoteUseCase {
        return NoteUseCase(
            getNotesUseCase = GetNotesUseCase(repository),
            deleteNoteUseCase = DeleteNoteUseCase(repository),
            addNoteUseCase = AddNoteuseCase(repository)
        )
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
    @Singleton
    fun provideBackfillRepository(
        invoiceDao: InvoiceDao,
        expenseDao: ExpenseDao,
        paymentDao: PaymentDao,
        cashFlowDao: CashFlowDao,
        stockTakeDao: StockTakeDao,
        dayReportsDao: DayReportsDao,
        tradeReceivablesDao: TradeReceivablesDao,
        discountsReceivedDao: DiscountsReceivedDao,
        orderDao: OrderDao
    ): com.example.learncompose303.data.repositories.BackfillRepository {
        return com.example.learncompose303.data.repositories.BackfillRepository(
            invoiceDao, expenseDao, paymentDao, cashFlowDao,
            stockTakeDao, dayReportsDao, tradeReceivablesDao, discountsReceivedDao, orderDao
        )
    }

    @Provides
    @Singleton
    fun provideCashFlowRepository(
        appDatabase: AppDatabase,
        deviceSession: DeviceSession
    ): com.example.learncompose303.data.repositories.CashFlowRepository {
        return com.example.learncompose303.data.repositories.CashFlowRepositoryImpl(
            appDatabase.cashFlowDao(), deviceSession
        )
    }

    @Provides
    @Singleton
    fun provideTradeReceivablesRepository(
        appDatabase: AppDatabase,
        deviceSession: DeviceSession
    ): com.example.learncompose303.data.repositories.TradeReceivablesRepository {
        return com.example.learncompose303.data.repositories.TradeReceivablesRepositoryImpl(
            appDatabase.tradeReceivablesDao(), deviceSession
        )
    }

    @Provides
    @Singleton
    fun provideDiscountsReceivedRepository(
        appDatabase: AppDatabase,
        deviceSession: DeviceSession
    ): com.example.learncompose303.data.repositories.DiscountsReceivedRepository {
        return com.example.learncompose303.data.repositories.DiscountsReceivedRepositoryImpl(
            appDatabase.discountsReceivedDao(), deviceSession
        )
    }
}

