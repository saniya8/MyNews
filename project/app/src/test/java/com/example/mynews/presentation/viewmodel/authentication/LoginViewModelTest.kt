package com.example.mynews.presentation.viewmodel.authentication

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.mynews.utils.logger.NoOpLogger
import com.example.mynews.domain.entities.LoginInputValidationType
import com.example.mynews.domain.repositories.AuthRepository
import com.example.mynews.domain.use_cases.ValidateLoginInputUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

// note: validating login inputs is tested in (test) com.example.mynews/domain/use_cases/ValidateLoginInputUseCaseTest

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var authRepository: AuthRepository

    @Mock
    private lateinit var validateLoginInputUseCase: ValidateLoginInputUseCase

    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(
            validateLoginInputUseCase = validateLoginInputUseCase,
            authRepository = authRepository,
            logger = NoOpLogger()
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------------------------------------------------------

    // TESTING: onEmailInputChange()


    @Test
    fun `onEmailInputChange should update email and validate input`() = runTest {
        val testEmail = "test@example.com"
        val currentPassword = viewModel.loginState.passwordInput

        whenever(validateLoginInputUseCase(testEmail, currentPassword))
            .thenReturn(LoginInputValidationType.Valid)

        viewModel.onEmailInputChange(testEmail)

        Assert.assertEquals(testEmail, viewModel.loginState.emailInput)
        Assert.assertTrue(viewModel.loginState.isInputValid)
        Assert.assertNull(viewModel.loginState.errorMessageInput)
    }

    // ---------------------------------------------------------

    // TESTING: onPasswordInputChange()

    @Test
    fun `onPasswordInputChange should update password and validate input`() = runTest {
        val testPassword = "Password123!"
        val currentEmail = viewModel.loginState.emailInput

        whenever(validateLoginInputUseCase(currentEmail, testPassword))
            .thenReturn(LoginInputValidationType.Valid)

        viewModel.onPasswordInputChange(testPassword)

        Assert.assertEquals(testPassword, viewModel.loginState.passwordInput)
        Assert.assertTrue(viewModel.loginState.isInputValid)
        Assert.assertNull(viewModel.loginState.errorMessageInput)
    }

    // ---------------------------------------------------------

    // TESTING: onLoginClick()

    @Test
    fun `onLoginClick with valid input and success should update state to success`() = runTest {
        val email = "test@example.com"
        val password = "Password123!"

        // Stub ALL validation input combos
        whenever(validateLoginInputUseCase(any(), any()))
            .thenReturn(LoginInputValidationType.Valid)

        // Stub login result
        whenever(authRepository.login(email, password)).thenReturn(true)

        // Set inputs
        viewModel.onEmailInputChange(email)
        viewModel.onPasswordInputChange(password)

        // Act
        viewModel.onLoginClick()
        advanceUntilIdle()

        // Assert
        val state = viewModel.loginState
        Assert.assertTrue(state.isSuccessfullyLoggedIn)
        Assert.assertFalse(state.isLoading)
        Assert.assertNull(state.errorMessageLogin)
        Assert.assertFalse(viewModel.showErrorDialog)
    }

    @Test
    fun `onLoginClick with valid input and failed login should show error`() = runTest {
        val email = "test@example.com"
        val password = "Password123!"

        // Stub validation to always return valid for any input
        whenever(validateLoginInputUseCase(any(), any()))
            .thenReturn(LoginInputValidationType.Valid)

        // Stub login result to simulate failure
        whenever(authRepository.login(email, password)).thenReturn(false)

        // Set inputs
        viewModel.onEmailInputChange(email)
        viewModel.onPasswordInputChange(password)

        // Act
        viewModel.onLoginClick()
        advanceUntilIdle()

        // Assert
        val state = viewModel.loginState
        Assert.assertFalse(state.isSuccessfullyLoggedIn)
        Assert.assertFalse(state.isLoading)
        Assert.assertEquals("Could not login", state.errorMessageLogin)
        Assert.assertTrue(viewModel.showErrorDialog)
    }
}