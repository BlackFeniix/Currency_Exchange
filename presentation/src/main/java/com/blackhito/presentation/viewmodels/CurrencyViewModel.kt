package com.blackhito.presentation.viewmodels

import android.content.Context
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
import java.math.BigDecimal
import java.math.RoundingMode
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

    var upperToLowerRatio = BigDecimal.ZERO
    var lowerToUpperRatio = BigDecimal.ZERO

    private val _upperFieldInput = MutableLiveData("")
    val userUpperInput: LiveData<String> = _upperFieldInput
    private val _lowerFieldInput = MutableLiveData("")
    val userLowerInput: LiveData<String> = _lowerFieldInput

    var userBalance = prefStorage.getUserBalance()

    fun updateAllDataRegularly() = viewModelScope.launch {
        loadCurrencyFromNetwork()
        while (networkStateFlow.value != NetworkState.NetworkError()) {
            delay(30000)

            val listOfCurrency =
                repository.loadCurrency()
                    .valutes.filterKeys { currencyListName.contains(it) }
                    .values.map { it.toPresentation() }
            _listCurrency.value = listOfCurrency

            updateCurrencyFromList()
            updateRatio(listCurrency.value?.get(upperPosition), listCurrency.value?.get(lowerPosition))
        }
    }

    fun setUpperInputField(newValue: String) {
        if (newValue == userUpperInput.value)
            return
        if (newValue == "0.0") {
            _upperFieldInput.value = ""
            _lowerFieldInput.value = ""
        }
        _upperFieldInput.value = newValue
        _lowerFieldInput.value = if (_upperFieldInput.value?.isNotEmpty() == true)
            upperToLowerRatio.times(newValue.toBigDecimal()).toString()
        else
            ""
    }

    fun setLowerInputField(newValue: String) {
        if (newValue == userLowerInput.value)
            return
        if (newValue == "0.0") {
            _lowerFieldInput.value = ""
            _upperFieldInput.value = ""
        }
        _lowerFieldInput.value = newValue
        _upperFieldInput.value = if (_lowerFieldInput.value?.isNotEmpty() == true)
            lowerToUpperRatio.times(newValue.toBigDecimal()).toString()
        else
            ""
    }

    private fun updateRatio(
        upperNewValue: CurrencyPresentation?,
        lowerNewValue: CurrencyPresentation?
    ) {
        val upperBaseValue = upperNewValue?.baseValue?.toBigDecimal() ?: BigDecimal.ZERO
        val lowerBaseValue = lowerNewValue?.baseValue?.toBigDecimal() ?: BigDecimal.ZERO
        upperToLowerRatio =  (upperBaseValue / lowerBaseValue).setScale(2, RoundingMode.HALF_EVEN)
        lowerToUpperRatio = (lowerBaseValue / upperBaseValue).setScale(2, RoundingMode.HALF_EVEN)
    }

    private fun loadCurrencyFromNetwork() = viewModelScope.launch {
        try {
            val listOfCurrency =
                repository.loadCurrency()
                    .valutes.filterKeys { currencyListName.contains(it) }
                    .values.map { it.toPresentation() }

            _listCurrency.value = listOfCurrency

            updateCurrencyFromList()
            if (_networkStateFlow.value != NetworkState.NetworkSuccess())
                _networkStateFlow.value = NetworkState.NetworkSuccess()
        } catch (e:Exception) {
            _networkStateFlow.value = NetworkState.NetworkError()
        }

    }

    private fun updateBalance() {
        prefStorage.updateUserBalance(userBalance)
        _upperCurrency.value = _upperCurrency.value?.copy()
        _lowerCurrency.value = _lowerCurrency.value?.copy()
    }

    fun checkRequirementsForTransaction() {
        val upperFieldInput = _upperFieldInput.value ?: ""
        val lowerFieldInput = _lowerFieldInput.value ?: ""
        val upperCurrency = upperCurrency.value ?: emptyCurrency
        val lowerCurrency = lowerCurrency.value ?: emptyCurrency
        if (upperCurrency.charCode == lowerCurrency.charCode) {
            _textNotification.value = appContext.getString(R.string.error_exchange_same_currency)
            return
        }

        if (upperFieldInput.isEmpty()) {
            _textNotification.value =
                appContext.getString(R.string.error_exchange_empty_input_field)
            return
        }

        if (userBalance.getBalanceFromCharCode(upperCurrency.charCode) < upperFieldInput.toDouble()) {
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

            _upperFieldInput.value = if (_upperFieldInput.value?.isEmpty() == true)
                ""
            else
                (lowerToUpperRatio).times(userLowerInput.value?.toBigDecimal()?:BigDecimal.ZERO).toString()
        } else {
            lowerPosition = newPosition
            updateRatio(
                listCurrency.value?.get(upperPosition),
                listCurrency.value?.get(lowerPosition)
            )
            _lowerCurrency.value = listCurrency.value?.get(lowerPosition)

            _lowerFieldInput.value = if (_lowerFieldInput.value?.isEmpty() == true)
                ""
            else
                (upperToLowerRatio).times(_upperFieldInput.value?.toBigDecimal()?: BigDecimal.ZERO).toString()
        }
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