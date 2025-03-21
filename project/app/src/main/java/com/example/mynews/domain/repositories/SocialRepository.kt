package com.example.mynews.domain.repositories

import com.example.mynews.domain.model.Reaction

interface SocialRepository {
    suspend fun getFriendIds(currentUserID: String, onResult: (List<String>) -> Unit)
    suspend fun getFriendIdsAndUsernames(currentUserID: String, onResult: (Map<String, String>) -> Unit)
    fun getFriendsReactions(friendIDs: List<String>,
                            onResult: (List<Reaction>) -> Unit,)
    suspend fun getFriends(currentUserID: String,
                           onResult: (Map<String, String>) -> Unit)
}