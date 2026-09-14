package com.example.bluff.domain.model

data class Tag(
    val id: String,
    val userId: String,
    val name: String,
    val color: String = "#6C63FF",
    val createdAt: Long = System.currentTimeMillis()
)
