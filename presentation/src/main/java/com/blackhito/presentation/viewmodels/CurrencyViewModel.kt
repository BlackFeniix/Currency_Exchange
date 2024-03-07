package com.blackhito.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackhito.domain.CurrencyRepository
import com.blackhito.domain.NetworkState
import com.blackhito.domain.currency.CurrencyListDomain
import com.blackhito.presentation.model.CurrencyPresentation
import com.blackhito.presentation.model.TransactionStatus
import com.blackhito.presentation.model.toPresentation
import com.blackhito.presentation.util.PreferencesStorageImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val repository: CurrencyRepository,
    private val prefStorage: PreferencesStorageImpl
) : ViewModel() {

    private val _currencyListLiveData = MutableStateFlow(CurrencyListDomain(emptyMap()))

    private val _firstCurrencyPresentation = MutableLiveData<CurrencyPresentation>()
    val firstCurrencyPresentation: LiveData<CurrencyPresentation>
        get() = _firstCurrencyPresentation

    private val _secondCurrencyPresentation = MutableLiveData<CurrencyPresentation>()
    val secondCurrencyPresentation: LiveData<CurrencyPresentation>
        get() = _secondCurrencyPresentation

    private val _currentExchangeRatioFirst = MutableLiveData<Double>()
    val currentExchangeRatioFirst: LiveData<Double>
        get() = _currentExchangeRatioFirst

    private val _currentExchangeRatioSecond = MutableLiveData(0.0)
    val currentExchangeRatioSecond: LiveData<Double>
        get() = _currentExchangeRatioSecond

    private var firstList: MutableList<CurrencyPresentation> = mutableListOf()
    private var secondList: MutableList<CurrencyPresentation> = mutableListOf()

    private var firstActivePosition = 0
    private var secondActivePosition = 0

    private var userBalance = prefStorage.getUserBalance()

    private val _networkStateFlow = MutableStateFlow<NetworkState>(NetworkState.NetworkDownload())
    val networkStateFlow: StateFlow<NetworkState>
        get() = _networkStateFlow.asStateFlow()

    private val _transactionStatus = MutableSharedFlow<TransactionStatus>()
    val transactionStatus: SharedFlow<TransactionStatus>
        get() = _transactionStatus.asSharedFlow()


    fun loadCurrencyFromNetwork() {
        viewModelScope.launch {
            try {
                val currencyListDomain = repository.loadCurrency()
                    .valutes.filterKeys {
                        currencyListName.contains(
                            it
                        )
                    }
                if (currencyListDomain.isNotEmpty()) {

                    firstList.addAll(currencyListDomain.values.map { it.toPresentation() })
                    secondList.addAll(currencyListDomain.values.map { it.toPresentation() })

                    updateExchangeRatio()
                    _networkStateFlow.value = NetworkState.NetworkSuccess()
                } else {
                    _networkStateFlow.value = NetworkState.NetworkError()
                }

            } catch (e: Exception) {
                _networkStateFlow.value = NetworkState.NetworkError()
            }
        }
    }

    fun getUserBalance() {
        userBalance = prefStorage.getUserBalance()
    }

    fun updateUserBalance() = viewModelScope.launch {
        firstCurrencyPresentation.value?.let {
            if (it.charCode == secondCurrencyPresentation.value?.charCode) {
                _transactionStatus.emit(TransactionStatus.ErrorSameCurrency)
                return@launch
            }
            if (it.userInputAmount == 0.0) {
                _transactionStatus.emit(TransactionStatus.ErrorEmptyFields)
            }

            if (it.userBalance < it.userInputAmount)
                _transactionStatus.emit(TransactionStatus.ErrorNotEnoughCurrency)
            else {
                calculateUpdateForBalance()

                _transactionStatus.emit(TransactionStatus.Success(listOf("fdf")))
                prefStorage.updateUserBalance(userBalance)
                updateExchangeRatio()
            }
        }
    }

    private fun calculateUpdateForBalance() {
        val firstCurrency = firstCurrencyPresentation.value
        val secondCurrency = secondCurrencyPresentation.value
        userBalance = when (firstCurrency?.charCode) {
            "USD" -> {
                val newValue = prefStorage.getUserBalance().usdCurrency - firstCurrency.userInputAmount
                userBalance.copy(usdCurrency = newValue)
            }

            "JPY" -> {
                val newValue = prefStorage.getUserBalance().jpyCurrency - firstCurrency.userInputAmount
                userBalance.copy(jpyCurrency = newValue)
            }

            "TRY" -> {
                val newValue = prefStorage.getUserBalance().tryCurrency - firstCurrency.userInputAmount
                userBalance.copy(tryCurrency = newValue)
            }

            "RSD" -> {
                val newValue = prefStorage.getUserBalance().rsdCurrency - firstCurrency.userInputAmount
                userBalance.copy(rsdCurrency = newValue)
            }

            "CAD" -> {
                val newValue = prefStorage.getUserBalance().cadCurrency - firstCurrency.userInputAmount
                userBalance.copy(cadCurrency = newValue)
            }
            else -> userBalance
        }

        userBalance = when (secondCurrency?.charCode) {
            "USD" -> {
                val newValue = prefStorage.getUserBalance().usdCurrency + secondCurrency.userInputAmount
                userBalance.copy(usdCurrency = newValue)
            }

            "JPY" -> {
                val newValue = prefStorage.getUserBalance().jpyCurrency + secondCurrency.userInputAmount
                userBalance.copy(jpyCurrency = newValue)
            }

            "TRY" -> {
                val newValue = prefStorage.getUserBalance().tryCurrency + secondCurrency.userInputAmount
                userBalance.copy(tryCurrency = newValue)
            }

            "RSD" -> {
                val newValue = prefStorage.getUserBalance().rsdCurrency + secondCurrency.userInputAmount
                userBalance.copy(rsdCurrency = newValue)
            }

            "CAD" -> {
                val newValue = prefStorage.getUserBalance().cadCurrency + secondCurrency.userInputAmount
                userBalance.copy(cadCurrency = newValue)
            }
            else -> userBalance
        }
    }

    private fun updateExchangeRatio() {
        val firstCurrency = firstList[firstActivePosition].also {
            it.userBalance = when (it.charCode) {
                "USD" -> userBalance.usdCurrency
                "JPY" -> userBalance.jpyCurrency
                "TRY" -> userBalance.tryCurrency
                "RSD" -> userBalance.rsdCurrency
                "CAD" -> userBalance.cadCurrency
                else -> 0.0
            }
        }
        val secondCurrency = secondList[secondActivePosition].also {
            it.userBalance = when (it.charCode) {
                "USD" -> userBalance.usdCurrency
                "JPY" -> userBalance.jpyCurrency
                "TRY" -> userBalance.tryCurrency
                "RSD" -> userBalance.rsdCurrency
                "CAD" -> userBalance.cadCurrency
                else -> 0.0
            }
        }

        _currentExchangeRatioFirst.value =
            firstCurrency.exchangeRatio.toDouble() / secondCurrency.exchangeRatio.toDouble()

        _currentExchangeRatioSecond.value =
            secondCurrency.exchangeRatio.toDouble() / firstCurrency.exchangeRatio.toDouble()

        _firstCurrencyPresentation.value = firstCurrency
        _secondCurrencyPresentation.value = secondCurrency

    }

    fun updatePosition(newPosition: Int, flag: String) {
        when (flag) {
            FIRST_WINDOW -> firstActivePosition = newPosition
            SECOND_WINDOW -> secondActivePosition = newPosition
            else -> {}
        }
        updateExchangeRatio()
    }

    companion object {
        val currencyListName = listOf(
            "USD", "JPY", "TRY", "RSD", "CAD"
        )
        const val FIRST_WINDOW = "FIRST_WINDOW"
        const val SECOND_WINDOW = "SECOND_WINDOW"
    }
}