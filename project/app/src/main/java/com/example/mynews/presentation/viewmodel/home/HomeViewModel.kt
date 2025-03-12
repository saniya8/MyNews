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