package com.example.mynews.presentation.viewmodel.social

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.domain.repositories.FriendsRepository
import com.example.mynews.domain.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val userRepository: UserRepository, // won't need this anymore
    private val friendsRepository: FriendsRepository
   // private val authRepository: AuthRepository
) : ViewModel() {

    // SK: later problem - add in the FriendsState to store error dialogue message

    // SK: will use _isFriendNotFound to produce the correct error message in error dialog box
    // later, similar to how done in RegisterViewModel, which makes use of RegisterState
    private val _isFriendNotFound = mutableStateOf(false) // pass to addFriend in FriendsRepository

    // SK: post to _friends when friend is added and when friend is removed
    private val _friends = MutableLiveData<List<String>>()
    val friends: LiveData<List<String>> = _friends

    // SK: not sure if we still need _username and username
    private val _username = MutableStateFlow<String?>("")
    val username: StateFlow<String?> = _username

    // SK: won't need _users and users if we do simple find friends screen
    private val _users = MutableLiveData<List<String>>()
    val users: LiveData<List<String>> = _users

    val searchQuery = mutableStateOf("")
    val isFriendNotFound = mutableStateOf(false)


    // SK: rewrite this function to be identical to savedArticlesViewModel's getSavedArticles
    // EXCEPT here, call,
    // friendsRepository.getFriends(userID) { friendsList ->
    //                Log.d("Get friend", "Successfully fetched friends")
    //                _friends.postValue(friendsList) // ViewModel updates UI state
    //            }
    // reason this needs to be re-written is due to the implementation of the function in the
    // friends repository
    fun fetchFriends() {
        viewModelScope.launch {
            try {
                val currentUserId = userRepository.getCurrentUserId()
                if (currentUserId == null) {
                    Log.e("FriendsViewModel", "Error: User ID is null, cannot fetch friends")
                    return@launch
                }

                friendsRepository.getFriendUsernames(currentUserId) { friendsList ->
                    _friends.value = friendsList
                }
                // val friendsList = friendsRepository.getFriends(currentUserId, onResult = {} ) // TODO
                // _friends.postValue(friendsList)
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "Error fetching friends", e)
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
        // val friendUsername = searchQuery.value.trim()
        if (friendUsername.isEmpty()) return

        viewModelScope.launch {
            try {
                 //Create a new list by adding the friend to the existing list
                val currentUserID = userRepository.getCurrentUserId().toString()

                val isAdded = friendsRepository.addFriend(currentUserID, friendUsername, isFriendNotFound)
                if (isAdded) {
                    // Clear the search query and refresh the friends list
                    searchQuery.value = ""
                    fetchFriends()
                }
//                val currentUserId = userRepository.getCurrentUserId()
//                val updatedFriends = _friends.value.orEmpty() + friendUsername
//
//                if (currentUserId != null) {
//                    // friendsRepository.addFriend(currentUserId, friendUsername, true) // TODO
//                }
//                _friends.postValue(updatedFriends)
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "Error adding friend", e)
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
                val currentUserId = userRepository.getCurrentUserId()
                if (currentUserId == null) {
                    Log.e("FriendsViewModel", "Error: User ID is null, cannot remove friend")
                    return@launch
                }

                // Call the repository to remove the friend
                val isRemoved = friendsRepository.removeFriend(currentUserId, friendUsername)
                if (isRemoved) {
                    // Refresh the friends list after removal
                    fetchFriends()
                }
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "Error removing friend", e)
            }
        }
    }
}