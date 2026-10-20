package com.dailytown.app

import androidx.test.core.app.ApplicationProvider
import com.dailytown.app.onboarding.OnboardingStore
import org.junit.rules.ExternalResource

/**
 * Puts the first-run guide into a known state before the activity under test launches.
 * Apply with a lower `order` than the compose/activity rule.
 */
class OnboardingStateRule(private val completed: Boolean = true) : ExternalResource() {
    override fun before() {
        val store = OnboardingStore(ApplicationProvider.getApplicationContext())
        if (completed) store.markCompleted() else store.reset()
    }
}
