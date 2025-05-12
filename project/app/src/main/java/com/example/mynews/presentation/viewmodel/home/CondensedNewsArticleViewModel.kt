package com.example.mynews.presentation.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.domain.model.home.CondensedNewsArticleModel
import com.example.mynews.domain.model.user.UserModel
import com.example.mynews.utils.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CondensedNewsArticleViewModel @Inject constructor(
    private val condensedNewsArticleModel: CondensedNewsArticleModel,
    private val userModel: UserModel,
    private val logger: Logger,
) : ViewModel() {

    val currentArticleUrl = condensedNewsArticleModel.currentArticleUrl
    val articleText = condensedNewsArticleModel.articleText
    val summarizedText = condensedNewsArticleModel.summarizedText


    fun fetchArticleText(url: String) {
        logger.d("Condensed Article", "URL clicked is: ${url}")

        viewModelScope.launch {
            condensedNewsArticleModel.fetchArticleText(url)
        }
    }

    fun fetchSummarizedText(url: String, text: String) {
        viewModelScope.launch {

                // get current user
                val userID = userModel.getCurrentUserId()

                if (userID.isNullOrEmpty()) {
                    logger.e("CondensedViewModel", "No user logged in. User ID is null or empty")
                    return@launch // return
                }

            condensedNewsArticleModel.fetchSummarizedText(url, text, userID)

        }
    }

    fun clearCondensedArticleState() {
        condensedNewsArticleModel.clearCondensedArticleState()
    }


    fun clearSummarizedText() {
        condensedNewsArticleModel.clearSummarizedText()
    }

    fun clearArticleText() {
        condensedNewsArticleModel.clearArticleText()
    }

}