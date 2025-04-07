package com.example.mynews.presentation.viewmodel.social


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.mynews.domain.model.social.FriendsModel
import com.example.mynews.domain.model.user.UserModel
import com.example.mynews.domain.result.AddFriendResult
import com.example.mynews.utils.MainDispatcherRule
import com.example.mynews.utils.logger.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class FriendsViewModelTest {

    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private val userModel: UserModel = mock()
    private val friendsModel: FriendsModel = mock()
    private val logger: Logger = mock()

    private lateinit var viewModel: FriendsViewModel

    private val testUserId = "test-user"
    private val testFriendUsername = "friend123"

    @Before
    fun setup() {
        whenever(friendsModel.friends).thenReturn(MutableLiveData(emptyList()))
        whenever(friendsModel.searchQuery).thenReturn(MutableStateFlow(""))
        whenever(friendsModel.recentlyAddedFriend).thenReturn(MutableStateFlow(null))

        viewModel = FriendsViewModel(userModel, friendsModel, logger)
    }

    @Test
    fun `updateSearchQuery delegates to model`() {
        viewModel.updateSearchQuery("abc")
        verify(friendsModel).updateSearchQuery("abc")
    }

    @Test
    fun `fetchFriends calls model with valid user`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)

        viewModel.fetchFriends()
        advanceUntilIdle()

        verify(userModel).getCurrentUserId()
        verify(friendsModel).fetchFriends(testUserId)
    }

    @Test
    fun `fetchFriends logs error when user ID is null`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(null)

        viewModel.fetchFriends()
        advanceUntilIdle()

        verify(logger).e(eq("FriendsViewModel"), eq("Error: User ID is null, cannot fetch friends"))
        verify(friendsModel, never()).fetchFriends(any())
    }

    @Test
    fun `addFriend handles SelfAddAttempt result correctly`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)
        whenever(friendsModel.addFriend(any(), any())).thenReturn(AddFriendResult.SelfAddAttempt)

        viewModel.addFriend(testFriendUsername)
        advanceUntilIdle()

        assertTrue(viewModel.showErrorDialog)
        assertEquals("You cannot add yourself \n as a friend.", viewModel.errorDialogMessage)
    }

    @Test
    fun `addFriend handles AlreadyAddedFriend result correctly`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)
        whenever(friendsModel.addFriend(any(), any())).thenReturn(AddFriendResult.AlreadyAddedFriend)

        viewModel.addFriend(testFriendUsername)
        advanceUntilIdle()

        assertTrue(viewModel.showErrorDialog)
        assertEquals("$testFriendUsername is already your friend.", viewModel.errorDialogMessage)
    }

    @Test
    fun `addFriend handles UserNotFound result correctly`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)
        whenever(friendsModel.addFriend(any(), any())).thenReturn(AddFriendResult.UserNotFound)

        viewModel.addFriend(testFriendUsername)
        advanceUntilIdle()

        assertTrue(viewModel.showErrorDialog)
        assertEquals("This user does not exist. \n Please check the username.", viewModel.errorDialogMessage)
    }

    @Test
    fun `addFriend handles generic Error result correctly`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)
        whenever(friendsModel.addFriend(any(), any()))
            .thenReturn(AddFriendResult.Error("something went wrong"))

        viewModel.addFriend(testFriendUsername)
        advanceUntilIdle()

        assertTrue(viewModel.showErrorDialog)
        assertEquals("Something went wrong. \n Please try again.", viewModel.errorDialogMessage)
    }



    @Test
    fun `addFriend skips empty username`() = runTest {
        viewModel.addFriend("")
        advanceUntilIdle()

        verify(userModel, never()).getCurrentUserId()
        verify(friendsModel, never()).addFriend(any(), any())
    }

    @Test
    fun `addFriend logs error if user ID is null`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(null)

        viewModel.addFriend(testFriendUsername)
        advanceUntilIdle()

        verify(logger).e(eq("FriendsViewModel"), eq("Error: User ID is null, cannot fetch friends"))
        verify(friendsModel, never()).addFriend(any(), any())
    }

    @Test
    fun `removeFriend calls model with valid user`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)

        viewModel.removeFriend(testFriendUsername)
        advanceUntilIdle()

        verify(friendsModel).removeFriend(testUserId, testFriendUsername)
    }

    @Test
    fun `removeFriend logs error if user ID is null`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(null)

        viewModel.removeFriend(testFriendUsername)
        advanceUntilIdle()

        verify(logger).e(eq("FriendsViewModel"), eq("Error: User ID is null, cannot remove friend"))
        verify(friendsModel, never()).removeFriend(any(), any())
    }
}
