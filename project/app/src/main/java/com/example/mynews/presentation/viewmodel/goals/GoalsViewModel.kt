package com.example.mynews.presentation.viewmodel.goals

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val userRepository: UserRepository
) : ViewModel() {

    private val _streakCount = MutableLiveData<Int>()
    val streakCount: LiveData<Int> = _streakCount

    private val _lastReadDate = MutableLiveData<String>()
    val lastReadDate: LiveData<String> = _lastReadDate

    val hasLoggedToday = mutableStateOf(false)

    init {
        fetchStreak()
    }

    fun logArticleRead(articleId: String) {
        viewModelScope.launch {
            val userID = userRepository.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                Log.e("GoalsViewModel", "No user logged in. User ID is null or empty")
                return@launch // return
            }

            goalsRepository.logArticleRead(userID, articleId)
        }
    }

    private fun fetchStreak() {
        viewModelScope.launch {
            val userID = userRepository.getCurrentUserId()

            if (userID.isNullOrEmpty()) {
                Log.e("GoalsViewModel", "No user logged in. User ID is null or empty")
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
}