package com.example.mynews.presentation.viewmodel.authentication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.domain.model.RegisterInputValidationType
import com.example.mynews.domain.repositories.AuthRepository
import com.example.mynews.domain.use_cases.ValidateRegisterInputUseCase
import com.example.mynews.presentation.state.RegisterState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val validateRegisterInputUseCase: ValidateRegisterInputUseCase,
    private val authRepository: AuthRepository
): ViewModel() {
    var showErrorDialog by mutableStateOf(false)

    var registerState by mutableStateOf(RegisterState())
        private set

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
            try {
                val registerResult = authRepository.register(
                    email = registerState.emailInput,
                    username = registerState.usernameInput,
                    password = registerState.passwordInput
                )

                if (registerResult) {
                    registerState = registerState.copy(
                        isSuccessfullyRegistered = true,
                        isLoading = false
                    )
                } else {
                    showErrorDialog = true
                    registerState = registerState.copy(
                        errorMessageRegisterProcess = "Could not register",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                showErrorDialog = true
                registerState = registerState.copy(
                    errorMessageRegisterProcess = "Could not register",
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
            RegisterInputValidationType.NoEmail -> {
                registerState.copy(errorMessageInput = "Please enter a valid email", isInputValid = false)
            }
            RegisterInputValidationType.UsernameTooLong -> {
                registerState.copy(errorMessageInput = "Username too long, must be less than 15 characters")
            }
            RegisterInputValidationType.UsernameTooShort -> {
                registerState.copy(errorMessageInput = "Username too short, must be more than 2 characters")
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
            RegisterInputValidationType.PasswordSpecialCharMissing -> {
                registerState.copy(errorMessageInput = "Password needs to contain at least one special character", isInputValid = false)
            }
            RegisterInputValidationType.PasswordNumberMissing -> {
                registerState.copy(errorMessageInput = "Password needs to contain at least one number", isInputValid = false)
            }
            RegisterInputValidationType.Valid -> {
                registerState.copy(errorMessageInput = null, isInputValid = true)
            }
        }
    }
}