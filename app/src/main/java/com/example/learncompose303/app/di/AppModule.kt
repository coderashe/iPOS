package com.example.learncompose303.app.di

import android.content.Context
import androidx.room.Room
import com.example.learncompose303.daos.ItemDao
import com.example.learncompose303.data.AppDatabase
import com.example.learncompose303.data.data_source.NoteDao
import com.example.learncompose303.data.data_source.StudentDao
import com.example.learncompose303.data.domain.repository.NoteRepository
import com.example.learncompose303.data.domain.use_case.AddNoteuseCase
import com.example.learncompose303.data.domain.use_case.DeleteNoteUseCase
import com.example.learncompose303.data.domain.use_case.GetNotesUseCase
import com.example.learncompose303.data.domain.use_case.NoteUseCase
import com.example.learncompose303.data.repositories.ItemRepository
import com.example.learncompose303.data.repositories.NoteRepositoryImpl
import com.example.learncompose303.data.repositories.RepositoryImpl
import com.example.learncompose303.data.repositories.StudentRepository
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

    // Provide ItemDao from AppDatabase
    @Provides
    fun provideItemDao(appDatabase: AppDatabase): ItemDao = appDatabase.itemDao()

    // Provide NoteDao from AppDatabase
    @Provides
    fun provideNoteDao(appDatabase: AppDatabase): NoteDao = appDatabase.noteDao()

    // Provide StudentDao from AppDatabase
    @Provides
    fun provideStudentDao(appDatabase: AppDatabase): StudentDao = appDatabase.studentDao()

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


//@Module
//@InstallIn(SingletonComponent::class)
//object AppModule {
//
//    @Provides
//    @Singleton
//    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
//        Room.databaseBuilder(
//            context,
//            AppDatabase::class.java,
//            AppDatabase.DATABASE_NAME
//        )
//            .fallbackToDestructiveMigration()
//            .build()
//
//    @Provides
//    fun provideItemDao(appDatabase: AppDatabase): ItemDao = appDatabase.itemDao()
//
//    @Provides
//    fun provideItemRepository(itemDao: ItemDao): ItemRepository = ItemRepository(itemDao)
//
//    @Provides
//    @Singleton
//    fun provideStudentRepository(appDatabase: AppDatabase): StudentRepository {
//        return RepositoryImpl(appDatabase.studentDao())
//    }
//
//    @Provides
//    @Singleton
//    fun provideNoteRepository(db: AppDatabase): NoteRepository {
//        return NoteRepositoryImpl(db.noteDao())
//    }
//
//    @Provides
//    @Singleton
//    fun provideNoteUseCase(repository: NoteRepository): NoteUseCase {
//        return NoteUseCase(
//            getNotesUseCase = GetNotesUseCase(repository),
//            deleteNoteUseCase = DeleteNoteUseCase(repository),
//            addNoteUseCase = AddNoteuseCase(repository)
//        )
//    }
//}








