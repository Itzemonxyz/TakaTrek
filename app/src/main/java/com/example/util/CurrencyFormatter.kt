package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CurrencyFormatter {
    fun formatCurrency(amount: Double, isUsd: Boolean): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        val decimalFormat = DecimalFormat("#,##0.00", symbols)
        val prefix = if (isUsd) "$" else "৳"
        return prefix + decimalFormat.format(amount)
    }
}
