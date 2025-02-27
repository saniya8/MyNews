package com.example.mynews.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.domain.repositories.AuthRepository
import com.example.mynews.domain.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val userRepository: UserRepository,  // Repository to handle user data
    private val authRepository: AuthRepository   // Repository to handle authentication
) : ViewModel() {

    private val _friends = MutableLiveData<List<String>>()
    val friends: LiveData<List<String>> = _friends

    private val _username = MutableStateFlow<String?>("")
    val username: StateFlow<String?> = _username

    fun fetchFriends() {
        viewModelScope.launch {
            try {
                val currentUserId = userRepository.getCurrentUserId()
                if (currentUserId == null) {
                    Log.e("FriendsViewModel", "Error: User ID is null, cannot fetch friends")
                    return@launch
                }

                val friendsList = userRepository.getUserFriends(currentUserId)
                _friends.postValue(friendsList)
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "Error fetching friends", e)
            }
        }
    }

    fun addFriend(friendUsername: String) {
        viewModelScope.launch {
            try {
                // Create a new list by adding the friend to the existing list
                val currentUserId = userRepository.getCurrentUserId()
                val updatedFriends = _friends.value.orEmpty() + friendUsername
                if (currentUserId != null) {
                    userRepository.updateUserFriends(currentUserId, updatedFriends)
                }
                _friends.postValue(updatedFriends)
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "Error adding friend", e)
            }
        }
    }

    fun removeFriend(friendUsername: String) {
        viewModelScope.launch {
            try {
                val currentUserId = userRepository.getCurrentUserId()
                // Create a new list by filtering out the friend
                val updatedFriends = _friends.value.orEmpty().filter { it != friendUsername }
                if (currentUserId != null) {
                    userRepository.updateUserFriends(currentUserId, updatedFriends)
                }
                _friends.postValue(updatedFriends)
            } catch (e: Exception) {
                Log.e("FriendsViewModel", "Error removing friend", e)
            }
        }
    }
}