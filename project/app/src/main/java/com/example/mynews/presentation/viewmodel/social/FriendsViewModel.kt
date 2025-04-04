package com.example.mynews.presentation.viewmodel.social

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.domain.logger.Logger
import com.example.mynews.domain.repositories.FriendsRepository
import com.example.mynews.domain.repositories.GoalsRepository
import com.example.mynews.domain.repositories.UserRepository
import com.example.mynews.presentation.state.AddFriendState
import com.example.mynews.presentation.state.RegisterState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val userRepository: UserRepository, // won't need this anymore
    private val friendsRepository: FriendsRepository,
    private val goalsRepository: GoalsRepository,
    private val logger: Logger,
) : ViewModel() {


    var showErrorDialog by mutableStateOf(false)

    // stores the error message to display in the dialog
    var errorDialogMessage by mutableStateOf<String?>(null)
        private set


    // SK: later problem - add in the FriendsState to store error dialogue message
    // SK: will use _isFriendNotFound to produce the correct error message in error dialog box
    // later, similar to how done in RegisterViewModel, which makes use of RegisterState
    private val _isFriendNotFound = mutableStateOf(false) // pass to addFriend in FriendsRepository

    // SK: post to _friends when friend is added and when friend is removed
    private val _friends = MutableLiveData<List<String>>()
    val friends: LiveData<List<String>> = _friends

    //private val _friendsIds = MutableLiveData<List<String>>()
    //val friendsIds: LiveData<List<String>> = _friendsIds

    // SK: not sure if we still need _username and username
    //private val _username = MutableStateFlow<String?>("")
    //val username: StateFlow<String?> = _username

    //val searchQuery = mutableStateOf("")

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _recentlyAddedFriend = MutableStateFlow<String?>(null)
    val recentlyAddedFriend: StateFlow<String?> = _recentlyAddedFriend




    private val isFriendNotFound = mutableStateOf(false)

    private var _selfAddAttemptErrorMessage = "You cannot add yourself \n as a friend."
    private var _alreadyAddedFriendErrorMessage = "is already your friend."
    private var _userNotFoundErrorMessage = "This user does not exist. \n Please check the username."
    private var _defaultErrorMessage = "Something went wrong. \n Please try again."


    fun updateSearchQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }

    // SK: rewrite this function to be identical to savedArticlesViewModel's getSavedArticles
    // EXCEPT here, call,
    // friendsRepository.getFriendIds(userID) { friendsList ->
    //                logger.d("Get friend", "Successfully fetched friends")
    //                _friends.postValue(friendsList) // ViewModel updates UI state
    //            }
    // This returns the user's friend's usernames
    fun fetchFriends() {
        viewModelScope.launch {
            try {
                val userID = userRepository.getCurrentUserId()
                if (userID.isNullOrEmpty()) {
                    logger.e("FriendsViewModel", "Error: User ID is null, cannot fetch friends")
                    return@launch
                }
                friendsRepository.getFriendUsernames(userID) { friendsList ->
                    _friends.value = friendsList
                }
            } catch (e: Exception) {
                logger.e("FriendsViewModel", "Error fetching friends", e)
            }
        }
    }

    // SK: below currentUserId, check if it's null and if so, log error and return (like in fetchFriends above)
    // add in...
    // val success= friendsRespository.addFriend(currentUserId, friendUsername, _isFriendNotFound)
    // if (success) then do _friends.postValue(updatedFriends)
    // else (i.e., !success) then log error
    // this is very similar to savedArticlesViewModel's deleteSavedArticle function flow
    fun addFriend(friendUsername: String) {
        if (friendUsername.isEmpty()) return
        viewModelScope.launch {
            try {



                val userID = userRepository.getCurrentUserId()
                if (userID.isNullOrEmpty()) {
                    logger.e("FriendsViewModel", "Error: User ID is null, cannot fetch friends")
                    return@launch
                }
                // normalized friendUsername in friendsRepository.addFriend
                val isAdded = friendsRepository.addFriend(userID, friendUsername, isFriendNotFound)

                /*if (isAdded) {
                    // Clear the search query and refresh the friends list
                    searchQuery.value = ""
                    fetchFriends()
                }*/

                val normalizedFriendUsername = friendUsername.trim().lowercase() // normalize here for local storing in _recentlyAddedFriend

                when (isAdded) {

                    is AddFriendState.Success -> {
                        _searchQuery.value = "" // clear search bar
                        _recentlyAddedFriend.value = normalizedFriendUsername
                        fetchFriends()         // refresh friends list

                        // Update the "Add 5 Friends" mission
                        updateAddFriendsMission(userID)
                        viewModelScope.launch {
                            delay(1500) // 2 second highlight
                            _recentlyAddedFriend.value = null // reset after animation
                        }
                    }

                    is AddFriendState.SelfAddAttempt -> {
                        errorDialogMessage = _selfAddAttemptErrorMessage
                        showErrorDialog = true
                    }

                    is AddFriendState.AlreadyAddedFriend -> {
                        errorDialogMessage = "$friendUsername $_alreadyAddedFriendErrorMessage"
                        showErrorDialog = true
                    }

                    is AddFriendState.UserNotFound -> {
                        errorDialogMessage = _userNotFoundErrorMessage
                        showErrorDialog = true
                    }

                    is AddFriendState.Error -> {
                        errorDialogMessage = _defaultErrorMessage
                        showErrorDialog = true
                    }
                }

            } catch (e: Exception) {
                logger.e("FriendsViewModel", "Error adding friend", e)
            }
        }
    }

    // SK: below currentUserId, check if it's null and if so, log error and return (like in fetchFriends above)
    // add in...
    // val success= friendsRespository.removeFriend(currentUserId, friendUsername)
    // if (success) then do _friends.postValue(updatedFriends)
    // else (i.e., !success) then log error
    // this is very similar to savedArticlesViewModel's deleteSavedArticle function flow
    fun removeFriend(friendUsername: String) {
        viewModelScope.launch {
            try {
                val userID = userRepository.getCurrentUserId()
                if (userID.isNullOrEmpty()) {
                    logger.e("FriendsViewModel", "Error: User ID is null, cannot remove friend")
                    return@launch
                }
                // Call the repository to remove the friend
                val isRemoved = friendsRepository.removeFriend(userID, friendUsername) // normalizes friendUsername in friendsRepository.removeFriend
                if (isRemoved) {
                    fetchFriends()
                    updateAddFriendsMission(userID)
                }
            } catch (e: Exception) {
                logger.e("FriendsViewModel", "Error removing friend", e)
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun updateAddFriendsMission(userId: String) {
        val friendCount = friendsRepository.getFriendCount(userId)
        val missions = goalsRepository.getMissions(userId)
        missions.filter { it.type == "add_friend" && !it.isCompleted }.forEach { mission ->
            val newCount = friendCount.coerceAtMost(mission.targetCount)
            goalsRepository.updateMissionProgress(userId, mission.id, newCount)
            if (newCount >= mission.targetCount) {
                goalsRepository.markMissionComplete(userId, mission.id)
            }
        }
    }




}