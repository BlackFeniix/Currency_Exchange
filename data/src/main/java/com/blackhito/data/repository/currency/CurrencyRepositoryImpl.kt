package com.blackhito.data.repository.currency

import com.blackhito.data.api.CurrencyApi
import com.blackhito.domain.CurrencyRepository
import com.blackhito.domain.currency.CurrencyListDomain
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(private val api: CurrencyApi) : CurrencyRepository {
    override suspend fun loadCurrency(): CurrencyListDomain = api.getCurrencyList().toDomain()
}