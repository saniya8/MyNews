package com.example.mynews.presentation.viewmodel.social

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.utils.logger.Logger
import com.example.mynews.domain.entities.Reaction
import com.example.mynews.domain.repositories.SocialRepository
import com.example.mynews.domain.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class SocialViewModel @Inject constructor(
    private val userRepository: UserRepository, // won't need this anymore
    private val socialRepository: SocialRepository,
    private val logger: Logger,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage.asStateFlow()

    // SK: post to _friends when friend is added and when friend is removed
    private val _friends = MutableLiveData<List<String>>()
    val friends: LiveData<List<String>> = _friends

    private val _friendsIds = MutableLiveData<List<String>>()
    val friendsIds: LiveData<List<String>> = _friendsIds

    private val _reactions = MutableStateFlow<List<Reaction>>(emptyList())
    val reactions: StateFlow<List<Reaction>> get() = _reactions.asStateFlow()

    private val _friendsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val friendsMap: StateFlow<Map<String, String>> = _friendsMap

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val filteredReactions: StateFlow<List<Reaction>> = combine(
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
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )


    fun updateSearchQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }


    fun fetchFriends() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userID = userRepository.getCurrentUserId()
                if (userID.isNullOrEmpty()) {
                    logger.e("SocialViewModel", "Error: User ID is null, cannot fetch friends")
                    _isLoading.value = false
                    return@launch
                }
                socialRepository.getFriends(userID) { friendMap ->
                    _friendsMap.value = friendMap // update friendsMap (friendID -> username)
                    //_friendsIds.value = friendMap.keys.toList() // update friend IDs list
                    //_friends.value = friendMap.values.toList() // update friend usernames list
                    if (friendMap.isEmpty()) { // avoids flickers
                        _reactions.value = emptyList()
                        _isLoading.value = false
                    }
                    // don't reset _isLoading here: let fetchFriendsReaction handle it
                    // since this means friendMap is not empty, so LaunchedEffect on friendMap
                    // in view will call fetchFriendsReactions

                }
            } catch (e: Exception) {
                logger.e("SocialViewModel", "Error fetching friends: ${e.message}", e)
                _isLoading.value = false
            }
        }
    }

    fun fetchFriendsReactions(friendIds: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                socialRepository.getFriendsReactions(friendIds) { reactions ->
                    _reactions.value = reactions // updates UI in real-time
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch friends' reactions: ${e.message}"
                _isLoading.value = false
            }
        }
    }

}