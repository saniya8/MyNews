package com.example.mynews.presentation.viewmodel.social

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.domain.model.Reaction
import com.example.mynews.domain.repositories.SocialRepository
import com.example.mynews.domain.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SocialViewModel @Inject constructor(
    private val userRepository: UserRepository, // won't need this anymore
    private val socialRepository: SocialRepository,
) : ViewModel() {

    // TODO might do something with these TBD
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

    // DONE - TODO update types
    //private val _friendsMap = MutableStateFlow<Map<Any?, Any?>>(emptyMap())
    //val friendsMap: StateFlow<Map<Any?, Any?>> = _friendsMap

    private val _friendsMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val friendsMap: StateFlow<Map<String, String>> = _friendsMap


    fun fetchFriends() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userID = userRepository.getCurrentUserId()
                if (userID.isNullOrEmpty()) {
                    Log.e("SocialViewModel", "Error: User ID is null, cannot fetch friends")
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
                Log.e("SocialViewModel", "Error fetching friends: ${e.message}", e)
                _isLoading.value = false
            }
        }
    }

    fun fetchFriendsReactions(friendIds: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            //if (friendIds.isEmpty()) { // immediately clear reactions if no friends exist
            //    _reactions.value = emptyList()
            //    _isLoading.value = false
            //    return@launch
            //}

            try {
                //val friendsReactions = socialRepository.getFriendsReactions(friendIds)
                //_reactions.value = friendsReactions
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

    // removing duplication of code and fixing bug where _friendids updates but _friends doesn't
    // also, no need for _friends and _friendids since friendsmap contains both of that
    // fetch friends updated version above is "merging" fetchfriendids and fetchfriendidsandusernames


    fun fetchFriendIds() {
        viewModelScope.launch {
            try {
                val userID = userRepository.getCurrentUserId()
                if (userID.isNullOrEmpty()) {
                    Log.e("SocialViewModel", "Error: User ID is null, cannot fetch friends")
                    return@launch
                }
                socialRepository.getFriendIds(userID) { friendsIDList ->
                    _friendsIds.postValue(friendsIDList)
                }
            } catch (e: Exception) {
                Log.e("SocialViewModel", "Error fetching friend IDs: ${e.message}", e)
            }
        }
    }

    fun fetchFriendIdsAndUsernames() {
        viewModelScope.launch {
            try {
                val userID = userRepository.getCurrentUserId()
                if (userID.isNullOrEmpty()) {
                    Log.e("SocialViewModel", "Error: User ID is null, cannot fetch friends")
                    return@launch
                }

                // Call the repository to get the map of friend IDs and usernames
                socialRepository.getFriendIdsAndUsernames(userID) { friendMap ->
                    _friendsMap.value = friendMap

                    // Update _friends and _friendsIds if needed
//                    _friendsIds.postValue(friendMap.keys.toList() as List<String>?)
//                    _friends.postValue(friendMap.values.toList() as List<String>?)
                    _friendsIds.postValue(friendMap.keys.map { it.toString() })
                    _friends.postValue(friendMap.values.map { it.toString() })
                }
            } catch (e: Exception) {
                Log.e("SocialViewModel", "Error fetching friend IDs and usernames", e)
            }
        }
    }

}