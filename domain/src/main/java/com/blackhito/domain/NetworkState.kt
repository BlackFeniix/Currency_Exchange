package com.blackhito.domain

sealed class NetworkState {
    open class NetworkSuccess : NetworkState()
    open class NetworkError : NetworkState()
    open class NetworkDownload : NetworkState()
}

