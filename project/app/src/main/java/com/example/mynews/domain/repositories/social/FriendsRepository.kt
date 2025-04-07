package com.example.mynews.domain.repositories.social

import com.example.mynews.domain.result.AddFriendResult

interface FriendsRepository {
    suspend fun addFriend(currentUserID: String, friendUsername: String): AddFriendResult
    suspend fun removeFriend(currentUserID: String, friendUsername: String): Boolean
    suspend fun getFriendUsernames(currentUserID: String, onResult: (List<String>) -> Unit)
    suspend fun getFriendCount(currentUserID: String): Int
}