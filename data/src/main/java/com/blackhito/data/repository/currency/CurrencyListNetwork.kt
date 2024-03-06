package com.blackhito.data.repository.currency

import com.blackhito.domain.currency.CurrencyListDomain
import com.google.gson.annotations.SerializedName

data class CurrencyListNetwork(
    @SerializedName("date")
    val date: String? = "",
    @SerializedName("PreviousDate")
    val previousDate: String? = "",
    @SerializedName("PreviousURL")
    val previousURL: String? = "",
    @SerializedName("Timestamp")
    val timestamp: String? = "",
    @SerializedName("Valute")
    val valutes: Map<String, CurrencyNetwork>?
)

fun CurrencyListNetwork.toDomain(): CurrencyListDomain = CurrencyListDomain(
    valutes = valutes?.mapValues { (_, currencyNetwork ) ->
        currencyNetwork.toDomain()
    } ?: emptyMap()
)