package com.example.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun formatCurrency(amount: Double, isUsd: Boolean): String {
        return if (isUsd) {
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            format.format(amount)
        } else {
            val format = NumberFormat.getCurrencyInstance(Locale("bn", "BD"))
            format.maximumFractionDigits = 2
            format.minimumFractionDigits = 2
            format.format(amount)
        }
    }
}
