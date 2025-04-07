package com.example.mynews.presentation.viewmodel.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.utils.logger.Logger
import com.example.mynews.domain.entities.Reaction
import com.example.mynews.domain.model.social.SocialModel
import com.example.mynews.domain.model.user.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SocialViewModel @Inject constructor(
    private val userModel: UserModel,
    private val socialModel: SocialModel,
    private val logger: Logger,
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage.asStateFlow()

    val friendsMap: StateFlow<Map<String, String>> = socialModel.friendsMap
    val reactions: StateFlow<List<Reaction>> = socialModel.reactions
    val searchQuery: StateFlow<String> = socialModel.searchQuery
    val filteredReactions: StateFlow<List<Reaction>> = socialModel.filteredReactions

    fun updateSearchQuery(newQuery: String) {
        socialModel.updateSearchQuery(newQuery)
    }

    fun fetchFriends() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userID = userModel.getCurrentUserId()
                if (userID.isNullOrEmpty()) {
                    logger.e("SocialViewModel", "Error: User ID is null, cannot fetch friends")
                    _isLoading.value = false
                    return@launch
                }

                socialModel.fetchFriends(userID)
                if (friendsMap.value.isEmpty()) {
                    _isLoading.value = false
                }

                // else:
                // don't reset _isLoading here: let fetchFriendsReaction handle it
                // since this means friendMap is not empty, so LaunchedEffect on friendMap
                // in view will call fetchFriendsReactions

            } catch (e: Exception) {
                logger.e("SocialViewModel", "Error fetching friends: ${e.message}", e)
                _errorMessage.value = "Failed to fetch friends: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun fetchFriendsReactions(friendIds: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                socialModel.fetchFriendsReactions(friendIds)
                _isLoading.value = false

            } catch (e: Exception) {
                logger.e("SocialViewModel", "Error fetching reactions: ${e.message}", e)
                _errorMessage.value = "Failed to fetch friends' reactions: ${e.message}"
                _isLoading.value = false
            }
        }
    }
}