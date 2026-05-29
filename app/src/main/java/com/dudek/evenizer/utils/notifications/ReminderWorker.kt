package com.dudek.evenizer.utils.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Event Reminder"
        val message = inputData.getString("message") ?: "You have an event coming up!"
        val notificationId = inputData.getInt("id", System.currentTimeMillis().toInt())

        NotificationHelper.showScheduleReminder(applicationContext, notificationId, title, message)
        return Result.success()
    }
}
