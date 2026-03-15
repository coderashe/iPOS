package com.example.learncompose303.data.data_source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.learncompose303.data.entities.ShoeBrand
import com.example.learncompose303.data.entities.ShoeSize
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoeBrandDao {

    @Query("SELECT * FROM shoeBrand")
    fun getAllShoeBrands(): Flow<List<ShoeBrand>>


    @Query("SELECT * FROM shoeBrand")
    suspend fun getAllShoeBrandsOnce(): List<ShoeBrand>

    @Query("SELECT * FROM shoeBrand WHERE brandID = :id")
    suspend fun getShoeBrandById(id: Long): ShoeBrand?

    @Query("SELECT * FROM shoeBrand WHERE brandID = :brandId")
    fun getBrandById(brandId: Long): Flow<ShoeBrand?>

    @Query("SELECT * FROM shoeBrand WHERE brandId = :brandId LIMIT 1")
    suspend fun getBrandNameById(brandId: Long): ShoeBrand?

    @Query("SELECT * FROM shoeBrand WHERE LOWER(brandName) = LOWER(:brandName) LIMIT 1")
    suspend fun getBrandByName(brandName: String): ShoeBrand?

    @Query("SELECT * FROM shoeBrand WHERE brandName = :brandName COLLATE NOCASE LIMIT 1")
    suspend fun getBrandByNameExact(brandName: String): ShoeBrand?

    @Query("SELECT * FROM shoeBrand WHERE LOWER(brandName) = LOWER(:brandName) LIMIT 1")
    suspend fun getBrandByNameCaseInsensitive(brandName: String): ShoeBrand?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoeBrand(shoeBrand: ShoeBrand): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoeBrands(shoeBrands: List<ShoeBrand>): List<Long>

    @Update
    suspend fun updateShoeBrand(shoeBrand: ShoeBrand)

    @Delete
    suspend fun deleteShoeBrand(shoeBrand: ShoeBrand)

    @Query("SELECT * FROM shoeBrand WHERE isSynced = 0 LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<ShoeBrand>

    @Query("UPDATE shoeBrand SET isSynced = 1 WHERE brandID IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM shoeBrand WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>
}
