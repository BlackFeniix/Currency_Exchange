package com.blackhito.domain.preferences_storage

interface PreferencesStorage {
    fun getUserBalance():UserBalanceDomain

    fun updateUserBalance(userBalance: UserBalanceDomain)
}
