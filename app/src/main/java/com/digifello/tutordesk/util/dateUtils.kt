package com.digifello.tutordesk.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun currentMonthKey(): String {
    val formatter = SimpleDateFormat("yyyy-MM", Locale.US)
    return formatter.format(Date())
}