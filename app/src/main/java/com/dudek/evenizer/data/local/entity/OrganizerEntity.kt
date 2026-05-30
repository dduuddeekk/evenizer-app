package com.dudek.evenizer.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dudek.evenizer.data.network.model.OrganizerData

@Entity(tableName = "organizers")
data class OrganizerEntity(
    @PrimaryKey val uuid: String,
    val name: String,
    val status: String,
    val isVerified: Boolean,
    val isPublic: Boolean,
    val description: String?,
    val logo: String?,
    val createdAt: String,
    val updatedAt: String,
    val userUuid: String?,
    val followCount: Int,
    val isFollow: Boolean,
    val type: String // "ALL" or "MY"
)

fun OrganizerData.toEntity(type: String): OrganizerEntity {
    return OrganizerEntity(
        uuid = uuid,
        name = name,
        status = status,
        isVerified = isVerified,
        isPublic = isPublic,
        description = description,
        logo = logo,
        createdAt = createdAt,
        updatedAt = updatedAt,
        userUuid = userUuid,
        followCount = followCount,
        isFollow = isFollow,
        type = type
    )
}

fun OrganizerEntity.toData(): OrganizerData {
    return OrganizerData(
        uuid = uuid,
        name = name,
        status = status,
        isVerified = isVerified,
        isPublic = isPublic,
        description = description,
        logo = logo,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = null,
        userUuid = userUuid,
        followCount = followCount,
        isFollow = isFollow,
        roles = null,
        organizerMembers = null,
        organizerLocations = null,
        count = null
    )
}
