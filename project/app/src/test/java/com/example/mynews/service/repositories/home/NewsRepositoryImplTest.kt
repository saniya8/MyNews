package com.example.mynews.service.repositories.home

import android.content.Context
import com.example.mynews.domain.entities.Source
import com.example.mynews.domain.entities.SourcesResponse
import com.example.mynews.service.news.NewsApiClient
import com.example.mynews.service.newsbias.NewsBiasProvider
import com.example.mynews.service.repositories.Constant
import com.example.mynews.utils.MainDispatcherRule
import com.example.mynews.utils.TestDataFactory
import com.example.mynews.utils.logger.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever


@OptIn(ExperimentalCoroutinesApi::class)
class NewsRepositoryImplTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context: Context = mock()
    private val apiClient: NewsApiClient = mock()
    private val biasProvider: NewsBiasProvider = mock()
    private val logger: Logger = mock()

    private lateinit var repository: NewsRepositoryImpl

    @Before
    fun setup() {
        repository = NewsRepositoryImpl(
            context = context,
            newsApiClient = apiClient,
            newsBiasProvider = biasProvider,
            logger = logger,
        )
    }

    @Test
    fun `getTopHeadlines returns expected response`() = runTest {
        val expected = TestDataFactory.createNewsResponse()
        whenever(apiClient.getTopHeadlines(any(), any())).thenReturn(expected)

        val result = repository.getTopHeadlines()

        assertEquals(expected, result)
        verify(apiClient).getTopHeadlines(language = "en", apiKey = Constant.NEWS_API_KEY)
    }

    @Test
    fun `getEverythingBySearch returns expected response`() = runTest {
        val expected = TestDataFactory.createNewsResponse()
        whenever(apiClient.getEverythingBySearch(any(), any(), any())).thenReturn(expected)

        val result = repository.getEverythingBySearch("climate")

        assertEquals(expected, result)
        verify(apiClient).getEverythingBySearch(language = "en", query = "climate", apiKey = Constant.NEWS_API_KEY)
    }

    @Test
    fun `getTopHeadlinesByCategory returns expected response`() = runTest {
        val expected = TestDataFactory.createNewsResponse()
        whenever(apiClient.getTopHeadlinesByCategory(any(), any(), any())).thenReturn(expected)

        val result = repository.getTopHeadlinesByCategory("technology")

        assertEquals(expected, result)
        verify(apiClient).getTopHeadlinesByCategory(language = "en", category = "technology", apiKey = Constant.NEWS_API_KEY)
    }

    @Test
    fun `getTopHeadlinesByCountry returns error response when sources fail`() = runTest {
        val sourcesResponse = SourcesResponse(status = "error", sources = emptyList())
        whenever(apiClient.getSourcesByCountry(any(), any(), any())).thenReturn(sourcesResponse)

        val result = repository.getTopHeadlinesByCountry("us")

        assertEquals("error", result.status)
        assertTrue(result.articles.isEmpty())
    }

    @Test
    fun `getTopHeadlinesByCountry returns headlines when sources are valid`() = runTest {
        val sourcesResponse = SourcesResponse(
            status = "ok",
            sources = listOf(Source(id = "abc-news", name = "ABC News"))
        )
        val expected = TestDataFactory.createNewsResponse()
        whenever(apiClient.getSourcesByCountry(any(), any(), any())).thenReturn(sourcesResponse)
        whenever(apiClient.getTopHeadlinesBySources(any(), any(), any())).thenReturn(expected)

        val result = repository.getTopHeadlinesByCountry("us")

        assertEquals(expected, result)
        verify(apiClient).getTopHeadlinesBySources(
            apiKey = Constant.NEWS_API_KEY,
            sources = "abc-news", // joined by comma
            language = "en"
        )
    }

    @Test
    fun `getEverythingByDateRange calculates correct from date and returns response`() = runTest {
        val expected = TestDataFactory.createNewsResponse()
        whenever(apiClient.getEverythingByDateRange(any(), any(), any())).thenReturn(expected)

        val result = repository.getEverythingByDateRange("Last 7 Days")

        assertEquals(expected, result)

        verify(apiClient).getEverythingByDateRange(
            eq(Constant.NEWS_API_KEY),
            argThat { this.matches(Regex("""\d{4}-\d{2}-\d{2}""")) },
            eq("en")
        )
    }


    @Test
    fun `startFetchingBiasData calls provider method`() = runTest {
        repository.startFetchingBiasData()
        verify(biasProvider).startFetchingBiasData()
    }

    @Test
    fun `getAllBiasMappings returns expected flow`() {
        val flow = MutableStateFlow(mapOf("cnn" to "left", "fox-news" to "right"))
        whenever(biasProvider.getAllBiasMappings()).thenReturn(flow)

        val result = repository.getAllBiasMappings()

        assertEquals(flow, result)
    }

    @Test
    fun `getBiasForSource returns expected result`() = runTest {
        whenever(biasProvider.getBiasForSource("cnn")).thenReturn("left")

        val result = repository.getBiasForSource("cnn")

        assertEquals("left", result)
    }
}

