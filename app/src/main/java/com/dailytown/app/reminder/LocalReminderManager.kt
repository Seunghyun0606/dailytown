package com.dailytown.app.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.dailytown.app.MainActivity
import java.time.ZonedDateTime

data class ReminderPreference(
    val enabled: Boolean = false,
    val hour: Int = 19,
    val minute: Int = 0,
)

class LocalReminderManager(context: Context) {
    private val appContext = context.applicationContext
    private val preferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)
    private val notificationManager = appContext.getSystemService(NotificationManager::class.java)

    fun preference(): ReminderPreference = ReminderPreference(
        enabled = preferences.getBoolean(KEY_ENABLED, false),
        hour = preferences.getInt(KEY_HOUR, 19).coerceIn(0, 23),
        minute = preferences.getInt(KEY_MINUTE, 0).coerceIn(0, 59),
    )

    fun enable(hour: Int = 19, minute: Int = 0) {
        val sanitized = ReminderPreference(true, hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        preferences.edit()
            .putBoolean(KEY_ENABLED, true)
            .putInt(KEY_HOUR, sanitized.hour)
            .putInt(KEY_MINUTE, sanitized.minute)
            .apply()
        ensureChannel()
        scheduleNext(sanitized)
    }

    fun disable() {
        preferences.edit().putBoolean(KEY_ENABLED, false).apply()
        alarmManager.cancel(alarmPendingIntent())
    }

    fun restoreIfEnabled() {
        val pref = preference()
        if (pref.enabled) {
            ensureChannel()
            scheduleNext(pref)
        }
    }

    fun canPostNotifications(): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    fun postReminder() {
        val pref = preference()
        if (!pref.enabled || !canPostNotifications()) return
        ensureChannel()

        val openApp = PendingIntent.getActivity(
            appContext,
            REQUEST_OPEN_APP,
            Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = Notification.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Daily Town 탐험")
            .setContentText("오늘 동네를 걸을 일이 있다면 새로운 탐험을 확인해 보세요.")
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun scheduleNext(preference: ReminderPreference = preference(), now: ZonedDateTime = ZonedDateTime.now()) {
        if (!preference.enabled) return
        val next = nextTrigger(now, preference.hour, preference.minute)
        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            next.toInstant().toEpochMilli(),
            WINDOW_MILLIS,
            alarmPendingIntent(),
        )
    }

    private fun alarmPendingIntent(): PendingIntent = PendingIntent.getBroadcast(
        appContext,
        REQUEST_REMINDER,
        Intent(appContext, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun ensureChannel() {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "탐험 리마인더",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = "사용자가 직접 켠 Daily Town 탐험 리마인더"
            },
        )
    }

    companion object {
        internal const val WINDOW_MILLIS = 30 * 60 * 1000L
        private const val PREFS_NAME = "daily_town_reminder"
        private const val KEY_ENABLED = "enabled"
        private const val KEY_HOUR = "hour"
        private const val KEY_MINUTE = "minute"
        private const val CHANNEL_ID = "daily_town_exploration_reminder"
        private const val NOTIFICATION_ID = 4101
        private const val REQUEST_REMINDER = 4102
        private const val REQUEST_OPEN_APP = 4103

        fun nextTrigger(now: ZonedDateTime, hour: Int, minute: Int): ZonedDateTime {
            var next = now
                .withHour(hour.coerceIn(0, 23))
                .withMinute(minute.coerceIn(0, 59))
                .withSecond(0)
                .withNano(0)
            if (!next.isAfter(now)) next = next.plusDays(1)
            return next
        }
    }
}
