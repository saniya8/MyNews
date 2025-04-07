package com.example.mynews.model.home


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mynews.domain.model.home.HomeModel
import com.example.mynews.domain.repositories.goals.GoalsRepository
import com.example.mynews.domain.repositories.home.HomeRepository
import com.example.mynews.domain.entities.Article
import javax.inject.Inject

class HomeModelImpl @Inject constructor(
    private val homeRepository: HomeRepository,
    private val goalsRepository: GoalsRepository
) : HomeModel {

    private val _articleReactions = MutableLiveData<Map<String, String?>>()
    override val articleReactions: LiveData<Map<String, String?>> = _articleReactions

    override suspend fun getReaction(userID: String, article: Article): String? {
        return homeRepository.getReaction(userID, article)
    }

    override suspend fun setReaction(userID: String, article: Article, reaction: String?) {
        val result = homeRepository.setReaction(userID, article, reaction)
        when {
            result.isFirstReaction -> {
                // first time reacting to this article
                goalsRepository.logReactionAdded(userID)
            }

            result.wasDeleted -> {
                // user removed a reaction
                goalsRepository.logReactionRemoved(userID)
            }

            result.wasSwitched -> {
                // just switching between reactions — no impact on missions
            }
        }
    }

    override fun trackReactions(userID: String) {
        homeRepository.trackReactions(userID) { userArticleReactions ->
            Log.d("HomeModel", "Received updated reactions: $userArticleReactions")
            _articleReactions.postValue(userArticleReactions)
        }
    }




}