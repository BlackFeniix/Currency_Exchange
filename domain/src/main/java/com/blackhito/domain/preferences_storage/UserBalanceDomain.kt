package com.blackhito.domain.preferences_storage

data class UserBalanceDomain(
    val usdCurrency: Double,
    val jpyCurrency: Double,
    val tryCurrency: Double,
    val rsdCurrency: Double,
    val cadCurrency: Double
)

fun UserBalanceDomain.getBalanceFromCharCode(charCode: String): Double {
    return when (charCode) {
        "USD" -> usdCurrency
        "JPY" -> jpyCurrency
        "TRY" -> tryCurrency
        "RSD" -> rsdCurrency
        "CAD" -> cadCurrency
        else -> 0.0
    }
}