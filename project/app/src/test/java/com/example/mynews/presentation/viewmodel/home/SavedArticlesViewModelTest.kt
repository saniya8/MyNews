package com.example.mynews.presentation.viewmodel.home


import com.example.mynews.domain.model.home.SavedArticlesModel
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
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SavedArticlesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var savedArticlesViewModel: SavedArticlesViewModel
    private val userModel: UserModel = mock()
    private val savedArticlesModel: SavedArticlesModel = mock()
    private val logger: Logger = mock()

    private val testUserId = "unittestmock1"
    private val testArticle = TestDataFactory.createIndexedArticle(3)

    @Before
    fun setup() {
        savedArticlesViewModel = SavedArticlesViewModel(userModel, savedArticlesModel, logger)
        reset(userModel, savedArticlesModel, logger) // reset mocks before each test
    }

    @Test
    fun `saveArticle calls model with valid user`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)

        savedArticlesViewModel.saveArticle(testArticle)

        advanceUntilIdle()

        verify(savedArticlesModel).saveArticle(testUserId, testArticle)
        verifyNoMoreInteractions(savedArticlesModel)
    }

    @Test
    fun `saveArticle logs error when user is null`() = runTest {

            val article = TestDataFactory.createIndexedArticle(1)
            whenever(userModel.getCurrentUserId()).thenReturn(null)

            val viewModel = SavedArticlesViewModel(userModel, savedArticlesModel, logger)


            viewModel.saveArticle(article)
            advanceUntilIdle()


            verify(logger).e(
                "SavedArticlesViewModel",
                "No user logged in. User ID is null or empty"
            )
            verify(savedArticlesModel, never()).saveArticle(any(), any())
        }



    @Test
    fun `deleteSavedArticle calls model with valid user`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)

        savedArticlesViewModel.deleteSavedArticle(testArticle)

        advanceUntilIdle()

        verify(savedArticlesModel).deleteSavedArticle(testUserId, testArticle)
        verifyNoMoreInteractions(savedArticlesModel)
    }

    @Test
    fun `deleteSavedArticle logs error when user is null`() = runTest {

        val article = TestDataFactory.createIndexedArticle(2)
        whenever(userModel.getCurrentUserId()).thenReturn(null)

        val viewModel = SavedArticlesViewModel(userModel, savedArticlesModel, logger)


        viewModel.deleteSavedArticle(article)
        advanceUntilIdle()


        verify(logger).e(
            "SavedArticlesViewModel",
            "No user logged in. User ID is null or empty"
        )
        verify(savedArticlesModel, never()).deleteSavedArticle(any(), any())
    }



    @Test
    fun `fetchSavedArticles calls model with valid user`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)

        savedArticlesViewModel.fetchSavedArticles()

        advanceUntilIdle()

        verify(savedArticlesModel).getSavedArticles(testUserId)
    }

    @Test
    fun `fetchSavedArticles logs error when user is null`() = runTest {

        whenever(userModel.getCurrentUserId()).thenReturn(null)

        val viewModel = SavedArticlesViewModel(userModel, savedArticlesModel, logger)


        viewModel.fetchSavedArticles()
        advanceUntilIdle()


        verify(logger).e(
            "SavedArticlesViewModel",
            "No user logged in. User ID is null or empty"
        )
        verify(savedArticlesModel, never()).getSavedArticles(any())
    }


}

