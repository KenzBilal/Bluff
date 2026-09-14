package com.example.bluff.ui.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateFormatter {
    private val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy").withZone(ZoneId.systemDefault())
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

    fun format(timestamp: Long): String {
        return formatter.format(Instant.ofEpochMilli(timestamp))
    }
    
    fun formatTime(timestamp: Long): String {
        return timeFormatter.format(Instant.ofEpochMilli(timestamp))
    }
}
