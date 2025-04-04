package com.example.mynews.presentation.viewmodel.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mynews.data.api.news.Article
import com.example.mynews.data.api.news.Source
import com.example.mynews.data.logger.NoOpLogger
import com.example.mynews.domain.repositories.SavedArticlesRepository
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
class SavedArticlesViewModelTest {

    // allows LiveData to work properly in tests
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var savedArticlesRepository: SavedArticlesRepository // create mock of savedArticlesRepository

    @Mock
    private lateinit var userRepository: UserRepository // create mock of UserRepository

    private lateinit var viewModel: SavedArticlesViewModel // declare viewmodel

    private val testDispatcher = StandardTestDispatcher() // coroutine dispatcher (to control coroutine execution in tests)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher) // test dispatcher
        viewModel = SavedArticlesViewModel( // initialize viewmodel
            userRepository = userRepository, // pass mock of userRepository
            savedArticlesRepository = savedArticlesRepository, // pass mock of savedArticlesRepository
            logger = NoOpLogger()) // prevents log errors in unit tests
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset dispatcher to original state for cleanup
    }

    // ---------------------------------------------------------

    // TESTING: saveArticle()

    @Test
    fun `saveArticle should call repository with correct userID and article`() = runTest { // runTest provides coroutine scope

        // step 1: Arrange


        val fakeUserId = "user123"
        val fakeArticle = Article(
            author = "John Doe",
            content = "Save test article",
            description = "Save test description",
            publishedAt = "2024-04-03",
            source = Source(id = "1", name = "Save Source"),
            title = "Save Me",
            url = "https://example.com/save-article",
            urlToImage = "https://example.com/image1.jpg"
        )

        // mock user ID
        `when`(userRepository.getCurrentUserId()).thenReturn(fakeUserId)

        // mock saveArticle result
        `when`(savedArticlesRepository.saveArticle(fakeUserId, fakeArticle)).thenReturn(true) // fake behaviour to say succeeded

        // step 2: Act
        viewModel.saveArticle(fakeArticle) // action being tested - saving an article

        // advance coroutine execution
        testDispatcher.scheduler.advanceUntilIdle() // wait for all coroutines to finish (simulate time passing)

        // step 3: Assert
        verify(savedArticlesRepository).saveArticle(fakeUserId, fakeArticle) // confirming that viewmodel actually called repository with correct parameters
    }

    // ---------------------------------------------------------

    // TESTING: deleteSavedArticle()

    @Test
    fun `deleteSavedArticle should call repository with correct userID and article`() = runTest {

        // step 1: Arrange

        val fakeUserId = "user456"
        val fakeArticle = Article(
            author = "John Doe",
            content = "Delete test article",
            description = "Delete test description",
            publishedAt = "2024-04-04",
            source = Source(id = "2", name = "Delete Source"),
            title = "Delete Me",
            url = "https://example.com/delete-article",
            urlToImage = "https://example.com/image2.jpg"
        )

        // mock user ID
        `when`(userRepository.getCurrentUserId()).thenReturn(fakeUserId)

        // mock deleteArticle result
        `when`(savedArticlesRepository.deleteSavedArticle(fakeUserId, fakeArticle)).thenReturn(true)

        // step 2: Act
        viewModel.deleteSavedArticle(fakeArticle)

        // advance coroutine execution
        testDispatcher.scheduler.advanceUntilIdle()

        // step 3: Assert
        verify(savedArticlesRepository).deleteSavedArticle(fakeUserId, fakeArticle)
    }

    // ---------------------------------------------------------

    // TESTING: fetchSavedArticles()

    @Test
    fun `fetchSavedArticles should update LiveData with articles from repository`() = runTest {
        val fakeUserId = "user789"
        val fakeArticleList = listOf(
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
            Article(
                author = "Author C",
                content = "Content C",
                description = "Description C",
                publishedAt = "2024-04-07",
                source = Source(id = "5", name = "Source C"),
                title = "Article C",
                url = "https://example.com/article-c",
                urlToImage = "https://example.com/image-c.jpg"
            )
        )

        `when`(userRepository.getCurrentUserId()).thenReturn(fakeUserId)

        doAnswer { invocation ->
            val userIdArg = invocation.getArgument<String>(0)
            val callback = invocation.getArgument<(List<Article>) -> Unit>(1)
            Assert.assertEquals(fakeUserId, userIdArg)
            callback(fakeArticleList)
            null
        }.`when`(savedArticlesRepository).getSavedArticles(any(), any())

        viewModel.fetchSavedArticles()
        testDispatcher.scheduler.advanceUntilIdle()

        val result = viewModel.savedArticles.value
        Assert.assertEquals(fakeArticleList, result)
    }



}