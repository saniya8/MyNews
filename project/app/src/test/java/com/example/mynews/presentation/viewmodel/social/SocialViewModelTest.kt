package com.example.mynews.presentation.viewmodel.social

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mynews.data.api.news.Article
import com.example.mynews.data.api.news.Source
import com.example.mynews.data.logger.NoOpLogger
import com.example.mynews.domain.model.Reaction
import com.example.mynews.domain.repositories.SocialRepository
import com.example.mynews.domain.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.kotlin.eq
import org.mockito.kotlin.doAnswer

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class SocialViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var socialRepository: SocialRepository

    private lateinit var viewModel: SocialViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        viewModel = SocialViewModel(
            userRepository = userRepository,
            socialRepository = socialRepository,
            logger = NoOpLogger()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------------------------------------------------------

    // TESTING: fetchFriends()

    @Test
    fun `fetchFriends should update friendsMap and leave isLoading true if friends returned`() = runTest {
        // Arrange
        val fakeUserId = "user123"
        val fakeFriendsMap = mapOf("friend1" to "alice", "friend2" to "bob")

        whenever(userRepository.getCurrentUserId()).thenReturn(fakeUserId)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Map<String, String>) -> Unit>(1)
            callback(fakeFriendsMap)
            null
        }.whenever(socialRepository).getFriends(eq(fakeUserId), any())

        // Act
        viewModel.fetchFriends()
        advanceUntilIdle()

        // Assert
        Assert.assertEquals(fakeFriendsMap, viewModel.friendsMap.value)
        Assert.assertEquals(true, viewModel.isLoading.value) // stays true until fetchFriendsReactions runs
    }


    @Test
    fun `fetchFriends should update friendsMap, clear reactions, and set isLoading false if no friends returned`() = runTest {
        val fakeUserId = "user123"
        val emptyFriendsMap = emptyMap<String, String>()

        whenever(userRepository.getCurrentUserId()).thenReturn(fakeUserId)

        doAnswer {
            val callback = it.getArgument<(Map<String, String>) -> Unit>(1)
            callback(emptyFriendsMap)
            null
        }.whenever(socialRepository).getFriends(eq(fakeUserId), any())

        viewModel.fetchFriends()
        advanceUntilIdle()

        Assert.assertEquals(emptyFriendsMap, viewModel.friendsMap.value)
        Assert.assertEquals(emptyList<Reaction>(), viewModel.reactions.value)
        Assert.assertEquals(false, viewModel.isLoading.value)
    }

    // ---------------------------------------------------------

    // TESTING: fetchFriendsReactions()

    @Test
    fun `fetchFriendsReactions should update reactions and set isLoading to false on success`() = runTest {
        // Arrange
        val friendIds = listOf("friend1", "friend2")
        val fakeReactions = listOf(
            Reaction(
                userID = "friend1",
                article = Article(
                    author = "Author A",
                    content = "Content A",
                    description = "Description A",
                    publishedAt = "2024-04-05",
                    source = Source(id = "3", name = "Source A"),
                    title = "Article A",
                    url = "https://example.com/article-a",
                    urlToImage = "https://example.com/image-a.jpg"
                ),

                reaction = "like",
                timestamp = 0L
            ),
            Reaction(
                userID = "friend2",
                article = Article(
                    author = "Author B",
                    content = "Content B",
                    description = "Description B",
                    publishedAt = "2024-04-06",
                    source = Source(id = "4", name = "Source B"),
                    title = "Article B",
                    url = "https://example.com/article-b",
                    urlToImage = "https://example.com/image-b.jpg"
                ),
                reaction = "love",
                timestamp = 0L
            )
        )

        doAnswer { invocation ->
            val callback = invocation.getArgument<(List<Reaction>) -> Unit>(1)
            callback(fakeReactions)
            null
        }.whenever(socialRepository).getFriendsReactions(eq(friendIds), any())

        // Act
        viewModel.fetchFriendsReactions(friendIds)
        advanceUntilIdle()

        // Assert
        Assert.assertEquals(fakeReactions, viewModel.reactions.value)
        Assert.assertFalse(viewModel.isLoading.value)
        Assert.assertNull(viewModel.errorMessage.value)
    }

}