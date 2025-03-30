package com.example.mynews.presentation.state

sealed class AddFriendState {
    object Success : AddFriendState()
    object SelfAddAttempt : AddFriendState()
    object AlreadyAddedFriend: AddFriendState()
    object UserNotFound : AddFriendState()
    data class Error(val message: String) : AddFriendState()
}