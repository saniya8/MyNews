package com.example.mynews.presentation.views.social

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

class GetRelativeTimestampTest {

    @Test
    fun `timestamp from now - should return Just now`() {
        val now = System.currentTimeMillis()
        val result = getRelativeTimestamp(now)
        assertEquals("Just now", result)
    }

    @Test
    fun `timestamp 5 minutes ago - should return 5 min ago`() {
        val now = System.currentTimeMillis()
        val fiveMinAgo = now - TimeUnit.MINUTES.toMillis(5)
        val result = getRelativeTimestamp(fiveMinAgo)
        assertEquals("5 min ago", result)
    }

    @Test
    fun `timestamp 2 hours ago - should return 2 hr ago`() {
        val now = System.currentTimeMillis()
        val twoHoursAgo = now - TimeUnit.HOURS.toMillis(2)
        val result = getRelativeTimestamp(twoHoursAgo)
        assertEquals("2 hr ago", result)
    }

    @Test
    fun `timestamp 1 day ago - should return Yesterday`() {
        val now = System.currentTimeMillis()
        val yesterday = now - TimeUnit.DAYS.toMillis(1)
        val result = getRelativeTimestamp(yesterday)
        assertEquals("Yesterday", result)
    }

    @Test
    fun `timestamp 3 days ago - should return 3 days ago`() {
        val now = System.currentTimeMillis()
        val threeDaysAgo = now - TimeUnit.DAYS.toMillis(3)
        val result = getRelativeTimestamp(threeDaysAgo)
        assertEquals("3 days ago", result)
    }

    @Test
    fun `timestamp 10 days ago - should return formatted date this year`() {
        val now = System.currentTimeMillis()
        val tenDaysAgo = now - TimeUnit.DAYS.toMillis(10)
        val result = getRelativeTimestamp(tenDaysAgo)
        // Format will vary depending on today’s date
        // We only assert it does NOT contain "ago" or "Yesterday"
        assert(!result.contains("ago") && !result.contains("Yesterday"))
    }

    @Test
    fun `timestamp from last year - should return formatted date with year`() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2023)
            set(Calendar.MONTH, Calendar.MARCH)
            set(Calendar.DAY_OF_MONTH, 29)
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 15)
        }

        val result = getRelativeTimestamp(calendar.timeInMillis)
        assert(result.contains("2023"))
    }
}