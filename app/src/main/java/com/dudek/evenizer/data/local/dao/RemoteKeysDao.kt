package com.dudek.evenizer.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dudek.evenizer.data.local.entity.RemoteKeys

@Dao
interface RemoteKeysDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<RemoteKeys>)

    @Query("SELECT * FROM remote_keys WHERE uuid = :uuid AND type = :type")
    suspend fun remoteKeysByUuid(uuid: String, type: String): RemoteKeys?

    @Query("DELETE FROM remote_keys WHERE type = :type")
    suspend fun clearRemoteKeys(type: String)
}
