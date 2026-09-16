package com.example.bluff.data.repository

import com.example.bluff.data.local.BluffDatabase
import com.example.bluff.data.local.entity.AppSettingsEntity
import com.example.bluff.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface AppSettingsRepository {
    fun getSettings(): Flow<AppSettings?>
    suspend fun updateSettings(settings: AppSettings): Result<Unit>
}

class AppSettingsRepositoryImpl(
    private val db: BluffDatabase,
    private val userIdProvider: () -> String
) : AppSettingsRepository {

    override fun getSettings(): Flow<AppSettings?> =
        db.appSettingsDao().getAppSettings(userIdProvider()).map { it?.toModel() }

    override suspend fun updateSettings(settings: AppSettings): Result<Unit> {
        return try {
            val userId = userIdProvider().ifEmpty { settings.userId }
            val entity = AppSettingsEntity.fromModel(settings.copy(userId = userId))
            val existing = db.appSettingsDao().getAppSettingsSync(userId)
            if (existing == null) {
                db.appSettingsDao().insertAppSettings(entity)
            } else {
                db.appSettingsDao().updateAppSettings(entity)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
