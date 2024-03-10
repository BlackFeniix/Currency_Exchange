package com.blackhito.presentation.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blackhito.domain.CurrencyRepository
import com.blackhito.domain.NetworkState
import com.blackhito.domain.preferences_storage.PreferencesStorage
import com.blackhito.domain.preferences_storage.getBalanceFromCharCode
import com.blackhito.presentation.R
import com.blackhito.presentation.model.CurrencyPresentation
import com.blackhito.presentation.model.toPresentation
import com.blackhito.presentation.util.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: CurrencyRepository,
    private val prefStorage: PreferencesStorage
) : ViewModel() {

    private val _networkStateFlow = MutableStateFlow<NetworkState>(NetworkState.NetworkDownload())
    val networkStateFlow: StateFlow<NetworkState>
        get() = _networkStateFlow.asStateFlow()

    private val _listCurrency = MutableLiveData<List<CurrencyPresentation>>()
    val listCurrency: LiveData<List<CurrencyPresentation>> = _listCurrency

    private val _upperCurrency = MutableLiveData<CurrencyPresentation>()
    val upperCurrency: LiveData<CurrencyPresentation> = _upperCurrency

    private val _lowerCurrency = MutableLiveData<CurrencyPresentation>()
    val lowerCurrency: LiveData<CurrencyPresentation> = _lowerCurrency

    private val _textNotification = MutableLiveData<String>()
    val textNotification: LiveData<String> = _textNotification

    var upperPosition: Int = 0
    var lowerPosition: Int = 0

    var upperToLowerRatio = 0.0
    var lowerToUpperRatio = 0.0

    var upperFieldInput = ""
    var lowerFieldInput = ""

    private val _upperFieldInput = MutableLiveData("")
    val userUpperInput: LiveData<String> = _upperFieldInput
    private val _lowerFieldInput = MutableLiveData("")
    val userLowerInput: LiveData<String> = _lowerFieldInput


    var userBalance = prefStorage.getUserBalance()

    fun updateAllDataRegularly() = viewModelScope.launch {
        loadCurrencyFromNetwork()
        while (true) {
            delay(30000)

            val listOfCurrency =
                repository.loadCurrency()
                    .valutes.filterKeys { currencyListName.contains(it) }
                    .values.map { it.toPresentation() }

            Log.e("LOG", listOfCurrency.toString())
            _listCurrency.value = listOfCurrency

            updateCurrencyFromList()
            updateRatio(listCurrency.value?.get(0), listCurrency.value?.get(0))
        }
    }

    private fun updateRatio(
        upperNewValue: CurrencyPresentation?,
        lowerNewValue: CurrencyPresentation?
    ) {
        val upperBaseValue = upperNewValue?.baseValue?.toDouble() ?: 0.0
        val lowerBaseValue = lowerNewValue?.baseValue?.toDouble() ?: 0.0
        upperToLowerRatio = String.format("%.2f", upperBaseValue / lowerBaseValue).toDouble()
        lowerToUpperRatio = String.format("%.2f", lowerBaseValue / upperBaseValue).toDouble()
    }

    fun updateInputField(isFocusOnFirstWindow: Boolean) {
        Log.e("LOG", "$upperFieldInput $lowerFieldInput")
        if (isFocusOnFirstWindow) {
            lowerFieldInput = if (upperFieldInput.isEmpty())
                ""
            else {
                String.format("%.2f",upperFieldInput.toDouble() * upperToLowerRatio)
            }
            _lowerFieldInput.postValue(lowerFieldInput)
        } else {
            upperFieldInput = if (lowerFieldInput.isEmpty())
                ""
            else {
                String.format("%.2f",lowerFieldInput.toDouble() * lowerToUpperRatio)
            }
            _upperFieldInput.postValue(upperFieldInput)
        }
        Log.e("LOG", "$upperFieldInput $lowerFieldInput")
    }

    private fun loadCurrencyFromNetwork() = viewModelScope.launch {
        val listOfCurrency =
            repository.loadCurrency()
                .valutes.filterKeys { currencyListName.contains(it) }
                .values.map { it.toPresentation() }

        Log.e("LOG", listOfCurrency.toString())
        _listCurrency.value = listOfCurrency

        updateCurrencyFromList()
        if (_networkStateFlow.value != NetworkState.NetworkSuccess())
            _networkStateFlow.value = NetworkState.NetworkSuccess()
    }

    private fun updateBalance() {
        prefStorage.updateUserBalance(userBalance)
        _upperCurrency.value = _upperCurrency.value?.copy()
        _lowerCurrency.value = _lowerCurrency.value?.copy()
    }

    fun checkRequirementsForTransaction() {
        val upperCurrency = upperCurrency.value ?: emptyCurrency
        val lowerCurrency = lowerCurrency.value ?: emptyCurrency
        if (upperCurrency.charCode == lowerCurrency.charCode) {
            // same currency error
            _textNotification.value = appContext.getString(R.string.error_exchange_same_currency)
            return
        }

        if (upperFieldInput.isEmpty()) {
            // empty field error
            _textNotification.value =
                appContext.getString(R.string.error_exchange_empty_input_field)
            return
        }

        if (userBalance.getBalanceFromCharCode(upperCurrency.charCode) < upperFieldInput.toDouble()) {
            // too big value for this currency
            _textNotification.value =
                appContext.getString(R.string.error_exchange_insufficient_funds)
            return
        } else {
            //ok, change values and updateBalance
            val newUpperBalance = userBalance.getBalanceFromCharCode(upperCurrency.charCode) -
                    upperFieldInput.toDouble()
            val newLowerBalance = userBalance.getBalanceFromCharCode(lowerCurrency.charCode) +
                    lowerFieldInput.toDouble()
            getBalanceCopyWithNewValue(upperCurrency.charCode, newUpperBalance)
            getBalanceCopyWithNewValue(lowerCurrency.charCode, newLowerBalance)

            updateBalance()

            //Message about transaction
            val statusMessage = StringBuilder()
                .append(
                    appContext.getString(
                        R.string.success_exchange,
                        Utils.getCurrencySymbol(upperCurrency.charCode),
                        upperFieldInput,
                        upperCurrency.charCode,
                        newUpperBalance
                    )
                )
                .appendLine("")
                .appendLine("Available accounts:")
                .appendLine("USD: ${Utils.getCurrencySymbol("USD")}${userBalance.usdCurrency}")
                .appendLine("JPY: ${Utils.getCurrencySymbol("JPY")}${userBalance.jpyCurrency}")
                .appendLine("TRY: ${Utils.getCurrencySymbol("TRY")}${userBalance.tryCurrency}")
                .appendLine("RSD: ${Utils.getCurrencySymbol("RSD")}${userBalance.rsdCurrency}")
                .appendLine("CAD: ${Utils.getCurrencySymbol("CAD")}${userBalance.cadCurrency}")
                .toString()

            _textNotification.value = statusMessage
            // clear inputFields
            upperFieldInput = ""
            lowerFieldInput = ""
            _upperFieldInput.value = ""
            _lowerFieldInput.value = ""
        }
    }

    private fun getBalanceCopyWithNewValue(
        charCode: String,
        newCurrencyValue: Double
    ) {
        userBalance = when (charCode) {
            "USD" -> userBalance.copy(usdCurrency = newCurrencyValue)

            "JPY" -> userBalance.copy(jpyCurrency = newCurrencyValue)

            "TRY" -> userBalance.copy(tryCurrency = newCurrencyValue)

            "RSD" -> userBalance.copy(rsdCurrency = newCurrencyValue)

            "CAD" -> userBalance.copy(cadCurrency = newCurrencyValue)

            else -> userBalance
        }
    }

    fun updatePosition(newPosition: Int, isUpperWindow: Boolean) {
        if (isUpperWindow) {
            upperPosition = newPosition
            updateRatio(
                listCurrency.value?.get(upperPosition),
                listCurrency.value?.get(lowerPosition)
            )
            _upperCurrency.value = listCurrency.value?.get(upperPosition)
        } else {
            lowerPosition = newPosition
            updateRatio(
                listCurrency.value?.get(upperPosition),
                listCurrency.value?.get(lowerPosition)
            )
            _lowerCurrency.value = listCurrency.value?.get(lowerPosition)
        }

        updateInputField(!isUpperWindow)
    }

    private fun updateCurrencyFromList() {
        _upperCurrency.value = listCurrency.value?.get(upperPosition)
        _lowerCurrency.value = listCurrency.value?.get(lowerPosition)
    }

    companion object {
        val currencyListName = listOf(
            "USD", "JPY", "TRY", "RSD", "CAD"
        )

        val emptyCurrency = CurrencyPresentation("", "", "")
    }
}