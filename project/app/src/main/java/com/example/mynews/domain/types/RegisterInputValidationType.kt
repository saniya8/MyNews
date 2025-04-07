package com.example.mynews.domain.types

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