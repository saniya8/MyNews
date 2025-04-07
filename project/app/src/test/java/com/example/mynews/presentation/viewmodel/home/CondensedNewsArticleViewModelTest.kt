package com.example.mynews.presentation.viewmodel.home

import com.example.mynews.domain.model.home.CondensedNewsArticleModel
import com.example.mynews.domain.model.user.UserModel
import com.example.mynews.utils.MainDispatcherRule
import com.example.mynews.utils.logger.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CondensedNewsArticleViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val condensedModel: CondensedNewsArticleModel = mock()
    private val userModel: UserModel = mock()
    private val logger: Logger = mock()

    private lateinit var viewModel: CondensedNewsArticleViewModel

    private val testUrl = "https://example.com/article"
    private val testText = "Some long article text"
    private val testUserId = "testUser123"

    @Before
    fun setup() {
        reset(condensedModel, userModel, logger)
        viewModel = CondensedNewsArticleViewModel(condensedModel, userModel, logger)
    }

    @Test
    fun `fetchArticleText calls model with correct url`() = runTest {
        viewModel.fetchArticleText(testUrl)
        advanceUntilIdle()

        verify(logger).d("Condensed Article", "URL clicked is: $testUrl")
        verify(condensedModel).fetchArticleText(testUrl)

        // also verify properties accessed by init
        verify(condensedModel).currentArticleUrl
        verify(condensedModel).articleText
        verify(condensedModel).summarizedText
        verifyNoMoreInteractions(condensedModel)
    }


    @Test
    fun `fetchSummarizedText calls model with valid user`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)

        viewModel.fetchSummarizedText(testUrl, testText)
        advanceUntilIdle()

        verify(userModel).getCurrentUserId()
        verify(condensedModel).fetchSummarizedText(testUrl, testText, testUserId)

        // also verify properties accessed by init
        verify(condensedModel).currentArticleUrl
        verify(condensedModel).articleText
        verify(condensedModel).summarizedText
        verifyNoMoreInteractions(condensedModel)
    }


    @Test
    fun `fetchSummarizedText logs error when user is null`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(null)

        viewModel.fetchSummarizedText(testUrl, testText)

        advanceUntilIdle()

        verify(logger).e(
            eq("CondensedViewModel"),
            eq("No user logged in. User ID is null or empty")
        )
        verify(condensedModel, never()).fetchSummarizedText(any(), any(), any())
    }


    @Test
    fun `clearCondensedArticleState calls model`() {
        viewModel.clearCondensedArticleState()

        verify(condensedModel).clearCondensedArticleState()
    }

    @Test
    fun `clearArticleText calls model`() {
        viewModel.clearArticleText()

        verify(condensedModel).clearArticleText()
    }

    @Test
    fun `clearSummarizedText calls model`() {
        viewModel.clearSummarizedText()

        verify(condensedModel).clearSummarizedText()
    }
}

