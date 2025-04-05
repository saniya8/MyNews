package com.example.mynews.presentation.viewmodel.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mynews.utils.logger.NoOpLogger
import com.example.mynews.domain.repositories.CondensedNewsArticleRepository
import com.example.mynews.domain.repositories.SettingsRepository
import com.example.mynews.domain.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever


@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class CondensedNewsArticleViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule() // allows LiveData/StateFlow testing

    @Mock
    private lateinit var condensedNewsArticleRepository: CondensedNewsArticleRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var settingsRepository: SettingsRepository

    private lateinit var viewModel: CondensedNewsArticleViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = CondensedNewsArticleViewModel(
            condensedNewsArticleRepository = condensedNewsArticleRepository,
            userRepository = userRepository,
            settingsRepository = settingsRepository,
            logger = NoOpLogger(),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------------------------------------------------------

    // TESTING: fetchArticleText()

    @Test
    fun `fetchArticleText sets articleText on success`() = runTest {
        // Arrange
        val testUrl = "https://example.com/test-article"
        val testArticleText = "This is the full article content."

        // Stub the repository method
        whenever(condensedNewsArticleRepository.getArticleText(testUrl)).thenReturn(testArticleText)

        // Act
        viewModel.fetchArticleText(testUrl)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        Assert.assertEquals(testUrl, viewModel.currentArticleUrl.value)
        Assert.assertEquals(testArticleText, viewModel.articleText.value)
    }

    // ---------------------------------------------------------

    // TESTING: fetchSummarizedText()

    @Test
    fun `fetchSummarizedText sets summarizedText on success`() = runTest {
        // Arrange
        val testUrl = "https://example.com/article"
        val fullText = "This is a full article text that needs summarizing."
        val userId = "user123"
        val numWords = 50
        val summary = "This is the summarized version."

        // Stub repository methods
        whenever(userRepository.getCurrentUserId()).thenReturn(userId)
        whenever(settingsRepository.getNumWordsToSummarize(userId)).thenReturn(numWords)
        whenever(condensedNewsArticleRepository.summarizeText(fullText, numWords)).thenReturn(summary)

        // Act
        viewModel.fetchArticleText(testUrl) // set the currentArticleUrl
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.fetchSummarizedText(testUrl, fullText)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        Assert.assertEquals(summary, viewModel.summarizedText.value)
    }

    // ---------------------------------------------------------

    // TESTING: clearCondensedArticleState()

    @Test
    fun `clearCondensedArticleState should reset articleUrl, articleText, and summarizedText`() = runTest {
        // Arrange: Set initial state
        viewModel.fetchArticleText("https://example.com/article")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.fetchSummarizedText("https://example.com/article", "Some full text")
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.clearCondensedArticleState()

        // Assert
        Assert.assertNull(viewModel.currentArticleUrl.value)
        Assert.assertEquals("", viewModel.articleText.value)
        Assert.assertEquals("", viewModel.summarizedText.value)
    }

    // ---------------------------------------------------------

    // TESTING: clearSummarizedText()

    @Test
    fun `clearSummarizedText should reset summarizedText to empty`() = runTest {
        // Arrange — simulate a filled summary
        viewModel.fetchSummarizedText("https://example.com", "Some long article text")
        testDispatcher.scheduler.advanceUntilIdle()

        // Act
        viewModel.clearSummarizedText()

        // Assert
        Assert.assertEquals("", viewModel.summarizedText.value)
    }

    // ---------------------------------------------------------

    // TESTING: clearArticleText()

    @Test
    fun `clearArticleText should reset articleText to empty`() = runTest {
        // Arrange — simulate a filled article
        viewModel.fetchArticleText("https://example.com")
        testDispatcher.scheduler.advanceUntilIdle()

        // Act — clear it
        viewModel.clearArticleText()

        // Assert
        Assert.assertEquals("", viewModel.articleText.value)
    }

}