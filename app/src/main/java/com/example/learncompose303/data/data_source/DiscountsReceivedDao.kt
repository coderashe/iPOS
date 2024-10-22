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
}
