package com.example.bluff.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.bluff.data.local.BluffDatabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val db: BluffDatabase,
    private val supabase: SupabaseClient
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val operations = db.syncQueueDao().getPendingOperations()
        
        if (operations.isEmpty()) {
            return Result.success()
        }

        var allSuccessful = true

        for (operation in operations) {
            try {
                when (operation.operationType) {
                    "INSERT" -> {
                        val payload = operation.payload?.let { Json.decodeFromString<JsonObject>(it) }
                        if (payload != null) {
                            supabase.postgrest[operation.tableName].insert(payload)
                        }
                    }
                    "UPDATE" -> {
                        val payload = operation.payload?.let { Json.decodeFromString<JsonObject>(it) }
                        if (payload != null) {
                            supabase.postgrest[operation.tableName].update(payload) {
                                filter { eq("id", operation.entityId) }
                            }
                        }
                    }
                    "DELETE" -> {
                        supabase.postgrest[operation.tableName].delete {
                            filter { eq("id", operation.entityId) }
                        }
                    }
                }
                
                // Success, delete from queue
                db.syncQueueDao().deleteOperation(operation.id)
                
            } catch (e: Exception) {
                allSuccessful = false
                db.syncQueueDao().incrementRetryCount(operation.id, e.message)
            }
        }
        
        // Cleanup failed operations
        db.syncQueueDao().deleteFailedOperations()

        return if (allSuccessful) Result.success() else Result.retry()
    }
}
