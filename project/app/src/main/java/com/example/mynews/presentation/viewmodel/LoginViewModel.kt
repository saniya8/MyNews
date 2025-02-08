package com.example.mynews.presentation.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mynews.domain.model.LoginInputValidationType
import com.example.mynews.domain.repositories.AuthRepository
//import com.example.mynews.domain.repositories.AuthRepository
import com.example.mynews.domain.use_cases.ValidateLoginInputUseCase
import com.example.mynews.presentation.state.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val validateLoginInputUseCase: ValidateLoginInputUseCase,
    private val authRepository: AuthRepository
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


    fun onLoginClick(){

        loginState = loginState.copy(isLoading = true)
        viewModelScope.launch {
            loginState = try{
                val loginResult = authRepository.login(
                    email = loginState.emailInput,
                    password = loginState.passwordInput
                )
                if (!loginResult) {
                    showErrorDialog = true
                    loginState = loginState.copy(
                        errorMessageLoginProcess = "Could not login",
                        isLoading = false
                    )}
                loginState.copy(isSuccessfullyLoggedIn = loginResult)
            }catch(e: Exception){
                showErrorDialog = true
                loginState.copy(
                    errorMessageLoginProcess = "Could not login",
                    isLoading = false
                )
            } finally {
                loginState = loginState.copy(isLoading = false)
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
            LoginInputValidationType.NoEmail -> {
                loginState.copy(errorMessageInput = "Please enter a valid email", isInputValid = false)
            }
            LoginInputValidationType.Valid -> {
                loginState.copy(errorMessageInput = null, isInputValid = true)
            }
        }
    }
}