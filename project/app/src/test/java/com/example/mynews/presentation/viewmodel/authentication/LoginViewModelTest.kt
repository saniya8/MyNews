package com.example.mynews.presentation.viewmodel.authentication

import com.example.mynews.domain.model.authentication.LoginModel
import com.example.mynews.domain.types.LoginInputValidationType
import com.example.mynews.domain.use_cases.ValidateLoginInputUseCase
import com.example.mynews.utils.MainDispatcherRule
import com.example.mynews.utils.logger.Logger
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val validateLoginInputUseCase: ValidateLoginInputUseCase = mock()
    private val loginModel: LoginModel = mock()
    private val logger: Logger = mock()

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = LoginViewModel(validateLoginInputUseCase, loginModel, logger)
    }

    @Test
    fun `onEmailInputChange updates state and triggers validation`() {
        whenever(validateLoginInputUseCase(any(), any())).thenReturn(LoginInputValidationType.Valid)

        viewModel.onEmailInputChange("test@email.com")

        assertEquals("test@email.com", viewModel.loginState.emailInput)
        assertNull(viewModel.loginState.errorMessageInput)
        assertTrue(viewModel.loginState.isInputValid)
    }

    @Test
    fun `onPasswordInputChange updates state and triggers validation`() {
        whenever(validateLoginInputUseCase(any(), any())).thenReturn(LoginInputValidationType.Valid)

        viewModel.onPasswordInputChange("password123")

        assertEquals("password123", viewModel.loginState.passwordInput)
        assertNull(viewModel.loginState.errorMessageInput)
        assertTrue(viewModel.loginState.isInputValid)
    }

    @Test
    fun `onToggleVisualTransformation toggles isPasswordShown`() {
        val initialState = viewModel.loginState.isPasswordShown
        viewModel.onToggleVisualTransformation()
        assertEquals(!initialState, viewModel.loginState.isPasswordShown)
    }

    @Test
    fun `onLoginClick sets success state on valid login`() = runTest {
        // stub validation so valid result is returned
        whenever(validateLoginInputUseCase(any(), any()))
            .thenReturn(LoginInputValidationType.Valid)

        viewModel.onEmailInputChange("test@email.com")
        viewModel.onPasswordInputChange("pass123")

        // stub login success
        whenever(loginModel.performLogin(any(), any())).thenReturn(true)

        viewModel.onLoginClick()
        advanceUntilIdle()

        assertTrue(viewModel.loginState.isSuccessfullyLoggedIn)
        assertFalse(viewModel.loginState.isLoading)
        assertFalse(viewModel.showErrorDialog)
    }


    @Test
    fun `onLoginClick shows error dialog when login fails`() = runTest {
        // stub validation so valid result returned
        whenever(validateLoginInputUseCase(any(), any()))
            .thenReturn(LoginInputValidationType.Valid)

        viewModel.onEmailInputChange("test@email.com")
        viewModel.onPasswordInputChange("pass123")

        // stub login failure
        whenever(loginModel.performLogin(any(), any())).thenReturn(false)

        viewModel.onLoginClick()
        advanceUntilIdle()

        assertTrue(viewModel.showErrorDialog)
        assertEquals("Could not login", viewModel.loginState.errorMessageLogin)
        assertFalse(viewModel.loginState.isSuccessfullyLoggedIn)
        assertFalse(viewModel.loginState.isLoading)
    }

    @Test
    fun `onLoginClick shows error dialog when login throws exception`() = runTest {
        // stub validation so valid result retuned
        whenever(validateLoginInputUseCase(any(), any()))
            .thenReturn(LoginInputValidationType.Valid)

        viewModel.onEmailInputChange("test@email.com")
        viewModel.onPasswordInputChange("pass123")

        // simulate login crash
        whenever(loginModel.performLogin(any(), any()))
            .thenThrow(RuntimeException("Oops"))

        viewModel.onLoginClick()
        advanceUntilIdle()

        assertTrue(viewModel.showErrorDialog)
        assertEquals("Could not login", viewModel.loginState.errorMessageLogin)
        assertFalse(viewModel.loginState.isSuccessfullyLoggedIn)
        assertFalse(viewModel.loginState.isLoading)
    }

    @Test
    fun `checkInputValidation sets error for empty fields`() {
        whenever(validateLoginInputUseCase(any(), any())).thenReturn(LoginInputValidationType.EmptyField)

        viewModel.onEmailInputChange("")
        viewModel.onPasswordInputChange("")

        assertFalse(viewModel.loginState.isInputValid)
        assertEquals("Please fill in empty fields", viewModel.loginState.errorMessageInput)
    }

    @Test
    fun `checkInputValidation sets error for invalid email`() {
        whenever(validateLoginInputUseCase(any(), any())).thenReturn(LoginInputValidationType.InvalidEmail)

        viewModel.onEmailInputChange("invalid-email")
        viewModel.onPasswordInputChange("password123")

        assertFalse(viewModel.loginState.isInputValid)
        assertEquals("Please enter a valid email", viewModel.loginState.errorMessageInput)
    }

    @Test
    fun `checkInputValidation sets valid state when input is valid`() {
        whenever(validateLoginInputUseCase(any(), any())).thenReturn(LoginInputValidationType.Valid)

        viewModel.onEmailInputChange("valid@email.com")
        viewModel.onPasswordInputChange("password123")

        assertTrue(viewModel.loginState.isInputValid)
        assertNull(viewModel.loginState.errorMessageInput)
    }
}