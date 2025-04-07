package com.example.mynews.presentation.viewmodel.settings

import com.example.mynews.domain.model.settings.SettingsModel
import com.example.mynews.domain.model.user.UserModel
import com.example.mynews.domain.result.DeleteAccountResult
import com.example.mynews.utils.MainDispatcherRule
import com.example.mynews.utils.logger.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever


@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userModel: UserModel = mock()
    private val settingsModel: SettingsModel = mock()
    private val logger: Logger = mock()

    private lateinit var viewModel: SettingsViewModel

    private val testUserId = "mock-user-123"
    private val testPassword = "securePassword123"

    @Before
    fun setup() {
        whenever(settingsModel.username).thenReturn(MutableStateFlow("TestUser"))
        whenever(settingsModel.email).thenReturn(MutableStateFlow("test@email.com"))
        whenever(settingsModel.numWordsToSummarize).thenReturn(MutableStateFlow(100))

        viewModel = SettingsViewModel(userModel, settingsModel, logger)

        reset(userModel, settingsModel, logger)
    }

    @Test
    fun `fetchNumWordsToSummarize calls model with valid user`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)

        viewModel.fetchNumWordsToSummarize()
        advanceUntilIdle()

        verify(userModel).getCurrentUserId()
        verify(settingsModel).fetchNumWordsToSummarize(testUserId)
        assert(viewModel.hasLoadedNumWordsToSummarize.value)
    }

    @Test
    fun `fetchNumWordsToSummarize logs error when user is null`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(null)

        viewModel.fetchNumWordsToSummarize()
        advanceUntilIdle()

        verify(logger).e(
            eq("SettingsViewModel"),
            eq("No user logged in. User ID is null or empty")
        )
        verify(settingsModel, never()).fetchNumWordsToSummarize(any())
    }

    @Test
    fun `updateNumWordsToSummarize calls model with valid user if in range`() = runTest {
        val newNumWords = 150
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)

        viewModel.updateNumWordsToSummarize(newNumWords)
        advanceUntilIdle()

        verify(logger).d("CondensedSettings", "Updated numWordsToSummarize to $newNumWords")
        verify(settingsModel).updateNumWordsToSummarize(testUserId, newNumWords)
    }

    @Test
    fun `updateNumWordsToSummarize logs debug if out of range`() {
        val outOfRange = 300
        viewModel.updateNumWordsToSummarize(outOfRange)

        verify(logger).d("SettingsDebug", "Out of range $outOfRange")
        verifyNoInteractions(userModel, settingsModel)
    }

    @Test
    fun `updateNumWordsToSummarize logs error when user is null`() = runTest {
        val validNumWords = 100
        whenever(userModel.getCurrentUserId()).thenReturn(null)

        viewModel.updateNumWordsToSummarize(validNumWords)
        advanceUntilIdle()

        verify(logger).e(
            eq("SettingsViewModel"),
            eq("No user logged in. User ID is null or empty")
        )
        verify(settingsModel, never()).updateNumWordsToSummarize(any(), any())
    }


    @Test
    fun `fetchUsername calls model with valid user`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)

        viewModel.fetchUsername()
        advanceUntilIdle()

        verify(settingsModel).fetchUsername(testUserId)
    }

    @Test
    fun `fetchUsername logs error when user is null`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(null)

        viewModel.fetchUsername()
        advanceUntilIdle()

        verify(logger).e(
            eq("SettingsViewModel"),
            eq("No user logged in. User ID is null or empty")
        )
        verify(settingsModel, never()).fetchUsername(any())
    }

    @Test
    fun `fetchEmail calls model with valid user`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(testUserId)

        viewModel.fetchEmail()
        advanceUntilIdle()

        verify(settingsModel).fetchEmail(testUserId)
    }

    @Test
    fun `fetchEmail logs error when user is null`() = runTest {
        whenever(userModel.getCurrentUserId()).thenReturn(null)

        viewModel.fetchEmail()
        advanceUntilIdle()

        verify(logger).e(
            eq("SettingsViewModel"),
            eq("No user logged in. User ID is null or empty")
        )
        verify(settingsModel, never()).fetchEmail(any())
    }

    @Test
    fun `logout updates state and logs debug`() = runTest {
        whenever(settingsModel.logout()).thenReturn(true)

        viewModel.logout()
        advanceUntilIdle()

        assert(viewModel.logoutState.value == true)
        verify(logger).d("LogoutDebug", "Logout success: true")
    }

    @Test
    fun `deleteAccount updates states and logs debug`() = runTest {
        val expectedResult = DeleteAccountResult.Success
        whenever(settingsModel.deleteAccount(testPassword)).thenReturn(expectedResult)

        viewModel.deleteAccount(testPassword)
        advanceUntilIdle()

        assert(viewModel.isDeletingAccount.value == false)
        assert(viewModel.deleteAccountState.value == expectedResult)
        verify(logger).d("LogoutDebug", "Delete account success: $expectedResult")
    }

    @Test
    fun `resetHasLoadedNumWordsToSummarize sets value to false`() {
        // preset to true
        viewModel.resetHasLoadedNumWordsToSummarize()
        assert(!viewModel.hasLoadedNumWordsToSummarize.value)
    }

    @Test
    fun `resetLogoutState sets logoutState to null`() {
        viewModel.resetLogoutState()
        assert(viewModel.logoutState.value == null)
    }

    @Test
    fun `resetDeleteAccountState sets deleteAccountState to null`() {
        viewModel.resetDeleteAccountState()
        assert(viewModel.deleteAccountState.value == null)
    }
}
