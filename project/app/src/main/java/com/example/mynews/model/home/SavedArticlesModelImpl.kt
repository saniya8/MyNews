package com.example.mynews.model.home


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mynews.domain.model.home.SavedArticlesModel
import com.example.mynews.domain.repositories.home.SavedArticlesRepository
import com.example.mynews.domain.entities.Article
import javax.inject.Inject

class SavedArticlesModelImpl @Inject constructor(
    private val savedArticlesRepository: SavedArticlesRepository,
) : SavedArticlesModel {

    private val _savedArticles = MutableLiveData<List<Article>>(emptyList())
    override val savedArticles: LiveData<List<Article>> = _savedArticles

    override suspend fun saveArticle(userID: String, article: Article): Boolean {
        val success = savedArticlesRepository.saveArticle(userID, article)
        if (success) {
            Log.d("SavedArticlesModel", "Article successfully saved: ${article.title}")
        } else {
            Log.e("SavedArticlesModel", "Problem saving article: ${article.title}")
        }
        return success
    }

    override suspend fun deleteSavedArticle(userID: String, article: Article): Boolean {
        val success = savedArticlesRepository.deleteSavedArticle(userID, article)
        if (success) {
            Log.d("SavedArticlesModel", "Article successfully deleted: ${article.title}")
        } else {
            Log.e("SavedArticlesModel", "Problem deleting article: ${article.title}")
        }
        return success
    }

    override fun getSavedArticles(userID: String) {
        savedArticlesRepository.getSavedArticles(userID) { userSavedArticles ->
            _savedArticles.postValue(userSavedArticles)
        }
    }
}