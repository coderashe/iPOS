package com.example.learncompose303.app.di

import android.content.Context
import androidx.room.Room
import com.example.learncompose303.daos.ItemDao
import com.example.learncompose303.data.AppDatabase
import com.example.learncompose303.data.domain.repository.NoteRepository
import com.example.learncompose303.data.domain.use_case.AddNoteuseCase
import com.example.learncompose303.data.domain.use_case.DeleteNoteUseCase
import com.example.learncompose303.data.domain.use_case.GetNotesUseCase
import com.example.learncompose303.data.domain.use_case.NoteUseCase
import com.example.learncompose303.data.repositories.ItemRepository
import com.example.learncompose303.data.repositories.NoteRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideItemDao(appDatabase: AppDatabase): ItemDao = appDatabase.itemDao()

    @Provides
    fun provideItemRepository(itemDao: ItemDao): ItemRepository = ItemRepository(itemDao)

    @Provides
    @Singleton
    fun provideNoteRepository(db: AppDatabase): NoteRepository {
        return NoteRepositoryImpl(db.noteDao())
    }

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








