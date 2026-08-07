package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.DiscountsReceived
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscountsReceivedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscountReceived(discountReceived: DiscountsReceived)

    @Update
    suspend fun updateDiscountReceived(discountReceived: DiscountsReceived)

    @Delete
    suspend fun deleteDiscountReceived(discountReceived: DiscountsReceived)

    @Query("SELECT * FROM discountsreceived WHERE discountId = :id")
    suspend fun getDiscountReceivedById(id: Long): DiscountsReceived?

    @Query("SELECT * FROM discountsreceived ORDER BY dateReceived DESC")
    fun getAllDiscountsReceived(): Flow<List<DiscountsReceived>>

    @Query("SELECT * FROM discountsreceived WHERE isSynced = 0 AND storeId != '' AND storeId IS NOT NULL LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<DiscountsReceived>

    @Query("UPDATE discountsreceived SET isSynced = 1 WHERE discountId IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM discountsreceived WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Query("UPDATE discountsreceived SET deviceId = :deviceId, storeId = :storeId WHERE deviceId = '' OR deviceId IS NULL")
    suspend fun backfillDeviceAndStore(deviceId: String, storeId: String)
}
