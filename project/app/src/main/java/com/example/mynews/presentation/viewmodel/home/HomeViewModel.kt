package com.example.mynews.presentation.viewmodel.home
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.data.api.news.Article
import com.example.mynews.data.api.news.Reaction
import com.example.mynews.domain.repositories.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.mynews.domain.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val homeRepository: HomeRepository,
) : ViewModel() {

    private val _articleReactions = MutableLiveData<Map<String, String?>>()
    val articleReactions: LiveData<Map<String, String?>> = _articleReactions

    private val _reactions = MutableStateFlow<List<Reaction>>(emptyList())
    val reactions: StateFlow<List<Reaction>> get() = _reactions.asStateFlow()

    // TODO might do something with these TBD
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage.asStateFlow()

    // In init, call trackReactions, and post the input to onReactionChanged (aka userArticleReactions)
    // to the _articleReactions
    // trackReactions only needs to be called once in init. This call will attach the snapshot
    // listener defined in trackReactions to the users_reactions collection. This means that
    // trackReactions will listen for any changes in the users_reactions, and if there is any change
    // it will update _articleReactions, which will update articleReactions. The View that observes
    // articleReactions will therefore automatically update. So, for example, when the user
    // selects/deselects a reaction, it calls updateReaction here, which calls setReaction in the
    // NewsRepositoryImpl, which makes a change to users_reactions. Since trackReactions is listening
    // to this via the snapshot listener, it will automatically trigger _articleReactions to
    // update.
    init {
        viewModelScope.launch {
            val userID = userRepository.getCurrentUserId()
            if (userID.isNullOrEmpty()) {
                Log.e("NewsViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            homeRepository.trackReactions(userID) { userArticleReactions ->
                Log.d("ReactionDebug", "Received reactions update: $userArticleReactions")
                _articleReactions.postValue(userArticleReactions)
            }
        }
    }

    // for retrieving user's reaction for a specific article
    fun fetchReaction(article: Article, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val userID = userRepository.getCurrentUserId()
            if (userID.isNullOrEmpty()) {
                Log.e("NewsViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            val reaction = homeRepository.getReaction(userID, article)
            onResult(reaction) // return the result directly to the UI
        }
    }

    fun updateReaction(article: Article, reaction: String?) {
        viewModelScope.launch {
            val userID = userRepository.getCurrentUserId()
            if (userID.isNullOrEmpty()) {
                Log.e("NewsViewModel", "No user logged in. User ID is null or empty")
                return@launch
            }
            homeRepository.setReaction(userID, article, reaction)
        }
    }

    fun fetchFriendsReactions(friendIds: List<String>) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val friendsReactions = homeRepository.getFriendsReactions(friendIds)
                _reactions.value = friendsReactions
            } catch (e: Exception) {
                _errorMessage.value = "Failed to fetch friends' reactions: ${e.message}"
            }
        }
    }

}