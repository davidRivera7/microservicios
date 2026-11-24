package com.techmart.producto.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.*

data class LoginRequest(
    val usuario: String = "",
    val contrasena: String = ""
)

@RestController
@RequestMapping("/api/productos")
class AuthControl {

    private val passwordEncoder = BCryptPasswordEncoder()

    companion object {

        private const val ENCRYPTED_USER_HASH = "\$2a\$10\$ZgpYQ84y4ehEJ1BXgZBwcOVHmgzhS27GU04KAF5jSpctlxD9ed846"
        private const val ENCRYPTED_PASS_HASH = "\$2a\$10\$ZgpYQ84y4ehEJ1BXgZBwcOVHmgzhS27GU04KAF5jSpctlxD9ed846"
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Map<String, String>> {
        val isUserValid = passwordEncoder.matches(request.usuario, ENCRYPTED_USER_HASH)
        val isPassValid = passwordEncoder.matches(request.contrasena, ENCRYPTED_PASS_HASH)

        return if (isUserValid && isPassValid) {
            ResponseEntity.ok(mapOf("status" to "success", "message" to "Login exitoso"))
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("status" to "error", "message" to "Credenciales incorrectas"))
        }
    }
}
