package com.example.mynews.presentation.viewmodel.goals

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.service.news.Article
import com.example.mynews.utils.logger.Logger
import com.example.mynews.domain.entities.Mission
import com.example.mynews.domain.repositories.GoalsRepository
import com.example.mynews.domain.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalsRepository: GoalsRepository,
    private val userRepository: UserRepository,
    private val logger: Logger,
) : ViewModel() {

    private val _streakCount = MutableLiveData<Int>()
    val streakCount: LiveData<Int> = _streakCount

    private val _lastReadDate = MutableLiveData<String>()
    val lastReadDate: LiveData<String> = _lastReadDate

    val hasLoggedToday = mutableStateOf(false)


    private val _missions = MutableLiveData<List<Mission>>(emptyList())
    val missions: LiveData<List<Mission>> = _missions


    init {
        fetchStreak()
        fetchMissions()
    }

    fun logArticleRead(article: Article) {
        viewModelScope.launch {
            val userID = userRepository.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                logger.e("GoalsViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            goalsRepository.logArticleRead(userID, article)
        }
    }

    // below is only called when article being reacted to is being reacted to for first time (so fresh reaction, not updated reaction)
    // this is due to LaunchedEffect in HomeScreen which only calls this if isFirstReactionForArticle is true
    fun logReaction(article: Article) {

        // check if article has already been reacted to
        val encodedUrl = Uri.encode(article.url)
        //if (reactedArticles.contains(encodedUrl)) {
        //    logger.d("GoalsViewModel", "Already logged reaction for ${article.title}")
        //    return
       // }
        //reactedArticles.add(encodedUrl)

        viewModelScope.launch {


            val userID = userRepository.getCurrentUserId()
            if (userID.isNullOrEmpty()) {
                logger.e("GoalsViewModel", "No user logged in. User ID is null or empty")
                return@launch
            }

            goalsRepository.logReaction(userID)

            // moved the below to  goalsRepository.logReaction
            // check if the reaction was for previously-reacted article or for new article

            //val isFirstTimeReaction = goalsRepository.isFirstTimeReaction(userID, article)
            //if (!isFirstTimeReaction) {
            //    logger.d("GoalsViewModel", "Reaction already exists for this article, not counting it")
            //    return@launch
            //}
            // only contribute reactions for new articles to missions
            /*val missions = goalsRepository.getMissions(userID)
            missions.filter { it.type == "react_to_article" && !it.isCompleted }.forEach { mission ->
                val newCount = mission.currentCount + 1
                goalsRepository.updateMissionProgress(userID, mission.id, newCount)
                if (newCount >= mission.targetCount) {
                    goalsRepository.markMissionComplete(userID, mission.id)
                }
            }*/
        }
    }

    private fun fetchStreak() {
        viewModelScope.launch {
            val userID = userRepository.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                logger.e("GoalsViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }
            goalsRepository.getStreakFlow(userID).collectLatest { (count, lastReadDate) ->
                _streakCount.value = count
                _lastReadDate.value = lastReadDate
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                hasLoggedToday.value = lastReadDate == today
            }
        }
    }

    private fun fetchMissions() {
        viewModelScope.launch {
            val userID = userRepository.getCurrentUserId()
            if (userID.isNullOrEmpty()) {
                logger.e("GoalsViewModel", "No user logged in. User ID is null or empty")
                return@launch
            }
            goalsRepository.getMissionsFlow(userID).collectLatest { missions ->
                _missions.value = missions
            }
        }
    }
}