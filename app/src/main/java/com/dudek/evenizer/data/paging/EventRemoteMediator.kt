package com.dudek.evenizer.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dudek.evenizer.data.local.AppDatabase
import com.dudek.evenizer.data.local.entity.EventEntity
import com.dudek.evenizer.data.local.entity.RemoteKeys
import com.dudek.evenizer.data.local.entity.toEntity
import com.dudek.evenizer.data.network.service.EventService

@OptIn(ExperimentalPagingApi::class)
class EventRemoteMediator(
    private val database: AppDatabase,
    private val service: EventService,
    private val type: String, // "ALL" or "MY"
    private val search: String? = null,
    private val category: String? = null
) : RemoteMediator<Int, EventEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EventEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: 1
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val response = if (type == "MY") {
                service.getMyEvents(page = page, limit = state.config.pageSize, search = search, category = category)
            } else {
                service.getAllEvents(page = page, limit = state.config.pageSize, search = search, category = category)
            }

            val events = response.data?.data ?: emptyList()
            val endOfPaginationReached = events.isEmpty() || (response.data?.meta?.page ?: 1) >= (response.data?.meta?.totalPages ?: 1)

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.remoteKeysDao().clearRemoteKeys(type)
                    database.eventDao().clearEventsByType(type)
                }
                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = events.map {
                    RemoteKeys(uuid = it.uuid, prevKey = prevKey, nextKey = nextKey, type = type)
                }
                database.remoteKeysDao().insertAll(keys)
                database.eventDao().insertAll(events.map { it.toEntity(type) })
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, EventEntity>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { event ->
                database.remoteKeysDao().remoteKeysByUuid(event.uuid, type)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, EventEntity>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { event ->
                database.remoteKeysDao().remoteKeysByUuid(event.uuid, type)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, EventEntity>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.uuid?.let { uuid ->
                database.remoteKeysDao().remoteKeysByUuid(uuid, type)
            }
        }
    }
}
