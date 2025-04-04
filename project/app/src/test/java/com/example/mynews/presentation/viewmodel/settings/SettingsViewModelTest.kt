package com.example.mynews.presentation.viewmodel.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mynews.data.logger.NoOpLogger
import com.example.mynews.domain.repositories.AuthRepository
import com.example.mynews.domain.model.User
import com.example.mynews.domain.repositories.SettingsRepository
import com.example.mynews.domain.repositories.UserRepository
import com.example.mynews.presentation.state.DeleteAccountResult
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class SettingsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var authRepository: AuthRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var settingsRepository: SettingsRepository

    private lateinit var viewModel: SettingsViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        viewModel = SettingsViewModel(
            authRepository = authRepository,
            userRepository = userRepository,
            settingsRepository = settingsRepository,
            logger = NoOpLogger(),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------------------------------------------------------

    // TESTING: fetchNumWordsToSummarize()

    @Test
    fun `fetchNumWordsToSummarize should update state with value from repository`() = runTest {
        // Arrange
        val fakeUserId = "user123"
        val fakeNumWords = 75

        whenever(userRepository.getCurrentUserId()).thenReturn(fakeUserId)
        whenever(settingsRepository.getNumWordsToSummarize(fakeUserId)).thenReturn(fakeNumWords)

        // Act
        viewModel.fetchNumWordsToSummarize()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        Assert.assertEquals(fakeNumWords, viewModel.numWordsToSummarize.value)
        Assert.assertTrue(viewModel.hasLoadedNumWordsToSummarize.value)
    }

    // ---------------------------------------------------------

    // TESTING: updateNumWordsToSummarize()

    @Test
    fun `updateNumWordsToSummarize should update value and call repository when input is valid`() = runTest {
        // Arrange
        val initialValue = viewModel.numWordsToSummarize.value
        val validNewValue = 150
        val fakeUserId = "user123"

        whenever(userRepository.getCurrentUserId()).thenReturn(fakeUserId)

        // Act
        viewModel.updateNumWordsToSummarize(validNewValue)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        Assert.assertEquals(validNewValue, viewModel.numWordsToSummarize.value)
        verify(settingsRepository).updateNumWordsToSummarize(fakeUserId, validNewValue)
    }

    @Test
    fun `updateNumWordsToSummarize should not update value when input is out of range`() = runTest {
        // Arrange
        val initialValue = viewModel.numWordsToSummarize.value
        val invalidNewValue = 30 // out of range (below 50)

        // Act
        viewModel.updateNumWordsToSummarize(invalidNewValue)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        Assert.assertEquals(initialValue, viewModel.numWordsToSummarize.value)
        verify(settingsRepository, never()).updateNumWordsToSummarize(any(), any())
    }

    // ---------------------------------------------------------

    // TESTING: fetchUsername()

    @Test
    fun `fetchUsername should set username from userRepository`() = runTest {
        // Arrange
        val fakeUserId = "user123"
        val fakeEmail = "user123@gmailcom"
        val fakeUsername = "TestUser"

        whenever(userRepository.getCurrentUserId()).thenReturn(fakeUserId)
        whenever(userRepository.getUserById(fakeUserId)).thenReturn(
            User(email = fakeEmail,
                 uid = fakeUserId,
                 username = fakeUsername,
                 loggedIn = true)
            )

        // Act
        viewModel.fetchUsername()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        Assert.assertEquals(fakeUsername, viewModel.username.value)
    }

    // ---------------------------------------------------------

    // TESTING: fetchEmail()

    @Test
    fun `fetchEmail should set email from userRepository`() = runTest {
        // Arrange
        val fakeUserId = "user123"
        val fakeEmail = "user123@gmailcom"
        val fakeUsername = "TestUser"

        whenever(userRepository.getCurrentUserId()).thenReturn(fakeUserId)
        whenever(userRepository.getUserById(fakeUserId)).thenReturn(
            User(email = fakeEmail,
                uid = fakeUserId,
                username = fakeUsername,
                loggedIn = true)
        )

        // Act
        viewModel.fetchEmail()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        Assert.assertEquals(fakeEmail, viewModel.email.value)
    }

    // ---------------------------------------------------------

    // TESTING: logout()

    @Test
    fun `logout should update logoutState to true on success`() = runTest {
        // Arrange
        whenever(authRepository.logout()).thenReturn(true)

        // Act
        viewModel.logout()
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        Assert.assertEquals(true, viewModel.logoutState.value)
    }


    // ---------------------------------------------------------

    // TESTING: deleteAccount()

    @Test
    fun `deleteAccount should update deleteAccountState to Success and toggle isDeletingAccount`() = runTest {
        // Arrange
        val password = "testPassword"
        whenever(authRepository.deleteAccount(password)).thenReturn(DeleteAccountResult.Success)

        // Act
        viewModel.deleteAccount(password)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        Assert.assertEquals(DeleteAccountResult.Success, viewModel.deleteAccountState.value)
        Assert.assertEquals(false, viewModel.isDeletingAccount.value)
    }

    @Test
    fun `deleteAccount should update deleteAccountState to IncorrectPassword`() = runTest {
        val password = "wrongPassword"
        whenever(authRepository.deleteAccount(password)).thenReturn(DeleteAccountResult.IncorrectPassword)

        viewModel.deleteAccount(password)
        testDispatcher.scheduler.advanceUntilIdle()

        Assert.assertEquals(DeleteAccountResult.IncorrectPassword, viewModel.deleteAccountState.value)
    }

    @Test
    fun `deleteAccount should update deleteAccountState to Error`() = runTest {
        val password = "anyPassword"
        whenever(authRepository.deleteAccount(password)).thenReturn(DeleteAccountResult.Error)

        viewModel.deleteAccount(password)
        testDispatcher.scheduler.advanceUntilIdle()

        Assert.assertEquals(DeleteAccountResult.Error, viewModel.deleteAccountState.value)
    }

}