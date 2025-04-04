package com.example.mynews.presentation.viewmodel.authentication

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.runtime.MutableState
import com.example.mynews.data.logger.NoOpLogger
import com.example.mynews.domain.model.RegisterInputValidationType
import com.example.mynews.domain.repositories.AuthRepository
import com.example.mynews.domain.use_cases.ValidateRegisterInputUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

// note: validating register inputs is tested in (test) com.example.mynews/domain/use_cases/ValidateRegisterInputUseCaseTest


@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class RegisterViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var authRepository: AuthRepository

    @Mock
    private lateinit var validateRegisterInputUseCase: ValidateRegisterInputUseCase

    private lateinit var viewModel: RegisterViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = RegisterViewModel(
            validateRegisterInputUseCase = validateRegisterInputUseCase,
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
    fun `onEmailInputChange should update email and trigger validation`() {
        val email = "test@example.com"

        whenever(validateRegisterInputUseCase(email, "", "", ""))
            .thenReturn(RegisterInputValidationType.EmptyField)

        viewModel.onEmailInputChange(email)

        val state = viewModel.registerState
        Assert.assertEquals(email, state.emailInput)
    }

    // ---------------------------------------------------------

    // TESTING: onUsernameInputChange()

    @Test
    fun `onUsernameInputChange should update username and trigger validation`() {
        val username = "my_user"

        whenever(validateRegisterInputUseCase("", username, "", ""))
            .thenReturn(RegisterInputValidationType.EmptyField)

        viewModel.onUsernameInputChange(username)

        val state = viewModel.registerState
        Assert.assertEquals(username, state.usernameInput)
    }


    // ---------------------------------------------------------

    // TESTING: onPasswordInputChange()

    @Test
    fun `onPasswordInputChange should update password and trigger validation`() {
        val password = "Password1!"

        whenever(validateRegisterInputUseCase("", "", password, ""))
            .thenReturn(RegisterInputValidationType.EmptyField)

        viewModel.onPasswordInputChange(password)

        val state = viewModel.registerState
        Assert.assertEquals(password, state.passwordInput)
    }


    // ---------------------------------------------------------

    // TESTING: onPasswordRepeatedInputChange()

    @Test
    fun `onPasswordRepeatedInputChange should update passwordRepeated and trigger validation`() {
        val repeated = "Password1!"

        whenever(validateRegisterInputUseCase("", "", "", repeated))
            .thenReturn(RegisterInputValidationType.EmptyField)

        viewModel.onPasswordRepeatedInputChange(repeated)

        val state = viewModel.registerState
        Assert.assertEquals(repeated, state.passwordRepeatedInput)
    }

    // ---------------------------------------------------------

    // TESTING: onRegisterClick()

    @Test
    fun `onRegisterClick with valid input and register success should update state to success`() = runTest {
        val email = "test@example.com"
        val username = "my_username"
        val password = "Password123!"

        // stub validation to return VALID for any combo (prevents early null return)
        whenever(validateRegisterInputUseCase(any(), any(), any(), any()))
            .thenReturn(RegisterInputValidationType.Valid)

        // stub successful register result
        whenever(authRepository.register(any(), any(), any(), any(), any()))
            .thenReturn(true)

        // now set inputs
        viewModel.onEmailInputChange(email)
        viewModel.onUsernameInputChange(username)
        viewModel.onPasswordInputChange(password)
        viewModel.onPasswordRepeatedInputChange(password)

        // Act
        viewModel.onRegisterClick()
        advanceUntilIdle()

        // Assert
        val state = viewModel.registerState
        Assert.assertTrue(state.isSuccessfullyRegistered)
        Assert.assertFalse(state.isLoading)
        Assert.assertNull(state.errorMessageRegister)
        Assert.assertFalse(viewModel.showErrorDialog)
    }

    @Test
    fun `onRegisterClick with username taken should show username taken error`() = runTest {
        val email = "test@example.com"
        val username = "taken_username"
        val password = "Password123!"

        whenever(validateRegisterInputUseCase(any(), any(), any(), any()))
            .thenReturn(RegisterInputValidationType.Valid)

        whenever(authRepository.register(any(), any(), any(), any(), any()))
            .thenAnswer {
                val isUsernameTaken = it.getArgument<MutableState<Boolean>>(3)
                isUsernameTaken.value = true
                false
            }

        viewModel.onEmailInputChange(email)
        viewModel.onUsernameInputChange(username)
        viewModel.onPasswordInputChange(password)
        viewModel.onPasswordRepeatedInputChange(password)

        viewModel.onRegisterClick()
        advanceUntilIdle()

        val state = viewModel.registerState
        Assert.assertFalse(state.isSuccessfullyRegistered)
        Assert.assertFalse(state.isLoading)
        Assert.assertEquals("Username is already taken. Please choose another one.", state.errorMessageRegister)
        Assert.assertTrue(viewModel.showErrorDialog)
    }

    @Test
    fun `onRegisterClick with email already used should show email used error`() = runTest {
        val email = "used@example.com"
        val username = "unique_username"
        val password = "Password123!"

        whenever(validateRegisterInputUseCase(any(), any(), any(), any()))
            .thenReturn(RegisterInputValidationType.Valid)

        whenever(authRepository.register(any(), any(), any(), any(), any()))
            .thenAnswer {
                val isEmailUsed = it.getArgument<MutableState<Boolean>>(4)
                isEmailUsed.value = true
                false
            }

        viewModel.onEmailInputChange(email)
        viewModel.onUsernameInputChange(username)
        viewModel.onPasswordInputChange(password)
        viewModel.onPasswordRepeatedInputChange(password)

        viewModel.onRegisterClick()
        advanceUntilIdle()

        val state = viewModel.registerState
        Assert.assertFalse(state.isSuccessfullyRegistered)
        Assert.assertFalse(state.isLoading)
        Assert.assertEquals("An account already exists with this email.", state.errorMessageRegister)
        Assert.assertTrue(viewModel.showErrorDialog)
    }

    @Test
    fun `onRegisterClick with failure and no specific reason should show default error`() = runTest {
        val email = "test@example.com"
        val username = "valid_username"
        val password = "Password123!"

        whenever(validateRegisterInputUseCase(any(), any(), any(), any()))
            .thenReturn(RegisterInputValidationType.Valid)

        whenever(authRepository.register(any(), any(), any(), any(), any()))
            .thenReturn(false) // No flags updated

        viewModel.onEmailInputChange(email)
        viewModel.onUsernameInputChange(username)
        viewModel.onPasswordInputChange(password)
        viewModel.onPasswordRepeatedInputChange(password)

        viewModel.onRegisterClick()
        advanceUntilIdle()

        val state = viewModel.registerState
        Assert.assertFalse(state.isSuccessfullyRegistered)
        Assert.assertFalse(state.isLoading)
        Assert.assertEquals("Could not register. Please verify your internet connection and try again.", state.errorMessageRegister)
        Assert.assertTrue(viewModel.showErrorDialog)
    }

}