package com.example.bluff.ui.settings

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluff.domain.model.AppSettings
import com.example.bluff.theme.*

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
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            settings?.let { s ->
                item {
                    SettingsSection(title = "Notifications") {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notifGranted) {
                            PermissionWarningCard {
                                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                        
                        SettingsSwitchRow(
                            icon = Icons.Default.NotificationsActive,
                            title = "Budget Alerts",
                            subtitle = "Get notified when nearing limits",
                            checked = s.notificationsBudget,
                            onCheckedChange = { vm.updateSetting(s) { st -> st.copy(notificationsBudget = it) } }
                        )
                        HorizontalDivider(color = CardColor, modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsSwitchRow(
                            icon = Icons.Default.Autorenew,
                            title = "Recurring Transactions",
                            subtitle = "Alerts for upcoming bills",
                            checked = s.notificationsRecurring,
                            onCheckedChange = { vm.updateSetting(s) { st -> st.copy(notificationsRecurring = it) } }
                        )
                        HorizontalDivider(color = CardColor, modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsSwitchRow(
                            icon = Icons.Default.Flag,
                            title = "Goal Progress",
                            subtitle = "Milestone achievements",
                            checked = s.notificationsGoals,
                            onCheckedChange = { vm.updateSetting(s) { st -> st.copy(notificationsGoals = it) } }
                        )
                        HorizontalDivider(color = CardColor, modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsSwitchRow(
                            icon = Icons.Default.Event,
                            title = "Daily Reminder",
                            subtitle = "Log your daily expenses",
                            checked = s.notificationsDailyReminder,
                            onCheckedChange = { vm.updateSetting(s) { st -> st.copy(notificationsDailyReminder = it) } }
                        )
                    }
                }

                item {
                    SettingsSection(title = "Security & Privacy") {
                        SettingsSwitchRow(
                            icon = Icons.Default.Dialpad,
                            title = "Require PIN",
                            subtitle = "App lock on startup",
                            checked = s.pinEnabled,
                            onCheckedChange = { vm.updateSetting(s) { st -> st.copy(pinEnabled = it) } }
                        )
                        HorizontalDivider(color = CardColor, modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsSwitchRow(
                            icon = Icons.Default.Fingerprint,
                            title = "Biometric Authentication",
                            subtitle = "Use fingerprint or face unlock",
                            checked = s.biometricEnabled,
                            onCheckedChange = { vm.updateSetting(s) { st -> st.copy(biometricEnabled = it) } }
                        )
                    }
                }
            } ?: run {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Primary)
                    }
                }
            }

            // Updates
            item {
                SettingsSection(title = "About & Updates") {
                    val currentVersion = com.example.bluff.BuildConfig.VERSION_NAME
                    var isChecking by remember { mutableStateOf(false) }
                    var updateInfo by remember { mutableStateOf<com.example.bluff.data.repository.UpdateInfo?>(null) }

                    SettingsActionRow(
                        icon = Icons.Default.SystemUpdate,
                        title = "Check for Updates",
                        subtitle = "Current version: $currentVersion",
                        isLoading = isChecking,
                        onClick = {
                            isChecking = true
                            vm.checkForUpdates { result ->
                                updateInfo = result
                                isChecking = false
                            }
                        }
                    )

                    if (updateInfo != null) {
                        AlertDialog(
                            onDismissRequest = { updateInfo = null },
                            containerColor = CardColor,
                            title = { Text(if (updateInfo!!.hasUpdate) "Update Available!" else "Up to date", color = TextPrimary) },
                            text = {
                                Text(
                                    if (updateInfo!!.hasUpdate) "Version ${updateInfo!!.latestVersion} is available. Download and install now?"
                                    else "You are already on the latest version ($currentVersion).",
                                    color = TextSecondary
                                )
                            },
                            confirmButton = {
                                if (updateInfo!!.hasUpdate && updateInfo!!.downloadUrl != null) {
                                    TextButton(onClick = {
                                        vm.startUpdateDownload(updateInfo!!.downloadUrl!!)
                                        updateInfo = null
                                    }) {
                                        Text("Download", color = Primary)
                                    }
                                } else {
                                    TextButton(onClick = { updateInfo = null }) {
                                        Text("OK", color = Primary)
                                    }
                                }
                            },
                            dismissButton = {
                                if (updateInfo!!.hasUpdate) {
                                    TextButton(onClick = { updateInfo = null }) {
                                        Text("Cancel", color = TextSecondary)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Danger Zone
            item {
                SettingsSection(title = "Danger Zone") {
                    var showClearDialog by remember { mutableStateOf(false) }
                    
                    SettingsActionRow(
                        icon = Icons.Default.DeleteForever,
                        title = "Factory Reset App",
                        subtitle = "Permanently wipe all data",
                        iconColor = ExpenseColor,
                        titleColor = ExpenseColor,
                        onClick = { showClearDialog = true }
                    )
                    
                    if (showClearDialog) {
                        AlertDialog(
                            onDismissRequest = { showClearDialog = false },
                            containerColor = CardColor,
                            title = { Text("Factory Reset", color = TextPrimary) },
                            text = { Text("This will permanently delete ALL data (Categories, Accounts, Transactions) and restart the app. This cannot be undone.", color = TextSecondary) },
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
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            color = TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF1E1E1E),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextSecondary, fontSize = 13.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconColor: Color = TextSecondary,
    titleColor: Color = TextPrimary,
    isLoading: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = titleColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = TextSecondary, fontSize = 13.sp)
        }
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Primary, strokeWidth = 2.dp)
        }
    }
}

@Composable
private fun PermissionWarningCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Primary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Notifications not allowed", color = Primary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("Allow", color = Primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
