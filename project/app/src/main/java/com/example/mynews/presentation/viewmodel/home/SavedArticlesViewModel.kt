package com.example.mynews.presentation.viewmodel.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.data.api.Article
import com.example.mynews.domain.repositories.SavedArticlesRepository
import com.example.mynews.domain.repositories.UserRepository
//import com.kwabenaberko.newsapilib.NewsApiClient
//import com.kwabenaberko.newsapilib.models.request.TopHeadlinesRequest
//import com.kwabenaberko.newsapilib.models.response.ArticleResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedArticlesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val savedArticlesRepository: SavedArticlesRepository,
): ViewModel() {


    private val _savedArticles = MutableLiveData<List<Article>>(emptyList())
    val savedArticles: LiveData<List<Article>> = _savedArticles

    // saving article in firestore collection
    fun saveArticle(article: Article) {

        viewModelScope.launch { // firestore operations are async so need this

            // get current user
            val userID = userRepository.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                Log.e("SavedArticlesViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            val success = savedArticlesRepository.saveArticle(userID, article)
            if (success) {
                Log.d("SavedArticlesViewModel", "Article successfully saved: ${article.title}")
            } else {
                Log.e("SavedArticlesViewModel", "Problem saving article: ${article.title}")
            }

        }
    }


    // deleting article in firestore collection
    fun deleteSavedArticle(article: Article) {

        viewModelScope.launch { // firestore operations are async so need this

            // get current user
            val userID = userRepository.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                Log.e("SavedArticlesViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            // at this point, successfully retrieved current user

            val success = savedArticlesRepository.deleteSavedArticle(userID, article)
            if (success) {
                Log.d("SavedArticlesViewModel", "Article successfully deleted: ${article.title}")
                _savedArticles.postValue(_savedArticles.value?.filter { it.url != article.url })
            } else {
                Log.e("SavedArticlesViewModel", "Problem deleting article: ${article.title}")
            }

        }

    }

    // retrieve user's saved articles from firestore
    fun fetchSavedArticles() {

        viewModelScope.launch{

            // get current user
            val userID = userRepository.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                Log.e("SavedArticlesViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            // at this point, successfully retrieved current user

            savedArticlesRepository.getSavedArticles(userID) { userSavedArticles ->
                Log.d("SavedArticlesViewModel", "Successfully fetched ${userSavedArticles.size} articles")
                _savedArticles.postValue(userSavedArticles) // ViewModel updates UI state
            }

        }

    }

}