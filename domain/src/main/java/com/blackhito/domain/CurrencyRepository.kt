package com.blackhito.domain

import com.blackhito.domain.currency.CurrencyListDomain

interface CurrencyRepository {

    suspend fun loadCurrency(): CurrencyListDomain
}