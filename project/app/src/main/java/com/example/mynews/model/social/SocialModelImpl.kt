package com.example.mynews.model.social

import com.example.mynews.domain.entities.Reaction
import com.example.mynews.domain.model.social.SocialModel
import com.example.mynews.domain.repositories.social.SocialRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class SocialModelImpl @Inject constructor(
    private val socialRepository: SocialRepository
) : SocialModel {

    private val _friendsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    override val friendsMap: StateFlow<Map<String, String>> = _friendsMap

    private val _reactions = MutableStateFlow<List<Reaction>>(emptyList())
    override val reactions: StateFlow<List<Reaction>> = _reactions

    private val _searchQuery = MutableStateFlow("")
    override val searchQuery: StateFlow<String> = _searchQuery

    override val filteredReactions: StateFlow<List<Reaction>> = combine(
        _reactions, _searchQuery, _friendsMap
    ) { allReactions, query, map ->
        val normalizedQuery = query.trim().lowercase()

        if (normalizedQuery.isEmpty()) {
            allReactions
        } else {
            allReactions.filter { reaction ->
                val username = map[reaction.userID].orEmpty().lowercase()
                username.contains(normalizedQuery)
            }
        }
    }.stateIn(
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    override fun updateSearchQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }

    override suspend fun fetchFriends(userId: String) {
        socialRepository.getFriends(userId) { friendMap ->
            _friendsMap.value = friendMap

            if (friendMap.isEmpty()) {
                _reactions.value = emptyList()
            }

            // if not empty, UI can then trigger fetchFriendsReactions
        }
    }

    override fun fetchFriendsReactions(friendIds: List<String>) {
        socialRepository.getFriendsReactions(friendIds) { updatedReactions ->
            _reactions.value = updatedReactions
        }
    }

}