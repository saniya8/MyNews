package com.example.mynews.domain.model.social

import androidx.lifecycle.LiveData
import com.example.mynews.domain.result.AddFriendResult
import kotlinx.coroutines.flow.StateFlow

interface FriendsModel {
    val friends: LiveData<List<String>>
    val searchQuery: StateFlow<String>
    val recentlyAddedFriend: StateFlow<String?>
    fun updateSearchQuery(newQuery: String)
    suspend fun fetchFriends(currentUserId: String)
    suspend fun addFriend(currentUserId: String, friendUsername: String): AddFriendResult
    suspend fun removeFriend(currentUserId: String, friendUsername: String)
}