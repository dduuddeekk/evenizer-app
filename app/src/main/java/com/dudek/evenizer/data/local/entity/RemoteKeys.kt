package com.dudek.evenizer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeys(
    @PrimaryKey val uuid: String,
    val prevKey: Int?,
    val nextKey: Int?,
    val type: String // To distinguish between different paginated lists
)
