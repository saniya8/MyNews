package com.example.mynews.presentation.viewmodel.goals

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mynews.data.api.news.Article
import com.example.mynews.data.api.news.Source
import com.example.mynews.data.logger.NoOpLogger
import com.example.mynews.domain.model.Mission
import com.example.mynews.domain.repositories.GoalsRepository
import com.example.mynews.domain.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class GoalsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var goalsRepository: GoalsRepository

    @Mock
    private lateinit var userRepository: UserRepository

    private lateinit var viewModel: GoalsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = GoalsViewModel(
            goalsRepository = goalsRepository,
            userRepository = userRepository,
            logger = NoOpLogger()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------------------------------------------------------

    // TESTING: logArticleRead()


    @Test
    fun `logArticleRead should call repository with correct userID and article`() = runTest {
        // step 1: arrange
        val fakeUserId = "user123"
        val fakeArticle = Article(
            author = "Test Author",
            content = "Test Content",
            description = "Test Description",
            publishedAt = "2024-04-06",
            source = Source(id = "1", name = "Test Source"),
            title = "Test Article",
            url = "https://example.com/test-article",
            urlToImage = "https://example.com/image.jpg"
        )

        // mock user id
        `when`(userRepository.getCurrentUserId()).thenReturn(fakeUserId)

        // step 2: act
        viewModel.logArticleRead(fakeArticle)

        // advance coroutine execution
        testDispatcher.scheduler.advanceUntilIdle()

        // step 3: assert
        verify(goalsRepository).logArticleRead(fakeUserId, fakeArticle)
    }

    @Test
    fun `logReaction should update and complete react_to_article missions`() = runTest {
        // Arrange
        val fakeUserId = "testUser123"
        val mission = Mission(
            id = "mission1",
            name = "React",
            description = "React to articles",
            targetCount = 3,
            currentCount = 2, // one away from completion
            isCompleted = false,
            type = "react_to_article"
        )

        `when`(userRepository.getCurrentUserId()).thenReturn(fakeUserId)
        `when`(goalsRepository.getMissions(fakeUserId)).thenReturn(listOf(mission))

        // Act
        viewModel.logReaction()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert — use real values (not matchers like eq())
        verify(goalsRepository).updateMissionProgress(fakeUserId, mission.id, 3)
        verify(goalsRepository).markMissionComplete(fakeUserId, mission.id)
    }

    // ---------------------------------------------------------

    // TESTING: logReaction()

    @Test
    fun `logReaction should not complete mission if new count is below target`() = runTest {
        val fakeUserId = "user123"
        val mission = Mission(
            id = "m1",
            name = "React",
            description = "",
            targetCount = 5,
            currentCount = 2,
            isCompleted = false,
            type = "react_to_article"
        )

        `when`(userRepository.getCurrentUserId()).thenReturn(fakeUserId)
        `when`(goalsRepository.getMissions(fakeUserId)).thenReturn(listOf(mission))

        viewModel.logReaction()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(goalsRepository).updateMissionProgress(fakeUserId, "m1", 3)
        verify(goalsRepository, never()).markMissionComplete(any(), any())
    }

    @Test
    fun `logReaction should not update already completed mission`() = runTest {
        val fakeUserId = "user123"
        val completedMission = Mission(
            id = "m2",
            name = "React",
            description = "",
            targetCount = 5,
            currentCount = 5,
            isCompleted = true,
            type = "react_to_article"
        )

        `when`(userRepository.getCurrentUserId()).thenReturn(fakeUserId)
        `when`(goalsRepository.getMissions(fakeUserId)).thenReturn(listOf(completedMission))

        viewModel.logReaction()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(goalsRepository, never()).updateMissionProgress(any(), any(), any())
        verify(goalsRepository, never()).markMissionComplete(any(), any())
    }

    @Test
    fun `logReaction should ignore missions with other types`() = runTest {
        val fakeUserId = "user123"
        val readMission = Mission(
            id = "m3",
            name = "Read",
            description = "",
            targetCount = 3,
            currentCount = 1,
            isCompleted = false,
            type = "read_article"
        )

        `when`(userRepository.getCurrentUserId()).thenReturn(fakeUserId)
        `when`(goalsRepository.getMissions(fakeUserId)).thenReturn(listOf(readMission))

        viewModel.logReaction()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(goalsRepository, never()).updateMissionProgress(any(), any(), any())
        verify(goalsRepository, never()).markMissionComplete(any(), any())
    }

}