package com.example.mynews.domain.use_cases

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.Before
import com.example.mynews.domain.model.LoginInputValidationType

// Tests Cases here are based off of the cases listed in
// com.example.mynews/domain/model/LoginInputValidationType

class ValidateLoginInputUseCaseTest {

    private lateinit var validateLoginInputUseCase: ValidateLoginInputUseCase

    @Before
    fun setUp() {
        validateLoginInputUseCase = ValidateLoginInputUseCase()
    }

    @Test
    fun `case 1 - empty email and password - should return EmptyField`() {
        val result = validateLoginInputUseCase("", "")
        assertEquals(LoginInputValidationType.EmptyField, result)
    }

    @Test
    fun `case 2 - empty email nonempty password - should return EmptyField`() {
        val result = validateLoginInputUseCase("", "password")
        assertEquals(LoginInputValidationType.EmptyField, result)
    }

    @Test
    fun `case 3 - nonempty email empty password - should return EmptyField`() {
        val result = validateLoginInputUseCase("email@example.com", "")
        assertEquals(LoginInputValidationType.EmptyField, result)
    }

    @Test
    fun `case 4 - invalid email should return NoEmail`() {
        val result = validateLoginInputUseCase("emailexample.com", "password")
        assertEquals(LoginInputValidationType.NoEmail, result)
    }

    @Test
    fun `case 5 - valid email and password should return Valid`() {
        val result = validateLoginInputUseCase("email@example.com", "password")
        assertEquals(LoginInputValidationType.Valid, result)
    }
}