package com.example.mynews.domain.use_cases

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import com.example.mynews.domain.model.RegisterInputValidationType

// Tests Cases here are based off of the cases listed in
// com.example.mynews/domain/model/RegisterInputValidationType

// The following cases were already tested and checked in StringValidationTest so not tested here
//    - PasswordUpperCaseMissing,
//    - PasswordNumberMissing,
//    - PasswordSpecialCharMissing,

class ValidateRegisterInputUseCaseTest {

    private lateinit var validateRegisterInputUseCase: ValidateRegisterInputUseCase

    @Before
    fun setUp() {
        validateRegisterInputUseCase = ValidateRegisterInputUseCase()
    }

    @Test
    fun `case 1 - empty email - should return EmptyField`() {
        val result = validateRegisterInputUseCase("", "username", "password", "password")
        assertEquals(RegisterInputValidationType.EmptyField, result)
    }

    @Test
    fun `case 2 - empty password - should return EmptyField`() {
        val result = validateRegisterInputUseCase("email@example.com", "username", "", "")
        assertEquals(RegisterInputValidationType.EmptyField, result)
    }

    @Test
    fun `case 3 - empty repeated password - should return EmptyField`() {
        val result = validateRegisterInputUseCase("email@example.com", "username", "password", "")
        assertEquals(RegisterInputValidationType.EmptyField, result)
    }

    @Test
    fun `case 4 - no email - should return NoEmail`() {
        val result = validateRegisterInputUseCase("emailexample.com", "username", "password", "password")
        assertEquals(RegisterInputValidationType.NoEmail, result)
    }

    @Test
    fun `case 5 - username too long - should return UsernameTooLong`() {
        val result = validateRegisterInputUseCase("email@example.com", "verylongusername12345", "password", "password")
        assertEquals(RegisterInputValidationType.UsernameTooLong, result)
    }

    @Test
    fun `case 6 - username too short - should return UsernameTooShort`() {
        val result = validateRegisterInputUseCase("email@example.com", "u", "password", "password")
        assertEquals(RegisterInputValidationType.UsernameTooShort, result)
    }

    @Test
    fun `case 7 - passwords do not match - should return PasswordsDoNotMatch`() {
        val result = validateRegisterInputUseCase("email@example.com", "username", "password", "differentpassword")
        assertEquals(RegisterInputValidationType.PasswordsDoNotMatch, result)
    }

    @Test
    fun `case 8 - password too short - should return PasswordTooShort`() {
        val result = validateRegisterInputUseCase("email@example.com", "username", "pass123", "pass123")
        assertEquals(RegisterInputValidationType.PasswordTooShort, result)
    }

    @Test
    fun `case 9 - valid input - should return Valid`() {
        val result = validateRegisterInputUseCase("email@example.com", "username", "Password1!", "Password1!")
        assertEquals(RegisterInputValidationType.Valid, result)
    }

}