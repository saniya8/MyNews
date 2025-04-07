package com.example.mynews.domain.use_cases

import com.example.mynews.domain.types.LoginInputValidationType

class ValidateLoginInputUseCase() {
    operator fun invoke(email: String, password:String): LoginInputValidationType {

        // emails and usernames cannot be empty or have only spaces
        // passwords can have spaces
        if (email.isBlank() || password.isEmpty()) {
            return LoginInputValidationType.EmptyField
        }

        val trimmedEmail = email.trim()
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,20}$")

        if (!emailRegex.matches(trimmedEmail)) {
            return LoginInputValidationType.InvalidEmail
        }

        return LoginInputValidationType.Valid
    }

}
