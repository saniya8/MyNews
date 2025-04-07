package com.example.mynews.domain.use_cases

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Before
import com.example.mynews.domain.types.LoginInputValidationType

// Tests Cases here are based off of the cases listed in
// com.example.mynews/domain/model/LoginInputValidationType

class ValidateLoginInputUseCaseTest {

    private lateinit var validateLoginInputUseCase: ValidateLoginInputUseCase

    private val valid_email = "email@example.com"
    private val valid_password = "Password1!"

    @Before
    fun setUp() {
        validateLoginInputUseCase = ValidateLoginInputUseCase()
    }

    // -----------------------------------------------

    // fields

    @Test
    fun `blank email and empty password - should return EmptyField`() {
        val result = validateLoginInputUseCase(
            email = "",
            password = ""
        )
        assertEquals(LoginInputValidationType.EmptyField, result)
    }

    @Test
    fun `blank email nonempty password - should return EmptyField`() {
        val result = validateLoginInputUseCase(
            email = "",
            password = valid_password,
        )
        assertEquals(LoginInputValidationType.EmptyField, result)
    }

    @Test
    fun `non-blank email empty password - should return EmptyField`() {
        val result = validateLoginInputUseCase(
            email = valid_email,
            password = "",
        )
        assertEquals(LoginInputValidationType.EmptyField, result)
    }

    // -----------------------------------------------

    // emails

    @Test
    fun `invalid email no @ - should return InvalidEmail`() {
        val result = validateLoginInputUseCase(
            email = "emailexample.com",
            password = valid_password,
        )
        assertEquals(LoginInputValidationType.InvalidEmail, result)
    }

    @Test
    fun `invalid email missing domain - should return InvalidEmail`() {
        val result = validateLoginInputUseCase(
            email = "email@",
            password = valid_password,
        )
        assertEquals(LoginInputValidationType.InvalidEmail, result)
    }

    @Test
    fun `invalid email missing part before @ - should return InvalidEmail`() {
        val result = validateLoginInputUseCase(
            email = "@example..com",
            password = valid_password,
        )
        assertEquals(LoginInputValidationType.InvalidEmail, result)
    }

    @Test
    fun `invalid email with no dot after domain - should return InvalidEmail`() {
        val result = validateLoginInputUseCase(
            email = "email@example",
            password = valid_password,
        )
        assertEquals(LoginInputValidationType.InvalidEmail, result)
    }

    @Test
    fun `invalid email with invalid characters - should return InvalidEmail`() {
        val result = validateLoginInputUseCase(
            email = "ema!l~@example.com",
            password = valid_password,
        )
        assertEquals(LoginInputValidationType.InvalidEmail, result)
    }

    @Test
    fun `invalid email with spaces - should return InvalidEmail`() {
        val result = validateLoginInputUseCase(
            email = "email @example.com",
            password = valid_password,
        )
        assertEquals(LoginInputValidationType.InvalidEmail, result)
    }

    // -----------------------------------------------

    // valid

    @Test
    fun `valid email and password should return Valid`() {
        val result = validateLoginInputUseCase(
            email = valid_email,
            password = valid_password,
        )
        assertEquals(LoginInputValidationType.Valid, result)
    }

}