package com.example.mynews.presentation.viewmodel.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.service.news.Article
import com.example.mynews.utils.logger.Logger
import com.example.mynews.domain.repositories.SavedArticlesRepository
import com.example.mynews.domain.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedArticlesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val savedArticlesRepository: SavedArticlesRepository,
    private val logger: Logger,
): ViewModel() {


    private val _savedArticles = MutableLiveData<List<Article>>(emptyList())
    val savedArticles: LiveData<List<Article>> = _savedArticles

    // saving article in firestore collection
    fun saveArticle(article: Article) {

        viewModelScope.launch { // firestore operations are async so need this

            // get current user
            val userID = userRepository.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                logger.e("SavedArticlesViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            val success = savedArticlesRepository.saveArticle(userID, article)
            if (success) {
                logger.d("SavedArticlesViewModel", "Article successfully saved: ${article.title}")
            } else {
                logger.e("SavedArticlesViewModel", "Problem saving article: ${article.title}")
            }

        }
    }


    // deleting article in firestore collection
    fun deleteSavedArticle(article: Article) {

        viewModelScope.launch { // firestore operations are async so need this

            // get current user
            val userID = userRepository.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                logger.e("SavedArticlesViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            // at this point, successfully retrieved current user

            val success = savedArticlesRepository.deleteSavedArticle(userID, article)
            if (success) {
                logger.d("SavedArticlesViewModel", "Article successfully deleted: ${article.title}")
                _savedArticles.postValue(_savedArticles.value?.filter { it.url != article.url })
            } else {
                logger.e("SavedArticlesViewModel", "Problem deleting article: ${article.title}")
            }

        }

    }

    // retrieve user's saved articles from firestore
    fun fetchSavedArticles() {

        viewModelScope.launch{

            // get current user
            val userID = userRepository.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                logger.e("SavedArticlesViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            // at this point, successfully retrieved current user

            savedArticlesRepository.getSavedArticles(userID) { userSavedArticles ->
                logger.d("SavedArticlesViewModel", "Successfully fetched ${userSavedArticles.size} articles")
                _savedArticles.postValue(userSavedArticles) // ViewModel updates UI state
            }

        }

    }

}