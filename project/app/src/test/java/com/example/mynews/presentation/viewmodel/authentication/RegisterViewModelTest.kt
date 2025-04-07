package com.example.mynews.presentation.viewmodel.authentication

import com.example.mynews.domain.model.authentication.RegisterModel
import com.example.mynews.domain.types.RegisterInputValidationType
import com.example.mynews.domain.use_cases.ValidateRegisterInputUseCase
import com.example.mynews.utils.MainDispatcherRule
import com.example.mynews.utils.logger.Logger
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val validateRegisterInputUseCase: ValidateRegisterInputUseCase = mock()
    private val registerModel: RegisterModel = mock()
    private val logger: Logger = mock()

    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        viewModel = RegisterViewModel(validateRegisterInputUseCase, registerModel, logger)
    }

    @Test
    fun `onEmailInputChange updates state and triggers validation`() {
        whenever(validateRegisterInputUseCase(any(), any(), any(), any()))
            .thenReturn(RegisterInputValidationType.Valid)

        viewModel.onEmailInputChange("test@email.com")

        assertEquals("test@email.com", viewModel.registerState.emailInput)
        assertTrue(viewModel.registerState.isInputValid)
        assertNull(viewModel.registerState.errorMessageInput)
    }

    @Test
    fun `onUsernameInputChange updates state and triggers validation`() {
        whenever(validateRegisterInputUseCase(any(), any(), any(), any()))
            .thenReturn(RegisterInputValidationType.Valid)

        viewModel.onUsernameInputChange("user123")

        assertEquals("user123", viewModel.registerState.usernameInput)
        assertTrue(viewModel.registerState.isInputValid)
        assertNull(viewModel.registerState.errorMessageInput)
    }

    @Test
    fun `onPasswordInputChange updates state and triggers validation`() {
        whenever(validateRegisterInputUseCase(any(), any(), any(), any()))
            .thenReturn(RegisterInputValidationType.Valid)

        viewModel.onPasswordInputChange("Password123!")

        assertEquals("Password123!", viewModel.registerState.passwordInput)
        assertTrue(viewModel.registerState.isInputValid)
    }

    @Test
    fun `onPasswordRepeatedInputChange updates state and triggers validation`() {
        whenever(validateRegisterInputUseCase(any(), any(), any(), any()))
            .thenReturn(RegisterInputValidationType.Valid)

        viewModel.onPasswordRepeatedInputChange("Password123!")

        assertEquals("Password123!", viewModel.registerState.passwordRepeatedInput)
        assertTrue(viewModel.registerState.isInputValid)
    }

    @Test
    fun `onToggleVisualTransformationPassword toggles state`() {
        val initial = viewModel.registerState.isPasswordShown
        viewModel.onToggleVisualTransformationPassword()
        assertEquals(!initial, viewModel.registerState.isPasswordShown)
    }

    @Test
    fun `onToggleVisualTransformationPasswordRepeated toggles state`() {
        val initial = viewModel.registerState.isPasswordRepeatedShown
        viewModel.onToggleVisualTransformationPasswordRepeated()
        assertEquals(!initial, viewModel.registerState.isPasswordRepeatedShown)
    }

    @Test
    fun `onRegisterClick sets success state when registration succeeds`() = runTest {
        whenever(validateRegisterInputUseCase(any(), any(), any(), any()))
            .thenReturn(RegisterInputValidationType.Valid)

        viewModel.onEmailInputChange("test@email.com")
        viewModel.onUsernameInputChange("testuser")
        viewModel.onPasswordInputChange("Password123!")
        viewModel.onPasswordRepeatedInputChange("Password123!")

        whenever(registerModel.performRegistration(any(), any(), any(), any(), any()))
            .thenReturn(true)

        viewModel.onRegisterClick()
        advanceUntilIdle()

        assertTrue(viewModel.registerState.isSuccessfullyRegistered)
        assertFalse(viewModel.registerState.isLoading)
        assertFalse(viewModel.showErrorDialog)
    }

    @Test
    fun `onRegisterClick shows error when username is taken`() = runTest {
        whenever(validateRegisterInputUseCase(any(), any(), any(), any()))
            .thenReturn(RegisterInputValidationType.Valid)

        viewModel.onEmailInputChange("taken@email.com")
        viewModel.onUsernameInputChange("takenusername")
        viewModel.onPasswordInputChange("Password123!")
        viewModel.onPasswordRepeatedInputChange("Password123!")

        // Simulate model setting username taken
        whenever(registerModel.performRegistration(
            any(), any(), any(),
            check {
                it.value = true // username taken
            },
            any()
        )).thenReturn(false)

        viewModel.onRegisterClick()
        advanceUntilIdle()

        assertTrue(viewModel.showErrorDialog)
        assertEquals("Username is already taken. Please choose another one.", viewModel.registerState.errorMessageRegister)
        assertFalse(viewModel.registerState.isSuccessfullyRegistered)
    }

    @Test
    fun `onRegisterClick shows error when email is already used`() = runTest {
        whenever(validateRegisterInputUseCase(any(), any(), any(), any()))
            .thenReturn(RegisterInputValidationType.Valid)

        viewModel.onEmailInputChange("email@used.com")
        viewModel.onUsernameInputChange("newuser")
        viewModel.onPasswordInputChange("Password123!")
        viewModel.onPasswordRepeatedInputChange("Password123!")

        whenever(registerModel.performRegistration(
            any(), any(), any(),
            any(),
            check {
                it.value = true // email already used
            }
        )).thenReturn(false)

        viewModel.onRegisterClick()
        advanceUntilIdle()

        assertTrue(viewModel.showErrorDialog)
        assertEquals("An account already exists with this email.", viewModel.registerState.errorMessageRegister)
        assertFalse(viewModel.registerState.isSuccessfullyRegistered)
    }

    @Test
    fun `onRegisterClick shows default error when registration fails generically`() = runTest {
        whenever(validateRegisterInputUseCase(any(), any(), any(), any()))
            .thenReturn(RegisterInputValidationType.Valid)

        viewModel.onEmailInputChange("test@email.com")
        viewModel.onUsernameInputChange("testuser")
        viewModel.onPasswordInputChange("Password123!")
        viewModel.onPasswordRepeatedInputChange("Password123!")

        whenever(registerModel.performRegistration(any(), any(), any(), any(), any()))
            .thenReturn(false)

        viewModel.onRegisterClick()
        advanceUntilIdle()

        assertTrue(viewModel.showErrorDialog)
        assertEquals("Could not register. Please verify your internet connection and try again.", viewModel.registerState.errorMessageRegister)
    }

    @Test
    fun `onRegisterClick shows default error when exception is thrown`() = runTest {
        whenever(validateRegisterInputUseCase(any(), any(), any(), any()))
            .thenReturn(RegisterInputValidationType.Valid)

        viewModel.onEmailInputChange("test@email.com")
        viewModel.onUsernameInputChange("testuser")
        viewModel.onPasswordInputChange("Password123!")
        viewModel.onPasswordRepeatedInputChange("Password123!")

        whenever(registerModel.performRegistration(any(), any(), any(), any(), any()))
            .thenThrow(RuntimeException("Oops"))

        viewModel.onRegisterClick()
        advanceUntilIdle()

        assertTrue(viewModel.showErrorDialog)
        assertEquals("Could not register. Please verify your internet connection and try again.", viewModel.registerState.errorMessageRegister)
    }

    @Test
    fun `checkInputValidation handles invalid email`() {
        whenever(validateRegisterInputUseCase(any(), any(), any(), any()))
            .thenReturn(RegisterInputValidationType.InvalidEmail)

        viewModel.onEmailInputChange("invalid-email")
        viewModel.onPasswordInputChange("pass")
        viewModel.onUsernameInputChange("user")
        viewModel.onPasswordRepeatedInputChange("pass")

        assertFalse(viewModel.registerState.isInputValid)
        assertEquals("Please enter a valid email", viewModel.registerState.errorMessageInput)
    }

    @Test
    fun `checkInputValidation handles empty field`() {
        whenever(validateRegisterInputUseCase(any(), any(), any(), any()))
            .thenReturn(RegisterInputValidationType.EmptyField)

        viewModel.onEmailInputChange("")
        viewModel.onPasswordInputChange("")
        viewModel.onUsernameInputChange("")
        viewModel.onPasswordRepeatedInputChange("")

        assertFalse(viewModel.registerState.isInputValid)
        assertEquals("Please fill in empty fields", viewModel.registerState.errorMessageInput)
    }
}