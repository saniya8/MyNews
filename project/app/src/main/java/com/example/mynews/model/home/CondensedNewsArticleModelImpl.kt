package com.example.mynews.model.home


import android.util.Log
import com.example.mynews.domain.model.home.CondensedNewsArticleModel
import com.example.mynews.domain.repositories.home.CondensedNewsArticleRepository
import com.example.mynews.domain.repositories.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class CondensedNewsArticleModelImpl @Inject constructor(
    private val condensedNewsArticleRepository: CondensedNewsArticleRepository,
    private val settingsRepository: SettingsRepository,
) : CondensedNewsArticleModel {

    private val _currentArticleUrl = MutableStateFlow<String?>(null)
    override val currentArticleUrl: StateFlow<String?> = _currentArticleUrl

    private val _articleText = MutableStateFlow("Loading...")
    override val articleText: StateFlow<String> = _articleText

    private val _summarizedText = MutableStateFlow("Loading...")
    override val summarizedText: StateFlow<String> = _summarizedText

    override suspend fun fetchArticleText(url: String) {
        Log.d("Condensed Article", "URL clicked is: ${url}")

        // reset state before getting article text

        if (_currentArticleUrl.value != url) {
            _currentArticleUrl.value = url
            _articleText.value = ""
            _summarizedText.value = ""
        }

        try {
            val text = condensedNewsArticleRepository.getArticleText(url)
            if (url == _currentArticleUrl.value) {
                _articleText.value = text
            }
        } catch (e: Exception) {
            Log.e("CondensedNewsVM", "Error fetching article text: ${e.message}", e)
            if (url == _currentArticleUrl.value) {
                _articleText.value = "Failed to load article."
            }
        }
    }


    override suspend fun fetchSummarizedText(url: String, text: String, userID: String) {

        try {

            val numWords = settingsRepository.getNumWordsToSummarize(userID) ?: 100 // fallback to 100 (default) if not found
            Log.d("CondensedSettings", "Fetched numWords, sending to summarizer: $numWords")

            val summaryText = condensedNewsArticleRepository.summarizeText(text, numWords)
            if (url == _currentArticleUrl.value) {
                _summarizedText.value = summaryText
            }
        } catch (e: Exception) {
            Log.e("CondensedNewsVM", "Error fetching summarized text: ${e.message}", e)
            if (url == _currentArticleUrl.value) {
                _summarizedText.value = "Failed to load article summary."
            }
        }

    }

    override fun clearCondensedArticleState() {
        _currentArticleUrl.value = null
        _articleText.value = ""
        _summarizedText.value = ""
    }


    override fun clearSummarizedText() {
        _summarizedText.value = ""
    }

    override fun clearArticleText() {
        _articleText.value = ""
    }

}