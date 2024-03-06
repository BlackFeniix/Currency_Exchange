package com.blackhito.domain.currency

data class CurrencyDomain(
    val charCode: String,
    val nominal: Int,
    val name: String,
    val value: Double
)