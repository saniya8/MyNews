package com.example.mynews.domain.result

sealed class AddFriendResult {
    object Success : AddFriendResult()
    object SelfAddAttempt : AddFriendResult()
    object AlreadyAddedFriend: AddFriendResult()
    object UserNotFound : AddFriendResult()
    data class Error(val message: String) : AddFriendResult()
}