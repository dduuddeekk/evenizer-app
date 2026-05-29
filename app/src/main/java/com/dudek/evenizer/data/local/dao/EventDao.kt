package com.dudek.evenizer.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dudek.evenizer.data.local.entity.EventEntity

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<EventEntity>)

    @Query("SELECT * FROM events WHERE type = :type ORDER BY createdAt DESC")
    fun getEventsByType(type: String): PagingSource<Int, EventEntity>

    @Query("DELETE FROM events WHERE type = :type")
    suspend fun clearEventsByType(type: String)

    @Query("SELECT * FROM events WHERE uuid = :uuid")
    suspend fun getEventByUuid(uuid: String): EventEntity?
}
