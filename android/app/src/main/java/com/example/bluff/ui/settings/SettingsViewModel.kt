package com.example.bluff.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.AppSettings
import com.example.bluff.domain.usecase.settings.GetAppSettingsUseCase
import com.example.bluff.domain.usecase.settings.UpdateAppSettingsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getSettingsUseCase: GetAppSettingsUseCase,
    private val updateSettingsUseCase: UpdateAppSettingsUseCase
) : ViewModel() {
    val settings = getSettingsUseCase()

    fun updateSetting(current: AppSettings, updater: (AppSettings) -> AppSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            updateSettingsUseCase(updater(current))
        }
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppContainer.instance.db
            db.transactionDao().deleteAll()
            db.budgetDao().deleteAll()
            db.goalDao().deleteAll()
            db.recurringTransactionDao().deleteAll()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = AppContainer.instance
                SettingsViewModel(container.getAppSettingsUseCase, container.updateAppSettingsUseCase)
            }
        }
    }
}
