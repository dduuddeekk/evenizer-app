package com.dudek.evenizer.utils.notifications

import android.content.Context
import com.dudek.evenizer.data.network.model.RundownData
import com.dudek.evenizer.data.network.model.YearSchedule
import java.text.SimpleDateFormat
import java.util.*

object ReminderManager {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    fun scheduleRemindersForSchedule(context: Context, schedule: List<YearSchedule>) {
        val now = System.currentTimeMillis()
        
        schedule.forEach { yearData ->
            yearData.months.forEach { monthData ->
                monthData.days.forEach { dayData ->
                    dayData.rundowns.forEach { rundown ->
                        scheduleRemindersForRundown(context, rundown, now)
                    }
                }
            }
        }
    }

    private fun scheduleRemindersForRundown(context: Context, rundown: RundownData, now: Long) {
        val startTime = try {
            isoFormat.parse(rundown.start)?.time ?: return
        } catch (e: Exception) {
            return
        }

        if (startTime <= now) return

        val title = "Upcoming Event: ${rundown.title}"
        val idBase = rundown.uuid.hashCode()

        // 2 days before
        val twoDaysMillis = 2 * 24 * 60 * 60 * 1000L
        scheduleIfFuture(context, idBase + 1, title, "Event starts in 2 days!", startTime - twoDaysMillis, now)

        // 1 day before
        val oneDayMillis = 24 * 60 * 60 * 1000L
        scheduleIfFuture(context, idBase + 2, title, "Event starts in 1 day!", startTime - oneDayMillis, now)

        // 30 mins before
        val thirtyMinsMillis = 30 * 60 * 1000L
        scheduleIfFuture(context, idBase + 3, title, "Event starts in 30 minutes!", startTime - thirtyMinsMillis, now)
    }

    private fun scheduleIfFuture(context: Context, id: Int, title: String, message: String, targetTime: Long, now: Long) {
        if (targetTime > now) {
            NotificationHelper.scheduleReminder(context, id, title, message, targetTime - now)
        }
    }
}
