package com.techmart.producto.controller

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class AuthControlTest {

    private val authControl = AuthControl()

    @Test
    fun testLoginSuccessWithAdminCredentials() {
        val request = LoginRequest(usuario = "Admin", contrasena = "Admin")
        val response = authControl.login(request)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("success", response.body?.get("status"))
    }

    @Test
    fun testLoginFailureWithInvalidCredentials() {
        val requestWrongUser = LoginRequest(usuario = "User", contrasena = "Admin")
        val response1 = authControl.login(requestWrongUser)

        assertEquals(HttpStatus.UNAUTHORIZED, response1.statusCode)
        assertEquals("error", response1.body?.get("status"))

        val requestWrongPass = LoginRequest(usuario = "Admin", contrasena = "WrongPass")
        val response2 = authControl.login(requestWrongPass)

        assertEquals(HttpStatus.UNAUTHORIZED, response2.statusCode)
        assertEquals("error", response2.body?.get("status"))
    }

    @Test
    fun generateAndVerifyBCryptHash() {
        val encoder = BCryptPasswordEncoder()
        val hash = encoder.encode("Admin")
        println("GENERATED_BCRYPT_HASH_FOR_ADMIN: $hash")
        assertTrue(encoder.matches("Admin", hash))
    }
}
