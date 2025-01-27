package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.ShoeBrand
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoeBrandDao {
    @Query("SELECT * FROM labels WHERE brandID = :id")
    suspend fun getLabelById(id: Long): ShoeBrand?

    @Query("SELECT * FROM labels")
    fun getAllLabels(): Flow<List<ShoeBrand>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabel(shoeBrand: ShoeBrand): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabels(shoeBrands: List<ShoeBrand>): List<Long>

    @Update
    suspend fun updateLabel(shoeBrand: ShoeBrand)

    @Delete
    suspend fun deleteLabel(shoeBrand: ShoeBrand)
}
