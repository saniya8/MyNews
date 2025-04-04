package com.example.mynews.presentation.viewmodel.social

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mynews.data.logger.NoOpLogger
import com.example.mynews.domain.model.Mission
import com.example.mynews.domain.repositories.FriendsRepository
import com.example.mynews.domain.repositories.GoalsRepository
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
import org.mockito.kotlin.whenever
import org.mockito.kotlin.eq
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.any as mockitoAny
import com.example.mynews.presentation.state.AddFriendState

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class FriendsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var friendsRepository: FriendsRepository

    @Mock
    private lateinit var goalsRepository: GoalsRepository

    private lateinit var viewModel: FriendsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = FriendsViewModel(
            userRepository = userRepository,
            friendsRepository = friendsRepository,
            goalsRepository = goalsRepository,
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
    fun `fetchFriends should update friends LiveData with fetched friend usernames`() = runTest {
        val fakeUserId = "user123"
        val expectedFriends = listOf("alice", "bob")

        // Stub user ID
        whenever(userRepository.getCurrentUserId()).thenReturn(fakeUserId)

        // Stub callback
        doAnswer { invocation ->
            val callback = invocation.getArgument<(List<String>) -> Unit>(1)
            callback(expectedFriends)
            null
        }.whenever(friendsRepository).getFriendUsernames(any(), any())

        // Act
        viewModel.fetchFriends()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        Assert.assertEquals(expectedFriends, viewModel.friends.value)
    }

    // ---------------------------------------------------------

    // TESTING: addFriend()

    @Test
    fun `addFriend should update recentlyAddedFriend and friends list on success`() = runTest {
        // Arrange
        val fakeUserId = "user123"
        val friendUsername = "alice"
        val normalizedFriendUsername = "alice" // trimmed + lowercased
        val mockFriendsList = listOf("alice", "bob")

        // Stub user ID
        whenever(userRepository.getCurrentUserId()).thenReturn(fakeUserId)

        // Stub addFriend to return success
        whenever(
            friendsRepository.addFriend(
                eq(fakeUserId),
                eq(friendUsername),
                mockitoAny() // properly use matcher here
            )
        ).thenReturn(AddFriendState.Success)

        // Stub getFriendUsernames to invoke callback
        doAnswer { invocation ->
            val callback = invocation.getArgument<(List<String>) -> Unit>(1)
            callback(mockFriendsList)
            null
        }.whenever(friendsRepository).getFriendUsernames(eq(fakeUserId), mockitoAny())

        // Act
        viewModel.addFriend(friendUsername)
        advanceUntilIdle()

        // Assert
        Assert.assertEquals(normalizedFriendUsername, viewModel.recentlyAddedFriend.value)
        Assert.assertEquals(mockFriendsList, viewModel.friends.value)
    }

    @Test
    fun `addFriend should show self-add error dialog when user tries to add themselves`() = runTest {
        // Arrange
        val fakeUserId = "user123"
        val friendUsername = "user123"

        whenever(userRepository.getCurrentUserId()).thenReturn(fakeUserId)

        whenever(
            friendsRepository.addFriend(
                eq(fakeUserId),
                eq(friendUsername),
                mockitoAny()
            )
        ).thenReturn(AddFriendState.SelfAddAttempt)

        // Act
        viewModel.addFriend(friendUsername)
        advanceUntilIdle()

        // Assert
        Assert.assertTrue(viewModel.showErrorDialog)
        Assert.assertEquals("You cannot add yourself \n as a friend.", viewModel.errorDialogMessage)
    }

    @Test
    fun `addFriend should show already added friend error dialog when friend already exists`() = runTest {
        // Arrange
        val fakeUserId = "user123"
        val friendUsername = "alice"

        whenever(userRepository.getCurrentUserId()).thenReturn(fakeUserId)

        whenever(
            friendsRepository.addFriend(
                eq(fakeUserId),
                eq(friendUsername),
                mockitoAny()
            )
        ).thenReturn(AddFriendState.AlreadyAddedFriend)

        // Act
        viewModel.addFriend(friendUsername)
        advanceUntilIdle()

        // Assert
        Assert.assertTrue(viewModel.showErrorDialog)
        Assert.assertEquals("alice is already your friend.", viewModel.errorDialogMessage)
    }

    @Test
    fun `addFriend should show user not found error dialog when username does not exist`() = runTest {
        // Arrange
        val fakeUserId = "user123"
        val friendUsername = "unknownUser"

        whenever(userRepository.getCurrentUserId()).thenReturn(fakeUserId)

        whenever(
            friendsRepository.addFriend(
                eq(fakeUserId),
                eq(friendUsername),
                mockitoAny()
            )
        ).thenReturn(AddFriendState.UserNotFound)

        // Act
        viewModel.addFriend(friendUsername)
        advanceUntilIdle()

        // Assert
        Assert.assertTrue(viewModel.showErrorDialog)
        Assert.assertEquals("This user does not exist. \n Please check the username.", viewModel.errorDialogMessage)
    }

    @Test
    fun `addFriend should show default error dialog on unknown error`() = runTest {
        // Arrange
        val fakeUserId = "user123"
        val friendUsername = "alice"

        whenever(userRepository.getCurrentUserId()).thenReturn(fakeUserId)

        whenever(
            friendsRepository.addFriend(
                eq(fakeUserId),
                eq(friendUsername),
                mockitoAny()
            )
        ).thenReturn(AddFriendState.Error("Something bad happened"))

        // Act
        viewModel.addFriend(friendUsername)
        advanceUntilIdle()

        // Assert
        Assert.assertTrue(viewModel.showErrorDialog)
        Assert.assertEquals("Something went wrong. \n Please try again.", viewModel.errorDialogMessage)
    }

    // ---------------------------------------------------------

    // TESTING: removeFriend()

    @Test
    fun `removeFriend should fetch updated friends and update mission on success`() = runTest {
        // Arrange
        val fakeUserId = "user123"
        val friendUsername = "alice"

        whenever(userRepository.getCurrentUserId()).thenReturn(fakeUserId)
        whenever(friendsRepository.removeFriend(eq(fakeUserId), eq(friendUsername))).thenReturn(true)

        // Stub getFriendUsernames
        val updatedFriendsList = listOf("bob", "charlie")
        doAnswer {
            val callback = it.getArgument<(List<String>) -> Unit>(1)
            callback(updatedFriendsList)
            null
        }.whenever(friendsRepository).getFriendUsernames(eq(fakeUserId), any())

        // Stub mission stuff
        whenever(friendsRepository.getFriendCount(fakeUserId)).thenReturn(2)
        whenever(goalsRepository.getMissions(fakeUserId)).thenReturn(emptyList()) // no missions to update

        // Act
        viewModel.removeFriend(friendUsername)
        advanceUntilIdle()

        // Assert
        Assert.assertEquals(updatedFriendsList, viewModel.friends.value)
    }

    @Test
    fun `removeFriend should not update state when repository returns false`() = runTest {
        // Arrange
        val fakeUserId = "user123"
        val friendUsername = "alice"

        whenever(userRepository.getCurrentUserId()).thenReturn(fakeUserId)
        whenever(friendsRepository.removeFriend(eq(fakeUserId), eq(friendUsername))).thenReturn(false)

        // Act
        viewModel.removeFriend(friendUsername)
        advanceUntilIdle()

        // Assert
        // friends list should remain null or unchanged
        Assert.assertNull(viewModel.friends.value)
    }


    // ---------------------------------------------------------

    // TESTING: updateAddFriendsMission()

    @Test
    fun `updateAddFriendsMission should complete mission when friend count meets target`() = runTest {
        // Arrange
        val userId = "user123"
        val mission = Mission(
            id = "m1",
            name = "Add 5 Friends",
            description = "Add 5 friends to complete this mission.",
            targetCount = 5,
            currentCount = 3,
            isCompleted = false,
            type = "add_friend"
        )

        whenever(friendsRepository.getFriendCount(userId)).thenReturn(5)
        whenever(goalsRepository.getMissions(userId)).thenReturn(listOf(mission))

        // Act
        viewModel.updateAddFriendsMission(userId)

        // Assert
        verify(goalsRepository).updateMissionProgress(userId, mission.id, 5)
        verify(goalsRepository).markMissionComplete(userId, mission.id)
    }

    @Test
    fun `updateAddFriendsMission should update progress only when target not yet met`() = runTest {
        // Arrange
        val userId = "user123"
        val mission = Mission(
            id = "m2",
            name = "Add 10 Friends",
            description = "Add 10 friends to complete this mission.",
            targetCount = 10,
            currentCount = 2,
            isCompleted = false,
            type = "add_friend"
        )

        whenever(friendsRepository.getFriendCount(userId)).thenReturn(5)
        whenever(goalsRepository.getMissions(userId)).thenReturn(listOf(mission))

        // Act
        viewModel.updateAddFriendsMission(userId)

        // Assert
        verify(goalsRepository).updateMissionProgress(userId, mission.id, 5)
        verify(goalsRepository, never()).markMissionComplete(any(), any())
    }

    @Test
    fun `updateAddFriendsMission should skip already completed missions`() = runTest {
        // Arrange
        val userId = "user123"
        val mission = Mission(
            id = "m3",
            name = "Add Friends",
            description = "This mission is already done.",
            targetCount = 5,
            currentCount = 5,
            isCompleted = true,
            type = "add_friend"
        )

        whenever(friendsRepository.getFriendCount(userId)).thenReturn(5)
        whenever(goalsRepository.getMissions(userId)).thenReturn(listOf(mission))

        // Act
        viewModel.updateAddFriendsMission(userId)

        // Assert
        verify(goalsRepository, never()).updateMissionProgress(any(), any(), any())
        verify(goalsRepository, never()).markMissionComplete(any(), any())
    }

}