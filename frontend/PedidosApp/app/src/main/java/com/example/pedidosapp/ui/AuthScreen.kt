package com.example.pedidosapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Importa tus modelos y tu cliente de red
import com.example.pedidosapp.model.ClienteRequest
import com.example.pedidosapp.model.ClienteResponse
import com.example.pedidosapp.RetrofitClient

@Composable
fun AuthScreen(
    onLoginSuccess: (ClienteResponse) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isRegisterMode) "Registro de Cliente" else "Iniciar Sesión",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isRegisterMode) {
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección de entrega") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = ""
                    try {
                        if (isRegisterMode) {
                            val resReg = RetrofitClient.instance.registrarCliente(
                                ClienteRequest(nombre, email, direccion)
                            )
                            if (resReg.isSuccessful) {
                                // Al registrar correctamente, consultamos sus datos para iniciar sesión
                                val resLogin = RetrofitClient.instance.loginCliente(email)
                                if (resLogin.isSuccessful && resLogin.body() != null) {
                                    onLoginSuccess(resLogin.body()!!)
                                } else {
                                    errorMessage = "Registro exitoso, pero ocurrió un error al obtener la cuenta."
                                }
                            } else {
                                errorMessage = "Error al registrar cliente (Código ${resReg.code()})."
                            }
                        } else {
                            val res = RetrofitClient.instance.loginCliente(email.trim())
                            if (res.isSuccessful && res.body() != null) {
                                onLoginSuccess(res.body()!!)
                            } else if (res.code() == 404) {
                                errorMessage = "El correo no existe en la base de datos."
                            } else {
                                errorMessage = "Error de servidor (Código HTTP ${res.code()})."
                            }
                        }
                    } catch (e: Exception) {
                        errorMessage = "Error de conexión: ${e.localizedMessage}"
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isRegisterMode) "Registrarse e Ingresar" else "Entrar")
        }

        TextButton(onClick = {
            isRegisterMode = !isRegisterMode
            errorMessage = ""
        }) {
            Text(
                if (isRegisterMode) "¿Ya tienes cuenta? Inicia sesión"
                else "¿No tienes cuenta? Regístrate aquí"
            )
        }
    }
}