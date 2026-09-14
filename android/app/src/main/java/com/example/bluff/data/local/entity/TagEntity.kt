package com.example.bluff.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bluff.domain.model.Tag

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val color: String,
    val createdAt: Long
) {
    fun toModel() = Tag(
        id = id,
        userId = userId,
        name = name,
        color = color,
        createdAt = createdAt
    )

    companion object {
        fun fromModel(model: Tag) = TagEntity(
            id = model.id,
            userId = model.userId,
            name = model.name,
            color = model.color,
            createdAt = model.createdAt
        )
    }
}
