package com.example.mynews.domain.repositories

import androidx.compose.runtime.MutableState

interface FriendsRepository {
    suspend fun addFriend(currentUserID: String, friendUsername: String, isFriendNotFound: MutableState<Boolean>): Boolean
    suspend fun removeFriend(currentUserID: String, friendUsername: String): Boolean
    suspend fun getFriends(currentUserID: String, onResult: (List<String>) -> Unit)
    suspend fun getFriendUsernames(currentUserID: String, onResult: (List<String>) -> Unit)
    suspend fun getFriendIdsAndUsernames(currentUserID: String, onResult: (Map<Any?, Any?>) -> Unit)
}