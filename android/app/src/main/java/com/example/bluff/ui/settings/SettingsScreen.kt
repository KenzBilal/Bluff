package com.example.bluff.ui.settings

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.domain.model.AppSettings
import com.example.bluff.theme.Background
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary

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

            settings?.let { s ->
                // Personal
                item {
                    SectionHeader("Personal")
                    SettingTextItem("Name", s.userName.ifBlank { "Not set" })
                    SettingTextItem("Currency", "${s.currencySymbol} (${s.currencyCode})")
                    SettingTextItem("Financial Month Start", "Day ${s.financialMonthStartDay}")
                    Spacer(Modifier.height(24.dp))
                }

                // Appearance
                item {
                    SectionHeader("Appearance")
                    SettingTextItem("Accent Color", s.accentColor)
                    SettingTextItem("Layout Density", s.layoutDensity)
                    SettingTextItem("Animation Intensity", s.animationIntensity)
                    Spacer(Modifier.height(24.dp))
                }

                // Notifications
                item {
                    SectionHeader("Notifications")
                    SettingSwitchItem("Budget Alerts", s.notificationsBudget) {
                        vm.updateSetting(s) { st -> st.copy(notificationsBudget = it) }
                    }
                    SettingSwitchItem("Recurring Transactions", s.notificationsRecurring) {
                        vm.updateSetting(s) { st -> st.copy(notificationsRecurring = it) }
                    }
                    SettingSwitchItem("Goal Progress", s.notificationsGoals) {
                        vm.updateSetting(s) { st -> st.copy(notificationsGoals = it) }
                    }
                    SettingSwitchItem("Daily Reminder", s.notificationsDailyReminder) {
                        vm.updateSetting(s) { st -> st.copy(notificationsDailyReminder = it) }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // Security
                item {
                    SectionHeader("Security")
                    SettingSwitchItem("Require PIN", s.pinEnabled) {
                        vm.updateSetting(s) { st -> st.copy(pinEnabled = it) }
                    }
                    SettingSwitchItem("Biometric Authentication", s.biometricEnabled) {
                        vm.updateSetting(s) { st -> st.copy(biometricEnabled = it) }
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
private fun SectionHeader(title: String) {
    Text(title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun SettingTextItem(title: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = TextSecondary, fontSize = 16.sp)
        Text(value, color = TextPrimary, fontSize = 16.sp)
    }
}

@Composable
private fun SettingSwitchItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = TextSecondary, fontSize = 16.sp)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
