package com.blackhito.presentation.model

import com.blackhito.domain.currency.CurrencyDomain

data class CurrencyPresentation(
    val name: String,
    val charCode: String,
    var baseValue: String = "",
    //var userInputAmount: Double = 0.0
)

fun CurrencyDomain.toPresentation() = CurrencyPresentation(
    name = name,
    charCode = charCode,
    baseValue = (value/nominal.toFloat()).toString()
)