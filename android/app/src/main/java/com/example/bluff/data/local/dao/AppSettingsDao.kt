package com.example.bluff.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bluff.data.local.entity.AppSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE userId = :userId LIMIT 1")
    fun getAppSettings(userId: String): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE userId = :userId LIMIT 1")
    suspend fun getAppSettingsSync(userId: String): AppSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSettings(settings: AppSettingsEntity)

    @Update
    suspend fun updateAppSettings(settings: AppSettingsEntity)

    @Query("DELETE FROM app_settings WHERE userId = :userId")
    suspend fun deleteSettings(userId: String)
}
