package com.example.mynews.utils

import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import com.example.mynews.utils.StringValidationTest

class StringValidationTest {

    @Test
    fun `containsNumber - string does not contain a number`(){
        val result = "NoNumber".containsNumber()
        assertFalse(result)
    }

    @Test
    fun `containsNumber - string contains a number`(){
        val result = "No7Number".containsNumber()
        assertTrue(result)
    }

    @Test
    fun `containsUpperCase - string does not contain an upper case`(){
        val result = "uppercase1".containsUpperCase()
        assertFalse(result)
    }

    @Test
    fun `containsUpperCase - string contains an upper case`(){
        val result = "Uppercase1".containsUpperCase()
        assertTrue(result)
    }

    @Test
    fun `containsSpecialChar - string does not contain a special char`(){
        val result = "SPECIAL4char".containsSpecialChar()
        assertFalse(result)
    }

    @Test
    fun `containsSpecialChar - string contains a special char`(){
        val result = "SPECIAL4!char".containsSpecialChar()
        assertTrue(result)
    }

}