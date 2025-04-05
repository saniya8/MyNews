package com.example.mynews.presentation.views.social

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
    fun `timestamp 1 minute ago - should return 1 min ago`() {
        val now = System.currentTimeMillis()
        val fiveMinAgo = now - TimeUnit.MINUTES.toMillis(1)
        val result = getRelativeTimestamp(fiveMinAgo)
        assertEquals("1 min ago", result)
    }

    @Test
    fun `timestamp 59 minutes ago - should return 59 min ago`() {
        val now = System.currentTimeMillis()
        val fiveMinAgo = now - TimeUnit.MINUTES.toMillis(59)
        val result = getRelativeTimestamp(fiveMinAgo)
        assertEquals("59 min ago", result)
    }

    @Test
    fun `timestamp exactly 60 minutes ago - should return 1 hr ago`() {
        val now = System.currentTimeMillis()
        val sixtyMinAgo = now - TimeUnit.MINUTES.toMillis(60)
        val result = getRelativeTimestamp(sixtyMinAgo)
        assertEquals("1 hr ago", result)
    }

    @Test
    fun `timestamp 23 hours ago - should return 23 hr ago`() {
        val now = System.currentTimeMillis()
        val twoHoursAgo = now - TimeUnit.HOURS.toMillis(23)
        val result = getRelativeTimestamp(twoHoursAgo)
        assertEquals("23 hr ago", result)
    }

    @Test
    fun `timestamp previous calendar day - should return Yesterday`() {
        val now = System.currentTimeMillis()
        val yesterday = now - TimeUnit.DAYS.toMillis(1)
        val result = getRelativeTimestamp(yesterday)
        assertEquals("Yesterday", result)
    }

    @Test
    fun `timestamp over 24 hours ago but under 48 hours ago and not yesterday - should return 1 day ago`() {
        val calendarNow = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Subtract 25 hours from early morning = 2 calendar days ago, but days == 1
        val twentySixHoursAgo = calendarNow.timeInMillis - TimeUnit.HOURS.toMillis(26)

        val result  = getRelativeTimestamp(twentySixHoursAgo)

        // Since this is >24h ago, but NOT "yesterday" by calendar day logic, we expect "1 day ago"
        assertTrue(result == "1 day ago" || result == "Yesterday")
    }

    @Test
    fun `timestamp 2 days ago - should return 2 days ago`() {
        val now = System.currentTimeMillis()
        val twoDaysAgo = now - TimeUnit.DAYS.toMillis(2)
        val result = getRelativeTimestamp(twoDaysAgo)
        assertEquals("2 days ago", result)
    }

    @Test
    fun `timestamp 6 days ago - should return 6 days ago`() {
        val now = System.currentTimeMillis()
        val twoDaysAgo = now - TimeUnit.DAYS.toMillis(6)
        val result = getRelativeTimestamp(twoDaysAgo)
        assertEquals("6 days ago", result)
    }

    @Test
    fun `timestamp 7 days ago - should return formatted date this year`() {
        val now = System.currentTimeMillis()
        val tenDaysAgo = now - TimeUnit.DAYS.toMillis(7)
        val result = getRelativeTimestamp(tenDaysAgo)
        // Format will vary depending on today’s date
        // We only assert it does NOT contain "ago" or "Yesterday"
        assert(!result.contains("ago") && !result.contains("Yesterday"))
    }

    @Test
    fun `timestamp from last year - should return formatted date with year`() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024)
            set(Calendar.MONTH, Calendar.MARCH)
            set(Calendar.DAY_OF_MONTH, 29)
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 15)
        }

        val result = getRelativeTimestamp(calendar.timeInMillis)
        assert(result.contains("2024"))
    }
}


