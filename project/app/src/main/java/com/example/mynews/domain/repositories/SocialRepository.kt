package com.example.mynews.domain.repositories

import com.example.mynews.domain.entities.Reaction

interface SocialRepository {
    fun getFriendsReactions(friendIDs: List<String>,
                            onResult: (List<Reaction>) -> Unit,)
    suspend fun getFriends(currentUserID: String,
                           onResult: (Map<String, String>) -> Unit)
}