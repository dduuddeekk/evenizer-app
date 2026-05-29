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
import com.dudek.evenizer.data.network.model.EventData
import com.dudek.evenizer.data.paging.EventRemoteMediator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EventRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val service = NetworkModule.getEventService(context)

    @OptIn(ExperimentalPagingApi::class)
    fun getEvents(search: String? = null, category: String? = null): Flow<PagingData<EventData>> {
        val pagingSourceFactory = { database.eventDao().getEventsByType("ALL") }

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            remoteMediator = EventRemoteMediator(
                database = database,
                service = service,
                type = "ALL",
                search = search,
                category = category
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { it.toData() }
        }
    }

    @OptIn(ExperimentalPagingApi::class)
    fun getMyEvents(search: String? = null, category: String? = null): Flow<PagingData<EventData>> {
        val pagingSourceFactory = { database.eventDao().getEventsByType("MY") }

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                enablePlaceholders = false
            ),
            remoteMediator = EventRemoteMediator(
                database = database,
                service = service,
                type = "MY",
                search = search,
                category = category
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map { it.toData() }
        }
    }
}
