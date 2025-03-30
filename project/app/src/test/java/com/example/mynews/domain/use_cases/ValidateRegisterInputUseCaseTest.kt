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

    private val valid_email = "email@example.com"
    private val valid_username = "my_username.1"
    private val valid_password = "Password1!"

    @Before
    fun setUp() {
        validateRegisterInputUseCase = ValidateRegisterInputUseCase()
    }

    // -----------------------------------------------

    // fields

    @Test
    fun `blank email no spaces - should return EmptyField`() {
        val result = validateRegisterInputUseCase(
            email = "",
            username = valid_username,
            password = valid_password,
            passwordRepeated = valid_password,
        )
        assertEquals(RegisterInputValidationType.EmptyField, result)
    }

    @Test
    fun `blank email with spaces - should return EmptyField`() {
        val result = validateRegisterInputUseCase(
            email = " ",
            username = valid_username,
            password = valid_password,
            passwordRepeated = valid_password,
        )
        assertEquals(RegisterInputValidationType.EmptyField, result)
    }

    @Test
    fun `blank username no spaces - should return EmptyField`() {
        val result = validateRegisterInputUseCase(
            email = valid_email,
            username = "",
            password = valid_password,
            passwordRepeated = valid_password,
        )
        assertEquals(RegisterInputValidationType.EmptyField, result)
    }

    @Test
    fun `blank username with spaces - should return EmptyField`() {
        val result = validateRegisterInputUseCase(
            email = valid_email,
            username = "   ",
            password = valid_password,
            passwordRepeated = valid_password,
        )
        assertEquals(RegisterInputValidationType.EmptyField, result)
    }

    @Test
    fun `empty password no spaces - should return EmptyField`() {
        val result = validateRegisterInputUseCase(
            email = valid_email,
            username = valid_username,
            password = "",
            passwordRepeated = valid_password,
        )
        assertEquals(RegisterInputValidationType.EmptyField, result)
    }

    @Test
    fun `empty password with spaces - should not return EmptyField`() {
        val result = validateRegisterInputUseCase(
            email = valid_email,
            username = valid_username,
            password = " ",
            passwordRepeated = valid_password,
        )
        assertNotEquals(RegisterInputValidationType.EmptyField, result)
    }

    @Test
    fun `empty password repeated no spaces - should return EmptyField`() {
        val result = validateRegisterInputUseCase(
            email = valid_email,
            username = valid_username,
            password = valid_password,
            passwordRepeated = "",
        )
        assertEquals(RegisterInputValidationType.EmptyField, result)
    }

    @Test
    fun `empty password repeated with spaces - should not return EmptyField`() {
        val result = validateRegisterInputUseCase(
            email = valid_email,
            username = valid_username,
            password = valid_password,
            passwordRepeated = "        ",
        )
        assertNotEquals(RegisterInputValidationType.EmptyField, result)
    }

    // -----------------------------------------------

    // emails

    @Test
    fun `invalid email no @ - should return InvalidEmail`() {
        val result = validateRegisterInputUseCase(
            email = "emailexample.com",
            username = valid_username,
            password = valid_password,
            passwordRepeated = valid_password,
        )
        assertEquals(RegisterInputValidationType.InvalidEmail, result)
    }

    @Test
    fun `invalid email missing domain - should return InvalidEmail`() {
        val result = validateRegisterInputUseCase(
            email = "email@",
            username = valid_username,
            password = valid_password,
            passwordRepeated = valid_password,
        )
        assertEquals(RegisterInputValidationType.InvalidEmail, result)
    }

    @Test
    fun `invalid email missing part before @ - should return InvalidEmail`() {
        val result = validateRegisterInputUseCase(
            email = "@example..com",
            username = valid_username,
            password = valid_password,
            passwordRepeated = valid_password,
        )
        assertEquals(RegisterInputValidationType.InvalidEmail, result)
    }

    @Test
    fun `invalid email with no dot after domain - should return InvalidEmail`() {
        val result = validateRegisterInputUseCase(
            email = "email@example",
            username = valid_username,
            password = valid_password,
            passwordRepeated = valid_password,
        )
        assertEquals(RegisterInputValidationType.InvalidEmail, result)
    }

    @Test
    fun `invalid email with invalid characters - should return InvalidEmail`() {
        val result = validateRegisterInputUseCase(
            email = "ema!l~@example.com",
            username = valid_username,
            password = valid_password,
            passwordRepeated = valid_password,
        )
        assertEquals(RegisterInputValidationType.InvalidEmail, result)
    }

    @Test
    fun `invalid email with spaces - should return InvalidEmail`() {
        val result = validateRegisterInputUseCase(
            email = "email @example.com",
            username = valid_username,
            password = valid_password,
            passwordRepeated = valid_password,
        )
        assertEquals(RegisterInputValidationType.InvalidEmail, result)
    }


    // -----------------------------------------------

    // usernames

    @Test
    fun `username with space - should return InvalidUsernameCharacters`() {
        val result = validateRegisterInputUseCase(
            email = valid_email,
            username = "user name",
            password = valid_password,
            passwordRepeated = valid_password,
        )
        assertEquals(RegisterInputValidationType.InvalidUsernameCharacters, result)
    }

    @Test
    fun `username with special char - should return InvalidUsernameCharacters`() {
        val result = validateRegisterInputUseCase(
            email = valid_email,
            username = "user!name",
            password = valid_password,
            passwordRepeated = valid_password,
        )
        assertEquals(RegisterInputValidationType.InvalidUsernameCharacters, result)
    }

    @Test
    fun `username with emoji - should return InvalidUsernameCharacters`() {
        val result = validateRegisterInputUseCase(
            email = valid_email,
            username = "user😀",
            password = valid_password,
            passwordRepeated = valid_password,
        )
        assertEquals(RegisterInputValidationType.InvalidUsernameCharacters, result)
    }


    @Test
    fun `username too long - should return UsernameTooLong`() {
        val result = validateRegisterInputUseCase(
            email = valid_email,
            username = "verylongusername12345",
            password = valid_password,
            passwordRepeated = valid_password,
        )
        assertEquals(RegisterInputValidationType.UsernameTooLong, result)
    }

    @Test
    fun `username too short - should return UsernameTooShort`() {
        val result = validateRegisterInputUseCase(
            email = valid_email,
            username = "um",
            password = valid_password,
            passwordRepeated = valid_password,
        )
        assertEquals(RegisterInputValidationType.UsernameTooShort, result)
    }

    // -----------------------------------------------

    // passwords

    @Test
    fun `password too short - should return PasswordTooShort`() {
        val result = validateRegisterInputUseCase(
            email = valid_email,
            username = valid_username,
            password = "Pass!23",
            passwordRepeated = "Pass!23",
        )
        assertEquals(RegisterInputValidationType.PasswordTooShort, result)
    }


    @Test
    fun `passwords valid individually but do not match - should return PasswordsDoNotMatch`() {
        val result = validateRegisterInputUseCase(
            email = valid_email,
            username = valid_username,
            password = "Password1!",
            passwordRepeated = "Password2!",
        )
        assertEquals(RegisterInputValidationType.PasswordsDoNotMatch, result)
    }


    @Test
    fun `password missing uppercase - should return PasswordUpperCaseMissing`() {
        val result = validateRegisterInputUseCase(
            email = valid_email,
            username = valid_username,
            password = "password1!",
            passwordRepeated = "password1!",
        )
        assertEquals(RegisterInputValidationType.PasswordUpperCaseMissing, result)
    }

    @Test
    fun `password missing number - should return PasswordNumberMissing`() {
        val result = validateRegisterInputUseCase(
            email = valid_email,
            username = valid_username,
            password = "Password!",
            passwordRepeated = "Password!",
        )
        assertEquals(RegisterInputValidationType.PasswordNumberMissing, result)
    }


    @Test
    fun `password missing special character - should return PasswordSpecialCharMissing`() {
        val result = validateRegisterInputUseCase(
            email = valid_email,
            username = valid_username,
            password = "Password1",
            passwordRepeated = "Password1",
        )
        assertEquals(RegisterInputValidationType.PasswordSpecialCharMissing, result)
    }


    // -----------------------------------------------

    // valid

    @Test
    fun `valid input - should return Valid`() {
        val result = validateRegisterInputUseCase(
            email = valid_email,
            username = valid_username,
            password = valid_password,
            passwordRepeated = valid_password
        )
        assertEquals(RegisterInputValidationType.Valid, result)
    }

}