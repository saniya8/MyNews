package com.example.mynews.domain.entities

enum class RegisterInputValidationType {
    EmptyField,
    InvalidEmail,
    InvalidUsernameCharacters,
    UsernameTooLong,
    UsernameTooShort,
    PasswordTooShort,
    PasswordsDoNotMatch,
    PasswordUpperCaseMissing,
    PasswordNumberMissing,
    PasswordSpecialCharMissing,
    Valid
}