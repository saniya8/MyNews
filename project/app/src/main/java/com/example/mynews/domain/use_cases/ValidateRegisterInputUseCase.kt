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
        if(email.isEmpty() || password.isEmpty() || passwordRepeated.isEmpty()){
            return RegisterInputValidationType.EmptyField
        }
        if("@" !in email){
            return RegisterInputValidationType.NoEmail
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