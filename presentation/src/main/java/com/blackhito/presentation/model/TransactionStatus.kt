package com.blackhito.presentation.model

sealed class TransactionStatus {
    open class Success(data:List<String>): TransactionStatus()
    object ErrorSameCurrency: TransactionStatus()
    object ErrorEmptyFields : TransactionStatus()
    object ErrorNotEnoughCurrency : TransactionStatus()
}