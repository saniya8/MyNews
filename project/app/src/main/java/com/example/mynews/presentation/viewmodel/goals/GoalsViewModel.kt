package com.example.mynews.presentation.viewmodel.goals

import androidx.compose.runtime.State
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.domain.entities.Article
import com.example.mynews.domain.entities.Mission
import com.example.mynews.domain.model.goals.GoalsModel
import com.example.mynews.domain.model.user.UserModel
import com.example.mynews.utils.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val goalsModel: GoalsModel,
    private val userModel: UserModel,
    private val logger: Logger,
) : ViewModel() {

    val streakCount: LiveData<Int> = goalsModel.streakCount
    val hasLoggedToday: State<Boolean> = goalsModel.hasLoggedToday
    val missions: LiveData<List<Mission>> = goalsModel.missions

    init {
        fetchStreak()
        fetchMissions()
    }

    private fun fetchStreak() {
        viewModelScope.launch {
            val userID = userModel.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                logger.e("GoalsViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            goalsModel.startStreakListener(userID)
        }
    }

    private fun fetchMissions() {
        viewModelScope.launch {
            val userID = userModel.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                logger.e("GoalsViewModel", "No user logged in. User ID is null or empty")
                return@launch
            }

            goalsModel.startMissionsListener(userID)
        }
    }

    fun logArticleRead(article: Article) {
        viewModelScope.launch {
            val userID = userModel.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                logger.e("GoalsViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            goalsModel.logArticleRead(userID, article)
        }
    }

    // logAddReaction, logRemoveReaction: put in goalsRepositoryImpl because it needs to be called by homeViewModel upon
    // fresh reaction, not upon updating reaction to previously-reacted article
    // homeViewModel can call logReaction from goalsRepositoryImpl, not from goalsViewModel

    // logAddOrRemoveFriend: put in goalsRepositoryImpl because it needs to be called by friendsViewModel upon
    // adding or removing a friend
    // friendsViewModel can call logAddOrRemoveFriend from goalsRepositoryImpl, not from goalsViewModel
}