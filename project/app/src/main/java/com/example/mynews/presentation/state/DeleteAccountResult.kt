package com.example.mynews.presentation.state

sealed class DeleteAccountResult {
    object Success : DeleteAccountResult()
    object IncorrectPassword : DeleteAccountResult()
    object Error : DeleteAccountResult()
}