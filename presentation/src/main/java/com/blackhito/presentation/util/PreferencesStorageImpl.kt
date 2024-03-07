package com.blackhito.presentation.util

import android.content.Context
import com.blackhito.domain.preferences_storage.PreferencesStorage
import com.blackhito.domain.preferences_storage.UserBalanceDomain
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject


class PreferencesStorageImpl @Inject constructor(@ApplicationContext  context: Context) : PreferencesStorage {

    private val prefs = context.getSharedPreferences(PREF_KEY, 0)

    override fun getUserBalance(): UserBalanceDomain {
        val usdCurrency = prefs.getFloat(USD_CURRENCY, 100.0f)
        val jpyCurrency = prefs.getFloat(JPY_CURRENCY, 100.0f)
        val tryCurrency = prefs.getFloat(TRY_CURRENCY, 100.0f)
        val rsdCurrency = prefs.getFloat(RSD_CURRENCY, 100.0f)
        val cadCurrency = prefs.getFloat(CAD_CURRENCY, 100.0f)
        return UserBalanceDomain(
            usdCurrency.toDouble(),
            jpyCurrency.toDouble(),
            tryCurrency.toDouble(),
            rsdCurrency.toDouble(),
            cadCurrency.toDouble()
        )
    }

    override fun updateUserBalance(userBalance: UserBalanceDomain) {
        prefs.edit()
            .putFloat(USD_CURRENCY, userBalance.usdCurrency.toFloat())
            .putFloat(JPY_CURRENCY, userBalance.jpyCurrency.toFloat())
            .putFloat(TRY_CURRENCY, userBalance.tryCurrency.toFloat())
            .putFloat(RSD_CURRENCY, userBalance.rsdCurrency.toFloat())
            .putFloat(CAD_CURRENCY, userBalance.cadCurrency.toFloat())
            .apply()
    }

    companion object {
        const val PREF_KEY = "SHARED_PREFERENCES"
        const val USD_CURRENCY = "usdCurrency"
        const val JPY_CURRENCY = "jpyCurrency"
        const val TRY_CURRENCY = "tryCurrency"
        const val RSD_CURRENCY = "rsdCurrency"
        const val CAD_CURRENCY = "cadCurrency"
    }
}