package com.example.mynews.domain.use_cases

import com.example.mynews.domain.model.LoginInputValidationType
import com.example.mynews.domain.model.RegisterInputValidationType

class ValidateLoginInputUseCase() {
    operator fun invoke(email: String, password:String):LoginInputValidationType{

        // emails and usernames can't have spaces, so can't be blank (empty or whitespaced)
        // passwords can have spaces, so just check if empty
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
