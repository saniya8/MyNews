package com.example.mynews.domain.repositories

import androidx.compose.runtime.MutableState
import com.example.mynews.presentation.state.AddFriendResult

interface FriendsRepository {
    suspend fun addFriend(currentUserID: String, friendUsername: String, isFriendNotFound: MutableState<Boolean>): AddFriendResult
    suspend fun removeFriend(currentUserID: String, friendUsername: String): Boolean
    //suspend fun getFriendIds(currentUserID: String, onResult: (List<String>) -> Unit)
    suspend fun getFriendUsernames(currentUserID: String, onResult: (List<String>) -> Unit)
    //suspend fun getFriendIdsAndUsernames(currentUserID: String, onResult: (Map<Any?, Any?>) -> Unit)

    suspend fun getFriendCount(currentUserID: String): Int
}