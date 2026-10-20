package com.dailytown.app.onboarding

import android.content.Context

/**
 * First-run state only (no gameplay progress). Kept separate from the progress DataStore so a
 * progress reset never replays onboarding, and onboarding can be replayed from Settings without
 * touching progress.
 */
class OnboardingStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isCompleted(): Boolean = preferences.getBoolean(KEY_COMPLETED, false)

    fun markCompleted() {
        preferences.edit().putBoolean(KEY_COMPLETED, true).apply()
    }

    fun reset() {
        preferences.edit().remove(KEY_COMPLETED).apply()
    }

    companion object {
        const val PREFS_NAME = "daily_town_onboarding"
        const val KEY_COMPLETED = "first_run_completed_v1"
    }
}
