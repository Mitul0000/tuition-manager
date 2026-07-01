package com.digifello.tuitionmanager.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtils {

    /**
     * Returns the current month as a Firestore-friendly key, e.g. "2026-07".
     * Used as the document ID for payment records, and to detect month
     * changes during the monthly maintenance routine.
     */
    fun currentMonthKey(): String {
        val format = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return format.format(Calendar.getInstance().time)
    }

    /**
     * Returns today's weekday name matching how we store batch days,
     * e.g. "Monday". Used by the Today screen to filter batches.
     */
    fun currentDayName(): String {
        val format = SimpleDateFormat("EEEE", Locale.getDefault())
        return format.format(Calendar.getInstance().time)
    }

    /**
     * Formats today's date for display, e.g. "October 24, 2026" —
     * used in the Today screen header.
     */
    fun formattedTodayDate(): String {
        val format = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        return format.format(Calendar.getInstance().time)
    }
}