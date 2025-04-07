package com.example.mynews.domain.model.social

import com.example.mynews.domain.entities.Reaction
import kotlinx.coroutines.flow.StateFlow

interface SocialModel {
    val friendsMap: StateFlow<Map<String, String>>  // friendId -> username
    val reactions: StateFlow<List<Reaction>>
    val searchQuery: StateFlow<String>         // for filtering
    val filteredReactions: StateFlow<List<Reaction>>

    fun updateSearchQuery(newQuery: String)
    suspend fun fetchFriends(userId: String)
    fun fetchFriendsReactions(friendIds: List<String>)

}