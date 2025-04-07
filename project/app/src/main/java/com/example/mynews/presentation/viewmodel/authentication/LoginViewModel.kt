package com.example.mynews.presentation.viewmodel.authentication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.domain.model.authentication.LoginModel
import com.example.mynews.domain.types.LoginInputValidationType
import com.example.mynews.domain.use_cases.ValidateLoginInputUseCase
import com.example.mynews.presentation.state.LoginState
import com.example.mynews.utils.logger.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    private val validateLoginInputUseCase: ValidateLoginInputUseCase,
    private val loginModel: LoginModel,
    private val logger: Logger,
): ViewModel() {

    var loginState by mutableStateOf(LoginState())
        private set

    var showErrorDialog by mutableStateOf(false)

    fun onEmailInputChange(newValue: String){
        loginState = loginState.copy(emailInput = newValue)
        checkInputValidation()
    }

    fun onPasswordInputChange(newValue: String){
        loginState = loginState.copy(passwordInput = newValue)
        checkInputValidation()
    }

    fun onToggleVisualTransformation(){
        loginState = loginState.copy(isPasswordShown = !loginState.isPasswordShown)
    }

    fun onLoginClick() {
        viewModelScope.launch {
            loginState = loginState.copy(isLoading = true)
            try {
                val loginResult = loginModel.performLogin(
                    email = loginState.emailInput, // normalized in authRepository.login
                    password = loginState.passwordInput
                )

                if (loginResult) {
                    loginState = loginState.copy(
                        isSuccessfullyLoggedIn = true,
                        isLoading = false
                    )
                } else {
                    showErrorDialog = true
                    loginState = loginState.copy(
                        errorMessageLogin = "Could not login",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                showErrorDialog = true
                loginState = loginState.copy(
                    errorMessageLogin = "Could not login",
                    isLoading = false
                )
            }
        }
    }

    private fun checkInputValidation(){
        val validationResult = validateLoginInputUseCase(
            loginState.emailInput,
            loginState.passwordInput
        )
        processInputValidationType(validationResult)
    }

    private fun processInputValidationType(type: LoginInputValidationType){
        loginState = when(type){
            LoginInputValidationType.EmptyField -> {
                loginState.copy(errorMessageInput = "Please fill in empty fields", isInputValid = false)
            }
            LoginInputValidationType.InvalidEmail -> {
                loginState.copy(errorMessageInput = "Please enter a valid email", isInputValid = false)
            }
            LoginInputValidationType.Valid -> {
                loginState.copy(errorMessageInput = null, isInputValid = true)
            }
        }
    }
}