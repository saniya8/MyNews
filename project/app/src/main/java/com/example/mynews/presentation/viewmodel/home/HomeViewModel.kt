package com.example.mynews.presentation.viewmodel.home
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.data.api.Article
import com.example.mynews.domain.repositories.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.mynews.domain.repositories.NewsRepository
import com.example.mynews.domain.repositories.UserRepository


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val homeRepository: HomeRepository,
) : ViewModel() {

    private val _articleReactions = MutableLiveData<Map<String, String?>>()
    val articleReactions: LiveData<Map<String, String?>> = _articleReactions

    init {
        viewModelScope.launch {

            // get current user
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

            // get current user
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

            // get current user
            val userID = userRepository.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                Log.e("NewsViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            homeRepository.setReaction(userID, article, reaction)

        }

    }

}