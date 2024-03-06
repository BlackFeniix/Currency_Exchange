package com.blackhito.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackhito.domain.CurrencyRepository
import com.blackhito.domain.currency.CurrencyListDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val repository: CurrencyRepository
) : ViewModel() {
    private val _liveData = MutableLiveData<CurrencyListDomain>()
    val liveData: LiveData<CurrencyListDomain>
        get() = _liveData

    fun load() {
        viewModelScope.launch {
            var currencyListDomain = repository.loadCurrency()
            Log.e("LOG", currencyListDomain.toString())
            _liveData.value =
                CurrencyListDomain(currencyListDomain.valutes.filterKeys { valuteList.contains(it) })
        }
    }

    companion object {
        const val MAXIMUM_VALUTE_AMOUNT = 5
        val valuteList = listOf(
            "USD", "JPY", "TRY", "RSD", "CAD"
        )
    }
}