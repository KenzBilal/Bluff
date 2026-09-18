package com.example.bluff.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.app.ActivityManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.bluff.domain.model.AppSettings
import com.example.bluff.theme.Background
import com.example.bluff.theme.CardColor
import com.example.bluff.theme.ExpenseColor
import com.example.bluff.theme.Primary
import com.example.bluff.theme.TextPrimary
import com.example.bluff.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val vm: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val settings by vm.settings.collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current

    // Notification permission state (Android 13+)
    var notifGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            else true
        )
    }
    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> notifGranted = granted }

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
                    // Android 13+ permission status row
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notifGranted) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Primary.copy(alpha = 0.1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Notifications not allowed", color = Primary, fontSize = 14.sp)
                                TextButton(onClick = {
                                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }) {
                                    Text("Allow", color = Primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
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
                    Spacer(Modifier.height(24.dp))
                }

            } ?: run {
                item {
                    val defaultSettings = AppSettings(userId = "")
                    SectionHeader("Personal")
                    SettingTextItem("Name", defaultSettings.userName.ifBlank { "Not set" })
                    SettingTextItem("Currency", "${defaultSettings.currencySymbol} (${defaultSettings.currencyCode})")
                    SettingTextItem("Financial Month Start", "Day ${defaultSettings.financialMonthStartDay}")
                    Spacer(Modifier.height(24.dp))

                    SectionHeader("Appearance")
                    SettingTextItem("Accent Color", defaultSettings.accentColor)
                    SettingTextItem("Layout Density", defaultSettings.layoutDensity)
                    SettingTextItem("Animation Intensity", defaultSettings.animationIntensity)
                    Spacer(Modifier.height(24.dp))
                }
            }

            // Danger Zone (always visible)
            item {
                SectionHeader("Danger Zone")
                var showClearDialog by remember { mutableStateOf(false) }
                SettingButtonItem("Factory Reset App", "Deletes ALL app data permanently (Categories, Accounts, Transactions)") {
                    showClearDialog = true
                }
                if (showClearDialog) {
                    AlertDialog(
                        onDismissRequest = { showClearDialog = false },
                        containerColor = com.example.bluff.theme.CardColor,
                        title = { Text("Factory Reset", color = TextPrimary) },
                        text = { Text("This will permanently delete ALL data and restart the app. This cannot be undone.", color = TextSecondary) },
                        confirmButton = {
                            TextButton(onClick = {
                                showClearDialog = false
                                val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                                am.clearApplicationUserData()
                            }) {
                                Text("Reset App", color = ExpenseColor)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showClearDialog = false }) {
                                Text("Cancel", color = TextSecondary)
                            }
                        }
                    )
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

@Composable
private fun SettingButtonItem(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = ExpenseColor.copy(alpha = 0.1f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = ExpenseColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextSecondary, fontSize = 14.sp)
        }
    }
}
