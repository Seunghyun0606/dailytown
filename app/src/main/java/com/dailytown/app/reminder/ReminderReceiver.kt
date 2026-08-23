package com.dailytown.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        LocalReminderManager(context).apply {
            postReminder()
            restoreIfEnabled()
        }
    }
}
