package com.ipos.data.data_source

import androidx.room.*
import com.ipos.data.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUsers(users: List<User>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM user")
    suspend fun getAllUsersOnce(): List<User>

    @Query("SELECT * FROM user WHERE userContact = :contact LIMIT 1")
    suspend fun getUserByContact(contact: String): User?

    @Query("SELECT * FROM user WHERE userContact = :contact AND userPassword = :password LIMIT 1")
    suspend fun login(contact: String, password: String): User?

    @Query("SELECT * FROM user WHERE userEmail = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM user WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): User?

    @Query("SELECT * FROM user WHERE isSynced = 0 LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<User>

    @Query("UPDATE user SET isSynced = 1 WHERE userId IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("UPDATE user SET isSynced = 0")
    suspend fun resetSyncStatus()

    @Query("SELECT COUNT(*) FROM user WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

}
