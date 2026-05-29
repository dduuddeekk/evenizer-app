package com.dudek.evenizer.utils.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dudek.evenizer.MainActivity
import com.dudek.evenizer.R
import java.util.concurrent.TimeUnit

object NotificationHelper {
    private const val CHANNEL_ID_REMINDERS = "schedule_reminders"
    private const val CHANNEL_ID_UPDATES = "app_updates"

    fun scheduleReminder(context: Context, id: Int, title: String, message: String, delayMillis: Long) {
        if (delayMillis <= 0) return

        val data = Data.Builder()
            .putInt("id", id)
            .putString("title", title)
            .putString("message", message)
            .build()

        val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("reminder_$id")
            .build()

        WorkManager.getInstance(context).enqueue(reminderRequest)
    }

    fun cancelReminder(context: Context, id: Int) {
        WorkManager.getInstance(context).cancelAllWorkByTag("reminder_$id")
    }

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nameReminders = "Schedule Reminders"
            val descReminders = "Notifications for upcoming events in your schedule"
            val importanceReminders = NotificationManager.IMPORTANCE_HIGH
            val channelReminders = NotificationChannel(CHANNEL_ID_REMINDERS, nameReminders, importanceReminders).apply {
                description = descReminders
            }

            val nameUpdates = "App Updates"
            val descUpdates = "Notifications for new messages or event requests"
            val importanceUpdates = NotificationManager.IMPORTANCE_DEFAULT
            val channelUpdates = NotificationChannel(CHANNEL_ID_UPDATES, nameUpdates, importanceUpdates).apply {
                description = descUpdates
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channelReminders)
            notificationManager.createNotificationChannel(channelUpdates)
        }
    }

    fun showScheduleReminder(context: Context, id: Int, title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(id, builder.build())
    }

    fun showAppUpdate(context: Context, id: Int, title: String, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_UPDATES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(id, builder.build())
    }
}
