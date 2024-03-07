package com.blackhito.currencyexchange.di.modules

import com.blackhito.domain.preferences_storage.PreferencesStorage
import com.blackhito.presentation.util.PreferencesStorageImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


@Module
@InstallIn(SingletonComponent::class)
abstract class PreferenceModule {

    @Binds
    abstract fun providePreference(impl: PreferencesStorageImpl) : PreferencesStorage
}