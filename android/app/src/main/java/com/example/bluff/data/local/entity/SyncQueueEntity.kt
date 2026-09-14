package com.example.bluff.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val operationType: String, // INSERT, UPDATE, DELETE
    val tableName: String,
    val entityId: String,
    val payload: String?, // JSON payload
    val createdAt: Long,
    val retryCount: Int = 0,
    val lastError: String? = null
)
