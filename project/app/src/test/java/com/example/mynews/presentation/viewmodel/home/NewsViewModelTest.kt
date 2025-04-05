package com.example.mynews.presentation.viewmodel.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mynews.service.news.Article
import com.example.mynews.service.news.NewsResponse
import com.example.mynews.service.news.Source
import com.example.mynews.utils.logger.NoOpLogger
import com.example.mynews.domain.repositories.NewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class NewsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var newsRepository: NewsRepository

    private lateinit var viewModel: NewsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        val fakeBiasFlow = MutableStateFlow<Map<String, String>>(emptyMap())
        whenever(newsRepository.getAllBiasMappings()).thenReturn(fakeBiasFlow)

        viewModel = NewsViewModel(
            newsRepository = newsRepository,
            logger = NoOpLogger(),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------------------------------------------------------

    // TESTING: handleNewsResponse()

    @Test
    fun `handleNewsResponse should post articles when response is OK with articles`() = runTest {
        // Arrange
        val articleList = listOf(
            Article(
                author = "Author A",
                content = "Content A",
                description = "Description A",
                publishedAt = "2024-04-05",
                source = Source(id = "cnn", name = "CNN"),
                title = "Title A",
                url = "https://example.com/a",
                urlToImage = "https://example.com/a.jpg"
            )
        )

        val response = NewsResponse(status = "ok", totalResults = 1, articles = articleList)

        // Act
        viewModel.handleNewsResponse(response)

        // Assert
        Assert.assertEquals(articleList, viewModel.articles.value)
    }

    @Test
    fun `handleNewsResponse should post empty list when response is not OK or has no articles`() = runTest {
        // Arrange
        val emptyResponse = NewsResponse(status = "ok", totalResults = 0, articles = emptyList())

        // Act
        viewModel.handleNewsResponse(emptyResponse)

        // Assert
        Assert.assertTrue(viewModel.articles.value?.isEmpty() == true)
    }

    // ---------------------------------------------------------

    // TESTING: fetchTopHeadlines()

    @Test
    fun `fetchTopHeadlines should fetch and post articles on success`() = runTest {
        // Arrange
        val testArticles = listOf(
            Article(
                author = "Author A",
                content = "Content A",
                description = "Description A",
                publishedAt = "2024-04-06",
                source = Source(id = "1", name = "Source A"),
                title = "Title A",
                url = "https://example.com/a",
                urlToImage = "https://example.com/image.jpg"
            )
        )
        val response = NewsResponse(status = "ok", totalResults = 1, articles = testArticles)

        whenever(newsRepository.getTopHeadlines()).thenReturn(response)

        // Act
        viewModel.fetchTopHeadlines()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        Assert.assertEquals(testArticles, viewModel.articles.value)
        Assert.assertFalse(viewModel.isFiltering.value)
    }

    // ---------------------------------------------------------

    // TESTING: fetchEverythingBySearch()

    @Test
    fun `fetchEverythingBySearch should fetch and post articles on success`() = runTest {
        val query = "technology"
        val testArticles = listOf(
            Article(
                author = "Author A",
                content = "Content A",
                description = "Description A",
                publishedAt = "2024-04-05",
                source = Source(id = "3", name = "Source A"),
                title = "Article A",
                url = "https://example.com/article-a",
                urlToImage = "https://example.com/image-a.jpg"
            ),
            Article(
                author = "Author B",
                content = "Content B",
                description = "Description B",
                publishedAt = "2024-04-06",
                source = Source(id = "4", name = "Source B"),
                title = "Article B",
                url = "https://example.com/article-b",
                urlToImage = "https://example.com/image-b.jpg"
            ),
        )
        val response = NewsResponse(status = "ok", totalResults = 1, articles = testArticles)

        whenever(newsRepository.getEverythingBySearch(query)).thenReturn(response)

        viewModel.fetchEverythingBySearch(query)
        testDispatcher.scheduler.advanceUntilIdle()

        Assert.assertEquals(testArticles, viewModel.articles.value)
        Assert.assertTrue(viewModel.isFiltering.value)
    }

    // ---------------------------------------------------------

    // TESTING: fetchTopHeadlinesByCategory()

    @Test
    fun `fetchTopHeadlinesByCategory should fetch and post articles on success`() = runTest {
        val category = "science"
        val testArticles = listOf(
            Article(
                author = "Author A",
                content = "Content A",
                description = "Description A",
                publishedAt = "2024-04-05",
                source = Source(id = "3", name = "Source A"),
                title = "Article A",
                url = "https://example.com/article-a",
                urlToImage = "https://example.com/image-a.jpg"
            ),
            Article(
                author = "Author B",
                content = "Content B",
                description = "Description B",
                publishedAt = "2024-04-06",
                source = Source(id = "4", name = "Source B"),
                title = "Article B",
                url = "https://example.com/article-b",
                urlToImage = "https://example.com/image-b.jpg"
            ),
        )

        val response = NewsResponse(status = "ok", totalResults = 1, articles = testArticles)

        whenever(newsRepository.getTopHeadlinesByCategory(category)).thenReturn(response)

        viewModel.fetchTopHeadlinesByCategory(category)
        testDispatcher.scheduler.advanceUntilIdle()

        Assert.assertEquals(testArticles, viewModel.articles.value)
        Assert.assertTrue(viewModel.isFiltering.value)
    }

    // ---------------------------------------------------------

    // TESTING: fetchTopHeadlinesByCountry()

    @Test
    fun `fetchTopHeadlinesByCountry should fetch and post articles on success`() = runTest {
        val country = "us"
        val testArticles = listOf(
            Article(
                author = "Author A",
                content = "Content A",
                description = "Description A",
                publishedAt = "2024-04-05",
                source = Source(id = "3", name = "Source A"),
                title = "Article A",
                url = "https://example.com/article-a",
                urlToImage = "https://example.com/image-a.jpg"
            ),
            Article(
                author = "Author B",
                content = "Content B",
                description = "Description B",
                publishedAt = "2024-04-06",
                source = Source(id = "4", name = "Source B"),
                title = "Article B",
                url = "https://example.com/article-b",
                urlToImage = "https://example.com/image-b.jpg"
            ),
        )
        val response = NewsResponse(status = "ok", totalResults = 1, articles = testArticles)

        whenever(newsRepository.getTopHeadlinesByCountry(country)).thenReturn(response)

        viewModel.fetchTopHeadlinesByCountry(country)
        testDispatcher.scheduler.advanceUntilIdle()

        Assert.assertEquals(testArticles, viewModel.articles.value)
        Assert.assertTrue(viewModel.isFiltering.value)
    }

    // ---------------------------------------------------------

    // TESTING: fetchEverythingByDateRange()

    @Test
    fun `fetchEverythingByDateRange should fetch and post articles on success`() = runTest {
        val dateRange = "2024-04-01_to_2024-04-07"
        val testArticles = listOf(
            Article(
                author = "Author A",
                content = "Content A",
                description = "Description A",
                publishedAt = "2024-04-05",
                source = Source(id = "3", name = "Source A"),
                title = "Article A",
                url = "https://example.com/article-a",
                urlToImage = "https://example.com/image-a.jpg"
            ),
            Article(
                author = "Author B",
                content = "Content B",
                description = "Description B",
                publishedAt = "2024-04-06",
                source = Source(id = "4", name = "Source B"),
                title = "Article B",
                url = "https://example.com/article-b",
                urlToImage = "https://example.com/image-b.jpg"
            ),
        )
        val response = NewsResponse(status = "ok", totalResults = 1, articles = testArticles)

        whenever(newsRepository.getEverythingByDateRange(dateRange)).thenReturn(response)

        viewModel.fetchEverythingByDateRange(dateRange)
        testDispatcher.scheduler.advanceUntilIdle()

        Assert.assertEquals(testArticles, viewModel.articles.value)
        Assert.assertTrue(viewModel.isFiltering.value)
    }

    // ---------------------------------------------------------

    // TESTING: fetchBiasBySource()

    @Test
    fun `fetchBiasForSource should return bias from repository if not in cache`() = runTest {
        // Arrange
        val sourceName = "CNN"
        val biasResult = "left"

        // Return an empty map for cached bias mappings
        val fakeBiasFlow = MutableStateFlow<Map<String, String>>(emptyMap())
        whenever(newsRepository.getAllBiasMappings()).thenReturn(fakeBiasFlow)
        whenever(newsRepository.getBiasForSource(sourceName)).thenReturn(biasResult)

        // Re-initialize the ViewModel to apply the mocked bias flow
        viewModel = NewsViewModel(newsRepository = newsRepository, logger = NoOpLogger())

        var result: String? = null

        // Act
        viewModel.fetchBiasForSource(sourceName) {
            result = it
        }

        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        Assert.assertEquals(biasResult, result)
    }

    @Test
    fun `fetchBiasForSource should return cached bias immediately if present`() = runTest {
        // Arrange
        val sourceName = "BBC"
        val cachedBias = "center"

        // Stub repository to return empty (we won't reach the fetch anyway)
        whenever(newsRepository.getAllBiasMappings()).thenReturn(MutableStateFlow(emptyMap()))
        whenever(newsRepository.getBiasForSource(sourceName)).thenReturn("ignored")

        // Init ViewModel
        viewModel = NewsViewModel(newsRepository = newsRepository, logger = NoOpLogger())

        // simulate that the cached value is already set
        viewModel._newsBiasMappings.value = mapOf(sourceName to cachedBias)

        var result: String? = null

        // Act
        viewModel.fetchBiasForSource(sourceName) {
            if (result == null) result = it // only keep first result (should be from cache)
        }

        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        Assert.assertEquals(cachedBias, result)
    }
}