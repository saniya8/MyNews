package com.example.mynews.presentation.viewmodel.home

import com.example.mynews.domain.model.home.NewsModel
import com.example.mynews.utils.MainDispatcherRule
import com.example.mynews.utils.logger.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: NewsViewModel
    private val newsModel: NewsModel = mock()
    private val logger: Logger = mock()

    @Before
    fun setup() {
        viewModel = NewsViewModel(newsModel, logger)
    }

    @Test
    fun `fetchTopHeadlines calls model and sets isFiltering false`() = runTest {
        whenever(newsModel.fetchTopHeadlines()).thenReturn(Unit)

        viewModel.fetchTopHeadlines()
        advanceUntilIdle()

        verify(newsModel).fetchTopHeadlines()
        assert(!viewModel.isFiltering.value)
    }

    @Test
    fun `fetchTopHeadlines does not call model again if already fetched`() = runTest {
        whenever(newsModel.fetchTopHeadlines()).thenReturn(Unit)

        viewModel.fetchTopHeadlines()
        advanceUntilIdle()

        viewModel.fetchTopHeadlines()
        advanceUntilIdle()

        verify(newsModel, times(1)).fetchTopHeadlines()
    }

    @Test
    fun `fetchTopHeadlines with forceFetch bypasses hasFetched check`() = runTest {
        whenever(newsModel.fetchTopHeadlines()).thenReturn(Unit)

        viewModel.fetchTopHeadlines()
        advanceUntilIdle()

        viewModel.fetchTopHeadlines(forceFetch = true)
        advanceUntilIdle()

        verify(newsModel, times(2)).fetchTopHeadlines()
    }

    @Test
    fun `fetchEverythingBySearch calls model and sets isFiltering true`() = runTest {
        whenever(newsModel.fetchEverythingBySearch("climate")).thenReturn(Unit)

        viewModel.fetchEverythingBySearch("climate")
        advanceUntilIdle()

        verify(newsModel).fetchEverythingBySearch("climate")
        assert(viewModel.isFiltering.value)
    }

    @Test
    fun `fetchTopHeadlinesByCategory calls model and sets isFiltering true`() = runTest {
        whenever(newsModel.fetchTopHeadlinesByCategory("technology")).thenReturn(Unit)

        viewModel.fetchTopHeadlinesByCategory("technology")
        advanceUntilIdle()

        verify(newsModel).fetchTopHeadlinesByCategory("technology")
        assert(viewModel.isFiltering.value)
    }

    @Test
    fun `fetchTopHeadlinesByCountry calls model and sets isFiltering true`() = runTest {
        whenever(newsModel.fetchTopHeadlinesByCountry("us")).thenReturn(Unit)

        viewModel.fetchTopHeadlinesByCountry("us")
        advanceUntilIdle()

        verify(newsModel).fetchTopHeadlinesByCountry("us")
        assert(viewModel.isFiltering.value)
    }

    @Test
    fun `fetchEverythingByDateRange calls model and sets isFiltering true`() = runTest {
        whenever(newsModel.fetchEverythingByDateRange("2023-01-01_to_2023-12-31")).thenReturn(Unit)

        viewModel.fetchEverythingByDateRange("2023-01-01_to_2023-12-31")
        advanceUntilIdle()

        verify(newsModel).fetchEverythingByDateRange("2023-01-01_to_2023-12-31")
        assert(viewModel.isFiltering.value)
    }

    @Test
    fun `fetchBiasForSource delegates to model`() {
        val callback = mock<(String) -> Unit>()
        viewModel.fetchBiasForSource("CNN", callback)

        verify(newsModel).fetchBiasForSource(eq("CNN"), eq(callback))
    }

    @Test
    fun `fetchTopHeadlines logs error when model throws`() = runTest {
        whenever(newsModel.fetchTopHeadlines()).thenThrow(RuntimeException("Fake error"))

        viewModel.fetchTopHeadlines(forceFetch = true)
        advanceUntilIdle()

        verify(logger).e(
            eq("NewsAPI Error"),
            eq("Failed to fetch top headlines: Fake error"),
            any()
        )
        assert(!viewModel.isFiltering.value)
    }

    @Test
    fun `fetchEverythingBySearch logs error when model throws`() = runTest {
        whenever(newsModel.fetchEverythingBySearch("war")).thenThrow(RuntimeException("Network error"))

        viewModel.fetchEverythingBySearch("war")
        advanceUntilIdle()

        verify(logger).e(
            eq("NewsAPI Error"),
            eq("Failed to fetch search results: Network error"),
            any()
        )
        assert(!viewModel.isFiltering.value)
    }

    @Test
    fun `fetchTopHeadlinesByCategory logs error when model throws`() = runTest {
        whenever(newsModel.fetchTopHeadlinesByCategory("sports")).thenThrow(RuntimeException("API issue"))

        viewModel.fetchTopHeadlinesByCategory("sports")
        advanceUntilIdle()

        verify(logger).e(
            eq("NewsAPI Error"),
            eq("Failed to fetch headlines by category: API issue"),
            any()
        )
        assert(!viewModel.isFiltering.value)
    }

    @Test
    fun `fetchTopHeadlinesByCountry logs error when model throws`() = runTest {
        whenever(newsModel.fetchTopHeadlinesByCountry("uk")).thenThrow(RuntimeException("HTTP 500"))

        viewModel.fetchTopHeadlinesByCountry("uk")
        advanceUntilIdle()

        verify(logger).e(
            eq("NewsAPI Error"),
            eq("Failed to fetch headlines by country: HTTP 500"),
            any()
        )
        assert(!viewModel.isFiltering.value)
    }

    @Test
    fun `fetchEverythingByDateRange logs error when model throws`() = runTest {
        whenever(newsModel.fetchEverythingByDateRange("range")).thenThrow(RuntimeException("Timeout"))

        viewModel.fetchEverythingByDateRange("range")
        advanceUntilIdle()

        verify(logger).e(
            eq("NewsAPI Error"),
            eq("Failed to fetch headlines by date range: Timeout"),
            any()
        )
        assert(!viewModel.isFiltering.value)
    }
}
