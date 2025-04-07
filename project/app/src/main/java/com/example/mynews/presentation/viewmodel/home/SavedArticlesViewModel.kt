package com.example.mynews.presentation.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.domain.entities.Article
import com.example.mynews.domain.model.home.SavedArticlesModel
import com.example.mynews.domain.model.user.UserModel
import com.example.mynews.utils.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedArticlesViewModel @Inject constructor(
    private val userModel: UserModel,
    private val savedArticlesModel: SavedArticlesModel,
    private val logger: Logger,
): ViewModel() {

    val savedArticles: LiveData<List<Article>> = savedArticlesModel.savedArticles

    fun saveArticle(article: Article) {

        viewModelScope.launch {

            // get current user
            val userID = userModel.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                logger.e("SavedArticlesViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            savedArticlesModel.saveArticle(userID, article)
        }
    }

    fun deleteSavedArticle(article: Article) {

        viewModelScope.launch {

            // get current user
            val userID = userModel.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                logger.e("SavedArticlesViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            savedArticlesModel.deleteSavedArticle(userID, article)

        }
    }


    fun fetchSavedArticles() {

        viewModelScope.launch{

            // get current user
            val userID = userModel.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                logger.e("SavedArticlesViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            // at this point, successfully retrieved current user

            savedArticlesModel.getSavedArticles(userID)

        }

    }

}