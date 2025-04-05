package com.example.mynews.presentation.viewmodel.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mynews.service.news.Article
import com.example.mynews.service.news.Source
import com.example.mynews.utils.logger.NoOpLogger
import com.example.mynews.domain.repositories.HomeRepository
import com.example.mynews.domain.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

/*@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class HomeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var homeRepository: HomeRepository

    private lateinit var viewModel: HomeViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = HomeViewModel(
            userRepository = userRepository,
            homeRepository = homeRepository,
            logger = NoOpLogger(),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------------------------------------------------------

    // TESTING: fetchReaction()

    @Test
    fun `fetchReaction should return correct reaction for article`() = runTest {
        // Arrange
        val fakeUserId = "user123"
        val fakeReaction = "like"
        val fakeArticle = Article(
            author = "Jane",
            content = "Test content",
            description = "Test desc",
            publishedAt = "2024-04-06",
            source = Source(id = "source1", name = "TestSource"),
            title = "Test Article",
            url = "https://example.com/article",
            urlToImage = "https://example.com/image.jpg"
        )

        whenever(userRepository.getCurrentUserId()).thenReturn(fakeUserId)
        whenever(homeRepository.getReaction(fakeUserId, fakeArticle)).thenReturn(fakeReaction)

        var result: String? = null

        // Act
        viewModel.fetchReaction(fakeArticle) { reaction ->
            result = reaction
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        Assert.assertEquals(fakeReaction, result)
    }

    // ---------------------------------------------------------

    // TESTING: updateReaction()

    @Test
    fun `updateReaction should call repository with correct userID and reaction`() = runTest {
        // Arrange
        val fakeUserId = "user123"
        val fakeReaction = "dislike"
        val fakeArticle = Article(
            author = "Author",
            content = "Some content",
            description = "Some desc",
            publishedAt = "2024-04-06",
            source = Source(id = "source1", name = "TestSource"),
            title = "Article Title",
            url = "https://example.com/article",
            urlToImage = "https://example.com/image.jpg"
        )

        whenever(userRepository.getCurrentUserId()).thenReturn(fakeUserId)

        // Act
        viewModel.updateReaction(fakeArticle, fakeReaction)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        verify(homeRepository).setReaction(fakeUserId, fakeArticle, fakeReaction)
    }

}

 */