package com.example.mynews.model.social

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mynews.domain.model.social.FriendsModel
import com.example.mynews.domain.repositories.goals.GoalsRepository
import com.example.mynews.domain.repositories.social.FriendsRepository
import com.example.mynews.domain.result.AddFriendResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class FriendsModelImpl @Inject constructor(
    private val friendsRepository: FriendsRepository,
    private val goalsRepository: GoalsRepository
) : FriendsModel {

    private val _friends = MutableLiveData<List<String>>()
    override val friends: LiveData<List<String>> = _friends

    private val _recentlyAddedFriend = MutableStateFlow<String?>(null)
    override val recentlyAddedFriend: StateFlow<String?> = _recentlyAddedFriend

    private val _searchQuery = MutableStateFlow("")
    override val searchQuery: StateFlow<String> = _searchQuery

    override fun updateSearchQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }

    override suspend fun fetchFriends(currentUserId: String) {
        friendsRepository.getFriendUsernames(currentUserId) { friendList ->
            _friends.postValue(friendList)
        }
    }

    override suspend fun addFriend(currentUserId: String, friendUsername: String): AddFriendResult {

        // normalized friendUsername in friendsRepository.addFriend
        val addFriendResult = friendsRepository.addFriend(currentUserId, friendUsername)

        // normalize here for local storing in _recentlyAddedFriend
        val normalizedFriendUsername = friendUsername.trim().lowercase()

        when (addFriendResult) {

            is AddFriendResult.Success -> {
                _searchQuery.value = "" // clear search bar
                _recentlyAddedFriend.value = normalizedFriendUsername
                fetchFriends(currentUserId) // refresh friends list

                goalsRepository.logAddOrRemoveFriend(currentUserId)
                // delay clearing the recently added username to trigger animation
                CoroutineScope(Dispatchers.Default).launch {
                    delay(1500)
                    _recentlyAddedFriend.value = null
                }
            }

            AddFriendResult.AlreadyAddedFriend -> {} // managed in viewmodel
            is AddFriendResult.Error -> {} // managed in viewmodel
            AddFriendResult.SelfAddAttempt -> {} // managed in viewmodel
            AddFriendResult.UserNotFound -> {} // managed in viewmodel
        }

        return addFriendResult

    }

    override suspend fun removeFriend(currentUserId: String, friendUsername: String) {

        // normalizes friendUsername in friendsRepository.removeFriend
        val wasRemoved = friendsRepository.removeFriend(currentUserId, friendUsername)

        if (wasRemoved) {
            fetchFriends(currentUserId)
            goalsRepository.logAddOrRemoveFriend(currentUserId)
        }

    }

}