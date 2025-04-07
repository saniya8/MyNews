package com.example.mynews.model.goals

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mynews.domain.entities.Article
import com.example.mynews.domain.entities.Mission
import com.example.mynews.domain.model.goals.GoalsModel
import com.example.mynews.domain.repositories.goals.GoalsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GoalsModelImpl(
    private val goalsRepository: GoalsRepository
) : GoalsModel {

    private val _streakCount = MutableLiveData<Int>()
    override val streakCount: LiveData<Int> = _streakCount

    private val _lastReadDate = MutableLiveData<String>()

    private val _hasLoggedToday = mutableStateOf(false)
    override val hasLoggedToday: State<Boolean> = _hasLoggedToday

    private val _missions = MutableLiveData<List<Mission>>()
    override val missions: LiveData<List<Mission>> = _missions

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun startStreakListener(userId: String) {
        coroutineScope.launch {
            goalsRepository.getStreakFlow(userId).collectLatest { (count, lastReadDate) ->
                _streakCount.postValue(count)
                _lastReadDate.postValue(lastReadDate)

                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                _hasLoggedToday.value = lastReadDate == today
            }
        }
    }

    override fun startMissionsListener(userId: String) {
        coroutineScope.launch {
            goalsRepository.getMissionsFlow(userId).collectLatest { missions ->
                _missions.postValue(missions)
            }
        }
    }

    override fun logArticleRead(userId: String, article: Article) {
        coroutineScope.launch {
            goalsRepository.logArticleRead(userId, article)
        }
    }
}