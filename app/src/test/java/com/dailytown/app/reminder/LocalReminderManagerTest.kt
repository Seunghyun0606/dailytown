package com.dailytown.app.reminder

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class LocalReminderManagerTest {
    private val zone = ZoneId.of("Asia/Seoul")

    @Test
    fun `reminder later today stays on the same date`() {
        val now = ZonedDateTime.of(2026, 8, 23, 18, 20, 0, 0, zone)
        val next = LocalReminderManager.nextTrigger(now, 19, 0)

        assertEquals(2026, next.year)
        assertEquals(8, next.monthValue)
        assertEquals(23, next.dayOfMonth)
        assertEquals(19, next.hour)
        assertEquals(0, next.minute)
    }

    @Test
    fun `reminder time already passed schedules tomorrow`() {
        val now = ZonedDateTime.of(2026, 8, 23, 20, 0, 0, 0, zone)
        val next = LocalReminderManager.nextTrigger(now, 19, 0)

        assertEquals(24, next.dayOfMonth)
        assertEquals(19, next.hour)
        assertEquals(0, next.minute)
    }
}
