package com.example.bluff.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.bluff.domain.model.Tag

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String,
    val updatedAt: String
) {
    fun toModel() = Tag(
        id = id,
        name = name,
        color = color,
        updatedAt = updatedAt
    )

    companion object {
        fun fromModel(model: Tag) = TagEntity(
            id = model.id,
            name = model.name,
            color = model.color,
            updatedAt = model.updatedAt
        )
    }
}
