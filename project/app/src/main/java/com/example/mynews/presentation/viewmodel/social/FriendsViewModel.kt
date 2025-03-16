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
) : ViewModel() {

    // SK: later problem - add in the FriendsState to store error dialogue message
    // SK: will use _isFriendNotFound to produce the correct error message in error dialog box
    // later, similar to how done in RegisterViewModel, which makes use of RegisterState
    private val _isFriendNotFound = mutableStateOf(false) // pass to addFriend in FriendsRepository

    // SK: post to _friends when friend is added and when friend is removed
    private val _friends = MutableLiveData<List<String>>()
    val friends: LiveData<List<String>> = _friends

    private val _friendsIds = MutableLiveData<List<String>>()
    val friendsIds: LiveData<List<String>> = _friendsIds

    // SK: not sure if we still need _username and username
    private val _username = MutableStateFlow<String?>("")
    val username: StateFlow<String?> = _username

    val searchQuery = mutableStateOf("")
    val isFriendNotFound = mutableStateOf(false)

    // TODO update types
    private val _friendsMap = MutableStateFlow<Map<Any?, Any?>>(emptyMap())
    val friendsMap: StateFlow<Map<Any?, Any?>> = _friendsMap

    // SK: rewrite this function to be identical to savedArticlesViewModel's getSavedArticles
    // EXCEPT here, call,
    // friendsRepository.getFriends(userID) { friendsList ->
    //                Log.d("Get friend", "Successfully fetched friends")
    //                _friends.postValue(friendsList) // ViewModel updates UI state
    //            }
    // This returns the user's friend's usernames
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
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "Error fetching friends", e)
            }
        }
    }

    fun getFriendIds() {
        viewModelScope.launch {
            try {
                val currentUserId = userRepository.getCurrentUserId().toString()
                friendsRepository.getFriends(currentUserId) { friendsIDList ->
                    _friendsIds.postValue(friendsIDList)
                }
            } catch (e: Exception) {
                Log.e("FriendsRepository", "Error fetching friend IDs: ${e.message}", e)
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
                val currentUserID = userRepository.getCurrentUserId().toString()
                val isAdded = friendsRepository.addFriend(currentUserID, friendUsername, isFriendNotFound)
                if (isAdded) {
                    // Clear the search query and refresh the friends list
                    searchQuery.value = ""
                    fetchFriends()
                }
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
                    fetchFriends()
                }
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "Error removing friend", e)
            }
        }
    }

    fun fetchFriendIdsAndUsernames() {
        viewModelScope.launch {
            try {
                val currentUserId = userRepository.getCurrentUserId()
                if (currentUserId == null) {
                    Log.e("FriendsViewModel", "Error: User ID is null, cannot fetch friends")
                    return@launch
                }

                // Call the repository to get the map of friend IDs and usernames
                friendsRepository.getFriendIdsAndUsernames(currentUserId) { friendMap ->
                    _friendsMap.value = friendMap

                    // Update _friends and _friendsIds if needed
//                    _friendsIds.postValue(friendMap.keys.toList() as List<String>?)
//                    _friends.postValue(friendMap.values.toList() as List<String>?)
                    _friendsIds.postValue(friendMap.keys.map { it.toString() })
                    _friends.postValue(friendMap.values.map { it.toString() })
                }
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "Error fetching friend IDs and usernames", e)
            }
        }
    }

}