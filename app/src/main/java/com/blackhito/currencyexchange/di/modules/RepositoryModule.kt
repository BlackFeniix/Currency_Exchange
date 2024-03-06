package com.blackhito.currencyexchange.di.modules

import com.blackhito.data.repository.currency.CurrencyRepositoryImpl
import com.blackhito.domain.CurrencyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun provideRepository(impl: CurrencyRepositoryImpl): CurrencyRepository
}