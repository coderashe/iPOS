package com.example.learncompose303.data.data_source

import androidx.room.*
import com.example.learncompose303.data.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM user WHERE userContact = :contact LIMIT 1")
    suspend fun getUserByContact(contact: String): User?

    @Query("SELECT * FROM user WHERE userContact = :contact AND userPassword = :password LIMIT 1")
    suspend fun login(contact: String, password: String): User?
}
