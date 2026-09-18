package com.example.bluff.data.repository

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.example.bluff.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val hasUpdate: Boolean,
    val latestVersion: String,
    val downloadUrl: String?
)

class UpdaterRepository(private val context: Context) {

    suspend fun checkForUpdates(): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/KenzBilal/Bluff/releases/latest")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")

            if (connection.responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val tagName = json.getString("tag_name") // e.g. "v1.1" or "1.1"
                val cleanTagName = tagName.removePrefix("v")
                
                val currentVersion = BuildConfig.VERSION_NAME

                var downloadUrl: String? = null
                val assets = json.getJSONArray("assets")
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    if (asset.getString("name").endsWith(".apk")) {
                        downloadUrl = asset.getString("browser_download_url")
                        break
                    }
                }

                // Simple version string comparison (assuming Semantic Versioning format)
                val hasUpdate = isNewerVersion(currentVersion, cleanTagName)
                return@withContext UpdateInfo(hasUpdate, cleanTagName, downloadUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext UpdateInfo(false, BuildConfig.VERSION_NAME, null)
    }

    fun startDownload(downloadUrl: String) {
        val request = DownloadManager.Request(Uri.parse(downloadUrl))
            .setTitle("Bluff Update")
            .setDescription("Downloading latest version of Bluff...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Bluff_Update.apk")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
    }

    private fun isNewerVersion(current: String, fetched: String): Boolean {
        // Fallback for "latest" (since old action used it)
        if (fetched == "latest") return false
        
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val fetchedParts = fetched.split(".").mapNotNull { it.toIntOrNull() }

        val length = maxOf(currentParts.size, fetchedParts.size)
        for (i in 0 until length) {
            val c = currentParts.getOrElse(i) { 0 }
            val f = fetchedParts.getOrElse(i) { 0 }
            if (f > c) return true
            if (f < c) return false
        }
        return false
    }
}
