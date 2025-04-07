package com.example.mynews.presentation.viewmodel.social

import com.example.mynews.domain.entities.Reaction
import com.example.mynews.domain.model.social.SocialModel
import com.example.mynews.domain.model.user.UserModel
import com.example.mynews.utils.MainDispatcherRule
import com.example.mynews.utils.TestDataFactory
import com.example.mynews.utils.logger.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SocialViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userModel: UserModel = mock()
    private val socialModel: SocialModel = mock()
    private val logger: Logger = mock()

    private lateinit var viewModel: SocialViewModel

    private val testUserId = "test-user-id"
    private val testFriendIds = listOf("friend1", "friend2")
    private val testArticle = TestDataFactory.createIndexedArticle(500)
    private val testReaction = Reaction(
        userID = "friend1",
        article = testArticle,
        reaction = "👍",
        timestamp = System.currentTimeMillis()
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)

        whenever(socialModel.friendsMap).thenReturn(MutableStateFlow(emptyMap()))
        whenever(socialModel.reactions).thenReturn(MutableStateFlow(emptyList()))
        whenever(socialModel.searchQuery).thenReturn(MutableStateFlow(""))
        whenever(socialModel.filteredReactions).thenReturn(MutableStateFlow(emptyList()))

        viewModel = SocialViewModel(userModel, socialModel, logger)
    }

    @Test
    fun `updateSearchQuery delegates to model`() {
        viewModel.updateSearchQuery("abc")
        verify(socialModel).updateSearchQuery("abc")
    }

    @Test
    fun `fetchFriends logs error and sets loading false if user is null`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(null)

        viewModel.fetchFriends()
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        verify(logger).e(
            eq("SocialViewModel"),
            eq("Error: User ID is null, cannot fetch friends")
        )
        verify(socialModel, never()).fetchFriends(any())
    }

    @Test
    fun `fetchFriends calls model with valid user ID`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)
        val populatedFriendsMap = mapOf("friend1" to "Alice")
        whenever(socialModel.friendsMap).thenReturn(MutableStateFlow(populatedFriendsMap))

        viewModel.fetchFriends()
        advanceUntilIdle()

        verify(socialModel).fetchFriends(testUserId)
        // do not assert isLoading here because fetchFriendsReactions might be expected after
    }

    @Test
    fun `fetchFriends sets loading false if friendMap is empty`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)
        whenever(socialModel.friendsMap).thenReturn(MutableStateFlow(emptyMap()))

        viewModel.fetchFriends()
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `fetchFriends logs exception and sets errorMessage`() = runTest {
        whenever(userModel.getCurrentUserId()).thenThrow(RuntimeException("unexpected"))

        viewModel.fetchFriends()
        advanceUntilIdle()

        assertEquals("Failed to fetch friends: unexpected", viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)
        verify(logger).e(
            eq("SocialViewModel"),
            eq("Error fetching friends: unexpected"),
            any()
        )
    }

    @Test
    fun `fetchFriendsReactions updates loading and calls model`() = runTest {
        viewModel.fetchFriendsReactions(testFriendIds)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
        verify(socialModel).fetchFriendsReactions(testFriendIds)
    }

    @Test
    fun `fetchFriendsReactions logs error and sets errorMessage on exception`() = runTest {
        doThrow(RuntimeException("reactions fail")).whenever(socialModel).fetchFriendsReactions(any())

        viewModel.fetchFriendsReactions(testFriendIds)
        advanceUntilIdle()

        assertEquals("Failed to fetch friends' reactions: reactions fail", viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)
        verify(logger).e(
            eq("SocialViewModel"),
            eq("Error fetching reactions: reactions fail"),
            any()
        )
    }
}