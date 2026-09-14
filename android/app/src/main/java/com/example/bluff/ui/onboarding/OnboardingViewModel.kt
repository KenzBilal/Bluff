package com.example.bluff.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val appContainer: AppContainer
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name

    private val _phone = MutableStateFlow("")
    val phone: StateFlow<String> = _phone

    fun updateName(newName: String) {
        _name.value = newName
    }

    fun updatePhone(newPhone: String) {
        _phone.value = newPhone
    }

    fun saveProfile() {
        viewModelScope.launch {
            // Save logic using userPreferencesManager
            appContainer.userPreferencesManager.setOnboardingComplete(true)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = AppContainer.instance
                OnboardingViewModel(container)
            }
        }
    }
}
