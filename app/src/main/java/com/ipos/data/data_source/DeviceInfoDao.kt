package com.ipos.data.data_source

import androidx.room.*
import com.ipos.data.entities.DeviceInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(device: DeviceInfo)

    @Query("SELECT * FROM device_info WHERE deviceId = :deviceId LIMIT 1")
    suspend fun getByDeviceId(deviceId: String): DeviceInfo?

    @Query("SELECT * FROM device_info LIMIT 1")
    suspend fun getActiveDevice(): DeviceInfo?

    @Query("SELECT storeId FROM device_info WHERE storeId != '' LIMIT 1")
    suspend fun getActiveStoreId(): String?

    @Query("UPDATE device_info SET isSynced = 0")
    suspend fun resetSyncStatus()

    @Query("SELECT * FROM device_info WHERE isSynced = 0 LIMIT :limit")
    suspend fun getUnsynced(limit: Int): List<DeviceInfo>

    @Query("UPDATE device_info SET isSynced = 1 WHERE deviceId = :deviceId")
    suspend fun markSynced(deviceId: String)

    @Query("SELECT COUNT(*) FROM device_info WHERE isSynced = 0")
    fun getUnsyncedCount(): Flow<Int>
}
