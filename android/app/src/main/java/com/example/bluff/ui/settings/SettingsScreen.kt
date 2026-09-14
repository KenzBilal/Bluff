package com.example.bluff.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.bluff.di.AppContainer
import com.example.bluff.domain.model.AppSettings
import com.example.bluff.domain.usecase.settings.GetSettingsUseCase
import com.example.bluff.domain.usecase.settings.UpdateSettingsUseCase
import com.example.bluff.theme.Background
import com.example.bluff.theme.CardColor
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase
) : ViewModel() {
    val settings = getSettingsUseCase()

    fun updateSetting(updater: (AppSettings) -> AppSettings) {
        val current = settings.value
        if (current != null) {
            CoroutineScope(Dispatchers.IO).launch {
                updateSettingsUseCase(updater(current))
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val container = AppContainer.instance
                SettingsViewModel(container.getSettingsUseCase, container.updateSettingsUseCase)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val settings by vm.settings.collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary
                )
            )
        },
        containerColor = Background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { Spacer(Modifier.height(16.dp)) }
            
            settings?.let { currentSettings ->
                item {
                    Text("Notifications", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    SettingSwitchItem("Budget Alerts", currentSettings.notificationsBudget) {
                        vm.updateSetting { s -> s.copy(notificationsBudget = it) }
                    }
                    SettingSwitchItem("Recurring Transactions", currentSettings.notificationsRecurring) {
                        vm.updateSetting { s -> s.copy(notificationsRecurring = it) }
                    }
                    SettingSwitchItem("Goal Progress", currentSettings.notificationsGoals) {
                        vm.updateSetting { s -> s.copy(notificationsGoals = it) }
                    }
                    SettingSwitchItem("Daily Reminder", currentSettings.notificationsDailyReminder) {
                        vm.updateSetting { s -> s.copy(notificationsDailyReminder = it) }
                    }
                    Spacer(Modifier.height(24.dp))
                }
                
                item {
                    Text("Security", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    SettingSwitchItem("Require PIN", currentSettings.pinEnabled) {
                        vm.updateSetting { s -> s.copy(pinEnabled = it) }
                    }
                    SettingSwitchItem("Biometric Authentication", currentSettings.biometricEnabled) {
                        vm.updateSetting { s -> s.copy(biometricEnabled = it) }
                    }
                }
            } ?: run {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = TextSecondary, fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
