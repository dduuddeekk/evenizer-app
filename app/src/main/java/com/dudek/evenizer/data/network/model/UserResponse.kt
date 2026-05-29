package com.dudek.evenizer.data.network.model

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: UserData? = null,
    val error: String? = null
)

@Serializable
data class UserListResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: UserListData? = null,
    val error: String? = null
)

@Serializable
data class UserListData(
    val data: List<UserData>,
    val meta: Meta
)

@Serializable
data class UserScheduleResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: List<YearSchedule> = emptyList()
)

@Serializable
data class YearSchedule(
    val year: Int,
    val months: List<MonthSchedule>
)

@Serializable
data class MonthSchedule(
    val month: Int,
    val days: List<DaySchedule>
)

@Serializable
data class DaySchedule(
    val day: Int,
    val rundowns: List<RundownData>
)
