package com.blackhito.presentation.util

import android.icu.util.Currency

object Utils {
    fun getCurrencySymbol(currencyName:String?): String {
        return Currency.getInstance(currencyName).symbol ?: "$"
    }
}