package com.blackhito.data.api

import com.blackhito.data.repository.currency.CurrencyListNetwork
import retrofit2.http.GET

interface CurrencyApi {

    @GET("daily_json.js")
    suspend fun getCurrencyList(): CurrencyListNetwork
}