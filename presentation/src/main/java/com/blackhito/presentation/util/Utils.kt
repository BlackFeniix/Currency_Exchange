package com.blackhito.presentation.util

import android.icu.util.Currency
import android.util.TypedValue
import android.widget.EditText

object Utils {
    fun getCurrencySymbol(currencyName:String?): String {
        return Currency.getInstance(currencyName).symbol ?: "$"
    }

    fun setEditTextInputSize(length: Int, editText: EditText) {
        when(length) {
            in 0..5 -> editText.setTextSize(TypedValue.COMPLEX_UNIT_SP,48f)
            in 6..10 -> editText.setTextSize(TypedValue.COMPLEX_UNIT_SP,32f)
            in 11..15 -> editText.setTextSize(TypedValue.COMPLEX_UNIT_SP,24f)
            in 16..20 -> editText.setTextSize(TypedValue.COMPLEX_UNIT_SP,16f)
            else -> editText.setTextSize(TypedValue.COMPLEX_UNIT_SP,12f)
        }
    }
}