package com.example.mynews.presentation.viewmodel.goals

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import com.example.mynews.domain.entities.Article
import com.example.mynews.domain.model.goals.GoalsModel
import com.example.mynews.domain.model.user.UserModel
import com.example.mynews.utils.MainDispatcherRule
import com.example.mynews.utils.TestDataFactory
import com.example.mynews.utils.logger.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GoalsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val goalsModel: GoalsModel = mock()
    private val userModel: UserModel = mock()
    private val logger: Logger = mock()

    private val testUserId = "test-user-123"
    private val testArticle: Article = TestDataFactory.createIndexedArticle(42)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        // regular field stubs
        whenever(goalsModel.streakCount).thenReturn(MutableLiveData(5))
        whenever(goalsModel.missions).thenReturn(MutableLiveData(emptyList()))
        whenever(goalsModel.hasLoggedToday).thenReturn(mutableStateOf(false))
    }

    @Test
    fun `init triggers fetchStreak and fetchMissions when user is valid`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)

        val viewModel = GoalsViewModel(goalsModel, userModel, logger)

        advanceUntilIdle()

        verify(goalsModel).startStreakListener(testUserId)
        verify(goalsModel).startMissionsListener(testUserId)
    }

    @Test
    fun `init logs error when user is null`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(null)

        val viewModel = GoalsViewModel(goalsModel, userModel, logger)

        advanceUntilIdle()

        verify(logger, times(2)).e(
            eq("GoalsViewModel"),
            eq("No user logged in. User ID is null or empty")
        )
        verify(goalsModel, never()).startStreakListener(any())
        verify(goalsModel, never()).startMissionsListener(any())
    }

    @Test
    fun `logArticleRead calls model when user is valid`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)

        val viewModel = GoalsViewModel(goalsModel, userModel, logger)

        viewModel.logArticleRead(testArticle)
        advanceUntilIdle()

        verify(goalsModel).logArticleRead(testUserId, testArticle)
    }

    @Test
    fun `logArticleRead logs error when user is null`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(null)

        val viewModel = GoalsViewModel(goalsModel, userModel, logger)

        viewModel.logArticleRead(testArticle)
        advanceUntilIdle()

        verify(logger, atLeast(1)).e(
            eq("GoalsViewModel"),
            eq("No user logged in. User ID is null or empty")
        )
    }

}