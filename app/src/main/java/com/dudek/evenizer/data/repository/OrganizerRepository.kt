package com.dudek.evenizer.data.repository

import android.content.Context
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.dudek.evenizer.data.local.AppDatabase
import com.dudek.evenizer.data.local.entity.toData
import com.dudek.evenizer.data.network.di.NetworkModule
import com.dudek.evenizer.data.network.model.OrganizerData
import com.dudek.evenizer.data.paging.OrganizerRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OrganizerRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val service = NetworkModule.getOrganizerService(context)

    @OptIn(ExperimentalPagingApi::class)
    fun getOrganizers(search: String? = null): Flow<PagingData<OrganizerData>> {
        val pagingSourceFactory = { database.organizerDao().getOrganizersByType("ALL") }

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            remoteMediator = OrganizerRemoteMediator(
                database = database,
                service = service,
                type = "ALL",
                search = search
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { it.toData() }
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getMyOrganizers(search: String? = null): Flow<PagingData<OrganizerData>> {
        val pagingSourceFactory = { database.organizerDao().getOrganizersByType("MY") }

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            remoteMediator = OrganizerRemoteMediator(
                database = database,
                service = service,
                type = "MY",
                search = search
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { it.toData() }
        }
    }
}
