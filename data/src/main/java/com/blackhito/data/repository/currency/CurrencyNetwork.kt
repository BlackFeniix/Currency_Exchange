package com.blackhito.data.repository.currency

import com.blackhito.domain.currency.CurrencyDomain
import com.google.gson.annotations.SerializedName

data class CurrencyNetwork(
    @SerializedName("ID")
    val id: String?,

    @SerializedName("NumCode")
    val numCode: String?,

    @SerializedName("CharCode")
    val charCode: String?,

    @SerializedName("Nominal")
    val nominal: Int?,

    @SerializedName("Name")
    val name: String?,

    @SerializedName("Value")
    val value: Double?,

    @SerializedName("Previous")
    val previous: Double?
)

fun CurrencyNetwork.toDomain(): CurrencyDomain = CurrencyDomain(
    charCode = charCode ?: "",
    nominal = nominal ?: -1,
    name = name ?: "",
    value = value ?: 0.0
)