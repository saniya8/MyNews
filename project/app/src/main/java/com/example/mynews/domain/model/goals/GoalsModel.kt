package com.example.mynews.domain.model.goals

import androidx.compose.runtime.State
import androidx.lifecycle.LiveData
import com.example.mynews.domain.entities.Article
import com.example.mynews.domain.entities.Mission


interface GoalsModel {

    val streakCount: LiveData<Int>
    val hasLoggedToday: State<Boolean>
    val missions: LiveData<List<Mission>>
    fun startStreakListener(userId: String)
    fun startMissionsListener(userId: String)
    fun logArticleRead(userId: String, article: Article)
}