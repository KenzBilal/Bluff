package com.example.bluff.domain.usecase.settings

import com.example.bluff.data.repository.AppSettingsRepository
import com.example.bluff.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

class GetAppSettingsUseCase(private val repository: AppSettingsRepository) {
    operator fun invoke(): Flow<AppSettings?> = repository.getSettings()
}

class UpdateAppSettingsUseCase(private val repository: AppSettingsRepository) {
    suspend operator fun invoke(settings: AppSettings): Result<Unit> =
        repository.updateSettings(settings)
}
