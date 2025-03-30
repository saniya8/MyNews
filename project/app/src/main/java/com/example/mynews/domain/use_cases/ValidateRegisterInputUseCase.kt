package com.example.mynews.domain.use_cases

import com.example.mynews.domain.model.RegisterInputValidationType
import com.example.mynews.utils.containsNumber
import com.example.mynews.utils.containsSpecialChar
import com.example.mynews.utils.containsUpperCase

class ValidateRegisterInputUseCase {
    operator fun invoke(
        email: String,
        username: String,
        password: String,
        passwordRepeated: String
    ): RegisterInputValidationType {

        // emails and usernames can't have spaces, so can't be blank (empty or whitespaced)
        // passwords can have spaces, so just check if empty
        if (email.isBlank() || username.isBlank() || password.isEmpty() || passwordRepeated.isEmpty()) {
            return RegisterInputValidationType.EmptyField
        }

        val trimmedEmail = email.trim()
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,20}$")

        if (!emailRegex.matches(trimmedEmail)) {
            return RegisterInputValidationType.InvalidEmail
        }

        if (!username.matches(Regex("^[a-zA-Z0-9._]+$"))) {
            return RegisterInputValidationType.InvalidUsernameCharacters
        }

        if(username.count() > 20){
            return RegisterInputValidationType.UsernameTooLong
        }
        if(username.count() < 3){
            return RegisterInputValidationType.UsernameTooShort
        }

        if(password!= passwordRepeated){
            return RegisterInputValidationType.PasswordsDoNotMatch
        }
        if(password.count() < 8){
            return RegisterInputValidationType.PasswordTooShort
        }
        if(!password.containsNumber()){
            return RegisterInputValidationType.PasswordNumberMissing
        }
        if(!password.containsUpperCase()){
            return RegisterInputValidationType.PasswordUpperCaseMissing
        }
        if(!password.containsSpecialChar()){
            return RegisterInputValidationType.PasswordSpecialCharMissing
        }
        return RegisterInputValidationType.Valid
    }
}