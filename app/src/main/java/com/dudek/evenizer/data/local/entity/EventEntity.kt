package com.dudek.evenizer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dudek.evenizer.data.network.model.EventData

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val uuid: String,
    val title: String,
    val start: String,
    val end: String,
    val status: String,
    val isPublic: Boolean,
    val banner: String?,
    val description: String,
    val createdAt: String,
    val updatedAt: String,
    val isFavorited: Boolean,
    val type: String // "ALL" or "MY"
)

fun EventData.toEntity(type: String): EventEntity {
    return EventEntity(
        uuid = uuid,
        title = title,
        start = start,
        end = end,
        status = status,
        isPublic = isPublic,
        banner = banner,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFavorited = isFavorited,
        type = type
    )
}

fun EventEntity.toData(): EventData {
    return EventData(
        uuid = uuid,
        title = title,
        start = start,
        end = end,
        status = status,
        isPublic = isPublic,
        banner = banner,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isFavorited = isFavorited
    )
}
