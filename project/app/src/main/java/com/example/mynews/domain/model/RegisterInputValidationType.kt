package com.example.mynews.domain.model

enum class RegisterInputValidationType {
    EmptyField,
    NoEmail,
    UsernameTooLong,
    UsernameTooShort,
    PasswordsDoNotMatch,
    PasswordUpperCaseMissing,
    PasswordNumberMissing,
    PasswordSpecialCharMissing,
    PasswordTooShort,
    Valid
}