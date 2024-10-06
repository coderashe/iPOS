package com.example.learncompose303.app.di

import android.content.Context
import androidx.room.Room
import com.example.learncompose303.daos.ItemDao
import com.example.learncompose303.data.AppDatabase
import com.example.learncompose303.data.data_source.CartItemDao
import com.example.learncompose303.data.data_source.CustomerDao
import com.example.learncompose303.data.data_source.DayReportsDao
import com.example.learncompose303.data.data_source.InvoiceDao
import com.example.learncompose303.data.data_source.InvoiceItemDao
import com.example.learncompose303.data.data_source.InvoiceLineDao
import com.example.learncompose303.data.data_source.NoteDao
import com.example.learncompose303.data.data_source.OrderDao
import com.example.learncompose303.data.data_source.PaymentDao
import com.example.learncompose303.data.data_source.PriceListDao
import com.example.learncompose303.data.data_source.ProductDao
import com.example.learncompose303.data.data_source.ProductDayReportDao
import com.example.learncompose303.data.data_source.StudentDao
import com.example.learncompose303.data.domain.repository.NoteRepository
import com.example.learncompose303.data.domain.use_case.AddNoteuseCase
import com.example.learncompose303.data.domain.use_case.DeleteNoteUseCase
import com.example.learncompose303.data.domain.use_case.GetNotesUseCase
import com.example.learncompose303.data.domain.use_case.NoteUseCase
import com.example.learncompose303.data.repositories.CartItemRepository
import com.example.learncompose303.data.repositories.CartItemRepositoryImpl
import com.example.learncompose303.data.repositories.CustomerRepository
import com.example.learncompose303.data.repositories.CustomerRepositoryImpl
import com.example.learncompose303.data.repositories.DayReportsRepository
import com.example.learncompose303.data.repositories.DayReportsRepositoryImpl
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
import com.example.learncompose303.data.repositories.StudentRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // Handle destructive migrations
            .build()

//    Provide daoS here

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
    fun provideInvoiceDao(database: AppDatabase): InvoiceDao = database.invoiceDao()


    // Provide ItemDao from AppDatabase
    @Provides
    fun provideItemDao(appDatabase: AppDatabase): ItemDao = appDatabase.itemDao()

    // Provide NoteDao from AppDatabase
    @Provides
    fun provideNoteDao(appDatabase: AppDatabase): NoteDao = appDatabase.noteDao()

    // Provide StudentDao from AppDatabase
    @Provides
    fun provideStudentDao(appDatabase: AppDatabase): StudentDao = appDatabase.studentDao()

    fun provideProductDayReportDao(appDatabase: AppDatabase): ProductDayReportDao =
        appDatabase.productDayReportDao()

    @Provides
    @Singleton
    fun provideProductDayReportRepository(appDatabase: AppDatabase): ProductDayReportRepository {
        return ProductDayReportRepositoryImpl(appDatabase.productDayReportDao())
    }

    fun provideDayReportsDao(appDatabase: AppDatabase): DayReportsDao =
        appDatabase.dayReportsDao()

    @Provides
    @Singleton
    fun provideDayReportsRepository(appDatabase: AppDatabase): DayReportsRepository {
        return DayReportsRepositoryImpl(appDatabase.dayReportsDao())
    }

    //    Provide repositories here
    @Provides
    @Singleton
    fun providePaymentRepository(appDatabase: AppDatabase): PaymentRepository {
        return PaymentRepositoryImpl(appDatabase.paymentDao())
    }

    @Provides
    @Singleton
    fun provideInvoiceRepository(appDatabase: AppDatabase): InvoiceRepository {
        return InvoiceRepositoryImpl(appDatabase.invoiceDao())
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
    fun provideInvoiceLineRepository(invoiceLineDao: InvoiceLineDao): InvoiceLineRepository {
        return InvoiceLineRepositoryImp(invoiceLineDao)
    }

    @Provides
    fun provideOrderDao(database: AppDatabase): OrderDao {
        return database.orderDao()
    }

    @Provides
    fun provideOrderRepository(orderDao: OrderDao): OrderRepository {
        return OrderRepositoryImpl(orderDao)
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
    fun provideProductRepository(appDatabase: AppDatabase): ProductRepository {
        return ProductRepositoryImpl(appDatabase.productDao())
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
}

