package com.example.learncompose303.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.learncompose303.daos.ItemDao
import com.example.learncompose303.data.data_source.NoteDao
import com.example.learncompose303.data.domain.model.Note

@Database(entities = [Item::class, Note::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
    abstract fun noteDao(): NoteDao

    companion object {
        const val DATABASE_NAME = "learn_db"
    }
}
