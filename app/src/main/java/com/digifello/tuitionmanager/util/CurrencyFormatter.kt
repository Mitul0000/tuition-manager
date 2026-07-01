package com.digifello.tuitionmanager.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {

    /**
     * Formats a Double as Bangladeshi Taka, e.g. 8000.0 -> "৳8,000".
     */
    fun format(amount: Double): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale("en", "BD"))
        numberFormat.maximumFractionDigits = 0
        return "৳${numberFormat.format(amount)}"
    }
}