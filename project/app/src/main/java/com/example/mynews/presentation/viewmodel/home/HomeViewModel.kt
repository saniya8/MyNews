package com.example.mynews.presentation.viewmodel.home
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.domain.entities.Article
import com.example.mynews.domain.model.home.HomeModel
import com.example.mynews.domain.model.user.UserModel
import com.example.mynews.utils.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userModel: UserModel,
    private val homeModel: HomeModel,
    private val logger: Logger,
) : ViewModel() {

    val articleReactions: LiveData<Map<String, String?>> = homeModel.articleReactions

    init {
        viewModelScope.launch {
            val userID = userModel.getCurrentUserId()
            if (userID.isNullOrEmpty()) {
                logger.e("HomeViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            homeModel.trackReactions(userID)
        }
    }

    fun fetchReaction(article: Article, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val userID = userModel.getCurrentUserId()
            if (userID.isNullOrEmpty()) {
                logger.e("HomeViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            val reaction = homeModel.getReaction(userID, article)
            onResult(reaction) // return the result directly to the UI
        }
    }

    fun updateReaction(article: Article, reaction: String?) {
        viewModelScope.launch {
            val userID = userModel.getCurrentUserId()
            if (userID.isNullOrEmpty()) {
                logger.e("HomeViewModel", "No user logged in. User ID is null or empty")
                return@launch
            }
            homeModel.setReaction(userID, article, reaction)
        }
    }

}