package com.dudek.evenizer.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dudek.evenizer.data.local.entity.OrganizerEntity

@Dao
interface OrganizerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(organizers: List<OrganizerEntity>)

    @Query("SELECT * FROM organizers WHERE type = :type ORDER BY createdAt DESC")
    fun getOrganizersByType(type: String): PagingSource<Int, OrganizerEntity>

    @Query("DELETE FROM organizers WHERE type = :type")
    suspend fun clearOrganizersByType(type: String)

    @Query("SELECT * FROM organizers WHERE uuid = :uuid")
    suspend fun getOrganizerByUuid(uuid: String): OrganizerEntity?
}
