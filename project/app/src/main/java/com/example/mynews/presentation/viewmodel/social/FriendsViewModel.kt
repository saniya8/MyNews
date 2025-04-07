package com.example.mynews.presentation.viewmodel.social

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.domain.model.social.FriendsModel
import com.example.mynews.domain.model.user.UserModel
import com.example.mynews.utils.logger.Logger
import com.example.mynews.domain.result.AddFriendResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val userModel: UserModel,
    private val friendsModel: FriendsModel,
    private val logger: Logger,
) : ViewModel() {


    var showErrorDialog by mutableStateOf(false)

    var errorDialogMessage by mutableStateOf<String?>(null)
        private set

    val friends: LiveData<List<String>> = friendsModel.friends
    val searchQuery: StateFlow<String> = friendsModel.searchQuery
    val recentlyAddedFriend: StateFlow<String?> = friendsModel.recentlyAddedFriend


    private var _selfAddAttemptErrorMessage = "You cannot add yourself \n as a friend."
    private var _alreadyAddedFriendErrorMessage = "is already your friend."
    private var _userNotFoundErrorMessage = "This user does not exist. \n Please check the username."
    private var _defaultErrorMessage = "Something went wrong. \n Please try again."


    fun updateSearchQuery(newQuery: String) {
        friendsModel.updateSearchQuery(newQuery)
    }

    fun fetchFriends() {
        viewModelScope.launch {
            try {
                val userID = userModel.getCurrentUserId()
                if (userID.isNullOrEmpty()) {
                    logger.e("FriendsViewModel", "Error: User ID is null, cannot fetch friends")
                    return@launch
                }
                friendsModel.fetchFriends(userID)
            } catch (e: Exception) {
                logger.e("FriendsViewModel", "Error fetching friends", e)
            }
        }
    }

    fun addFriend(friendUsername: String) {
        if (friendUsername.isEmpty()) return
        viewModelScope.launch {
            try {

                val userID = userModel.getCurrentUserId()
                if (userID.isNullOrEmpty()) {
                    logger.e("FriendsViewModel", "Error: User ID is null, cannot fetch friends")
                    return@launch
                }

                // normalized friendUsername in friendsRepository.addFriend
                val addFriendResult = friendsModel.addFriend(userID, friendUsername)

                when (addFriendResult) {

                    is AddFriendResult.Success -> Unit // already handled in model

                    is AddFriendResult.SelfAddAttempt -> {
                        errorDialogMessage = _selfAddAttemptErrorMessage
                        showErrorDialog = true
                    }

                    is AddFriendResult.AlreadyAddedFriend -> {
                        errorDialogMessage = "$friendUsername $_alreadyAddedFriendErrorMessage"
                        showErrorDialog = true
                    }

                    is AddFriendResult.UserNotFound -> {
                        errorDialogMessage = _userNotFoundErrorMessage
                        showErrorDialog = true
                    }

                    is AddFriendResult.Error -> {
                        errorDialogMessage = _defaultErrorMessage
                        showErrorDialog = true
                    }
                }

            } catch (e: Exception) {
                logger.e("FriendsViewModel", "Error adding friend", e)
            }
        }
    }

    fun removeFriend(friendUsername: String) {
        viewModelScope.launch {
            try {
                val userID = userModel.getCurrentUserId()
                if (userID.isNullOrEmpty()) {
                    logger.e("FriendsViewModel", "Error: User ID is null, cannot remove friend")
                    return@launch
                }
                friendsModel.removeFriend(userID, friendUsername) // normalizes friendUsername in friendsRepository.removeFriend
            } catch (e: Exception) {
                logger.e("FriendsViewModel", "Error removing friend", e)
            }
        }
    }

}