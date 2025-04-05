package com.example.mynews.presentation.viewmodel.authentication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.utils.logger.Logger
import com.example.mynews.domain.entities.RegisterInputValidationType
import com.example.mynews.domain.repositories.AuthRepository
import com.example.mynews.domain.use_cases.ValidateRegisterInputUseCase
import com.example.mynews.presentation.state.RegisterState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val validateRegisterInputUseCase: ValidateRegisterInputUseCase,
    private val authRepository: AuthRepository,
    private val logger: Logger,
): ViewModel() {
    var showErrorDialog by mutableStateOf(false)

    var registerState by mutableStateOf(RegisterState())
        private set

    private val _isUsernameTaken = mutableStateOf(false)
    private val _isEmailAlreadyUsed = mutableStateOf(false)

    private var _isUserNameTakenErrorMessage = "Username is already taken. Please choose another one."
    private var _isEmailAlreadyUsedErrorMessage = "An account already exists with this email."
    private var _defaultErrorMessage = "Could not register. Please verify your internet connection and try again."


    fun onEmailInputChange(newValue: String){
        registerState = registerState.copy(emailInput = newValue)
        checkInputValidation()
    }

    fun onUsernameInputChange(newValue: String){
        registerState = registerState.copy(usernameInput = newValue)
        checkInputValidation()
    }

    fun onPasswordInputChange(newValue: String){
        registerState = registerState.copy(passwordInput = newValue)
        checkInputValidation()
    }

    fun onPasswordRepeatedInputChange(newValue: String){
        registerState = registerState.copy(passwordRepeatedInput = newValue)
        checkInputValidation()
    }

    fun onToggleVisualTransformationPassword(){
        registerState = registerState.copy(isPasswordShown = !registerState.isPasswordShown)
    }

    fun onToggleVisualTransformationPasswordRepeated(){
        registerState = registerState.copy(
            isPasswordRepeatedShown = !registerState.isPasswordRepeatedShown
        )
    }

    // version 2 - works with version 2 of register in AuthRepositoryImpl
    fun onRegisterClick() {
        viewModelScope.launch {
            registerState = registerState.copy(isLoading = true)

            // reset error states before registering
            _isUsernameTaken.value = false
            _isEmailAlreadyUsed.value = false

            try {
                val registerResult = authRepository.register(
                    email = registerState.emailInput, // normalized in authRepository.register
                    username = registerState.usernameInput, // normalized in authRepository.register
                    password = registerState.passwordInput,
                    isUsernameTaken = _isUsernameTaken,
                    isEmailAlreadyUsed = _isEmailAlreadyUsed,
                )

                if (registerResult) {
                    registerState = registerState.copy(
                        isSuccessfullyRegistered = true,
                        isLoading = false
                    )
                } else { // register result returned false
                    showErrorDialog = true
                    registerState = registerState.copy(
                        errorMessageRegister = when {
                            _isUsernameTaken.value -> _isUserNameTakenErrorMessage
                            _isEmailAlreadyUsed.value -> _isEmailAlreadyUsedErrorMessage
                            else -> _defaultErrorMessage
                        },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                showErrorDialog = true
                registerState = registerState.copy(
                    errorMessageRegister = _defaultErrorMessage,
                    isLoading = false
                )
            }
        }
    }

    private fun checkInputValidation(){
        val validationResult = validateRegisterInputUseCase(
            registerState.emailInput,
            registerState.usernameInput,
            registerState.passwordInput,
            registerState.passwordRepeatedInput
        )
        processInputValidationType(validationResult)
    }


    private fun processInputValidationType(type: RegisterInputValidationType){
        registerState = when(type){

            RegisterInputValidationType.EmptyField -> {
                registerState.copy(errorMessageInput = "Please fill in empty fields", isInputValid = false)
            }

            RegisterInputValidationType.InvalidEmail -> {
                registerState.copy(errorMessageInput = "Please enter a valid email", isInputValid = false)
            }

            RegisterInputValidationType.InvalidUsernameCharacters -> {
                registerState.copy(errorMessageInput = "Username can only include letters, numbers, periods, or underscores", isInputValid = false)
            }

            RegisterInputValidationType.UsernameTooLong -> {
                registerState.copy(errorMessageInput = "Username too long, can only be up to 20 characters", isInputValid = false)
            }

            RegisterInputValidationType.UsernameTooShort -> {
                registerState.copy(errorMessageInput = "Username too short, must be at least 3 characters", isInputValid = false)
            }

            RegisterInputValidationType.PasswordTooShort -> {
                registerState.copy(errorMessageInput = "Password must be at least 8 characters", isInputValid = false)
            }

            RegisterInputValidationType.PasswordsDoNotMatch -> {
                registerState.copy(errorMessageInput = "Passwords do not match", isInputValid = false)
            }

            RegisterInputValidationType.PasswordUpperCaseMissing -> {
                registerState.copy(errorMessageInput = "Password needs to contain at least one upper case character", isInputValid = false)
            }

            RegisterInputValidationType.PasswordNumberMissing -> {
                registerState.copy(errorMessageInput = "Password needs to contain at least one number", isInputValid = false)
            }

            RegisterInputValidationType.PasswordSpecialCharMissing -> {
                registerState.copy(errorMessageInput = "Password needs to contain at least one special character", isInputValid = false)
            }

            RegisterInputValidationType.Valid -> {
                registerState.copy(errorMessageInput = null, isInputValid = true)
            }
        }
    }
}