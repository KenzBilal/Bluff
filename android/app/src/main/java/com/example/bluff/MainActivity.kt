package com.example.bluff

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.bluff.data.seed.DefaultDataSeeder
import com.example.bluff.di.AppContainer
import com.example.bluff.theme.BluffTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val permissionsToRequest = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            permissionsLauncher.launch(missingPermissions.toTypedArray())
        }

        lifecycleScope.launch {
            val container = AppContainer.instance
            DefaultDataSeeder(container.categoryRepository, container.accountRepository).seedIfNeeded()
        }

        setContent {
            com.example.bluff.theme.BluffTheme {
                var updateInfo by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.example.bluff.data.repository.UpdateInfo?>(null) }

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    try {
                        val repo = com.example.bluff.data.repository.UpdaterRepository(this@MainActivity)
                        val result = repo.checkForUpdates()
                        if (result.hasUpdate) {
                            updateInfo = result
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }

                if (updateInfo != null) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { updateInfo = null },
                        containerColor = com.example.bluff.theme.CardColor,
                        title = { androidx.compose.material3.Text("New Update Available!", color = com.example.bluff.theme.TextPrimary) },
                        text = {
                            androidx.compose.material3.Text(
                                "Version ${updateInfo!!.latestVersion} is out. You are currently on ${com.example.bluff.BuildConfig.VERSION_NAME}. Download and install now?",
                                color = com.example.bluff.theme.TextSecondary
                            )
                        },
                        confirmButton = {
                            if (updateInfo!!.downloadUrl != null) {
                                androidx.compose.material3.TextButton(onClick = {
                                    com.example.bluff.data.repository.UpdaterRepository(this@MainActivity)
                                        .startDownload(updateInfo!!.downloadUrl!!)
                                    updateInfo = null
                                }) {
                                    androidx.compose.material3.Text("Download", color = com.example.bluff.theme.Primary)
                                }
                            }
                        },
                        dismissButton = {
                            androidx.compose.material3.TextButton(onClick = { updateInfo = null }) {
                                androidx.compose.material3.Text("Later", color = com.example.bluff.theme.TextSecondary)
                            }
                        }
                    )
                }
            }
        }
    }
}
