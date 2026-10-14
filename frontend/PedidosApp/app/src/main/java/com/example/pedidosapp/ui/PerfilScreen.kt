package com.example.pedidosapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.pedidosapp.model.ClienteRequest
import com.example.pedidosapp.model.ClienteResponse
import com.example.pedidosapp.RetrofitClient


@Composable
fun PerfilScreen(
    clienteActual: ClienteResponse,
    onVolverClick: () -> Unit,
    onCuentaModificada: (ClienteResponse) -> Unit, // Actualiza el estado global de la app
    onGuardarExitoso: (String) -> Unit, // manda mensaje de exito
    onCuentaEliminada: () -> Unit // Cierra sesión y manda al AuthScreen
) {
    // Estas son las variables de las cajas de texto, pre-llenadas con la info actual
    var nombre by remember { mutableStateOf(clienteActual.nombre) }
    var direccion by remember { mutableStateOf(clienteActual.direccion) }
    var password by remember { mutableStateOf("") } // Se deja en blanco por seguridad

    var isLoading by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }


    // Controla si se muestra el cuadro de advertencia antes de eliminar
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarDialogoPassword by remember { mutableStateOf(false) }

    // Estados para el cambio de contraseña
    var passActual by remember { mutableStateOf("") }
    var nuevaPass by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Mi Perfil", style = MaterialTheme.typography.titleLarge)
            OutlinedButton(onClick = onVolverClick) {
                Text("Volver")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // CAJA DE TEXTO 1: Nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // CAJA DE TEXTO 2: Dirección
        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it },
            label = { Text("Dirección") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = clienteActual.email,
            onValueChange = { },
            label = { Text("Correo electrónico") },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (mensaje.isNotEmpty()) {
            Text(text = mensaje, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // BOTÓN: Modificar Perfil
        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    try {
                        // Construimos la petición con los datos de las cajas de texto
                        val request = ClienteRequest(
                            nombre = nombre,
                            email = clienteActual.email, // El correo no se cambia
                            contrasena = "",  // no importa porque la api la ignora
                            direccion = direccion,
                            status = 1
                        )

                        val response = RetrofitClient.instance.modificarCliente(clienteActual.id, request)
                        if (response.isSuccessful) {
                            // Actualizamos la información en el resto de la app
                            val clienteActualizado = clienteActual.copy(
                                nombre = nombre,
                                direccion = direccion
                            )
                            onCuentaModificada(clienteActualizado)
                            onGuardarExitoso("Datos actualizados correctamente.")
                        } else {
                            mensaje = "Error al actualizar los datos."
                        }
                    } catch (e: Exception) {
                        mensaje = "Error de conexión."
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text("Guardar Cambios")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // BOTÓN: Eliminar Cuenta
        OutlinedButton(
            onClick = { mostrarDialogoEliminar = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Eliminar Cuenta")
        }
    }

    // CUADRO DE DIÁLOGO: Cambio de Contraseña
    if (mostrarDialogoPassword) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoPassword = false },
            title = { Text("Cambiar Contraseña") },
            text = {
                Column {
                    Text("Ingresa tu contraseña actual por seguridad y la nueva que deseas asignar.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = passActual,
                        onValueChange = { passActual = it },
                        label = { Text("Contraseña actual") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nuevaPass,
                        onValueChange = { nuevaPass = it },
                        label = { Text("Nueva contraseña") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isLoading && passActual.isNotBlank() && nuevaPass.isNotBlank(),
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                // Enviamos la petición al nuevo endpoint
                                val request = ClienteRequest(
                                    nombre = nombre,
                                    email = clienteActual.email,
                                    contrasena = nuevaPass, // Esta es la que se encriptará
                                    direccion = direccion,
                                    status = 1
                                )
                                val response = RetrofitClient.instance.actualizarContrasena(
                                    id = clienteActual.id,
                                    passActual = passActual, // Esta valida contra la BD
                                    cliente = request
                                )

                                if (response.isSuccessful) {
                                    mensaje = "Contraseña actualizada exitosamente."
                                    mostrarDialogoPassword = false
                                } else if (response.code() == 400) {
                                    mensaje = "La contraseña actual es incorrecta."
                                    mostrarDialogoPassword = false
                                } else {
                                    mensaje = "Error al actualizar contraseña."
                                }
                            } catch (e: Exception) {
                                mensaje = "Error de conexión."
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                ) {
                    Text("Actualizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPassword = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // CUADRO DE DIÁLOGO: Confirmación de Soft Delete
    if (mostrarDialogoEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = { Text("¿Estás seguro?") },
            text = { Text("Tu cuenta será desactivada y ya no podrás iniciar sesión") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val response = RetrofitClient.instance.desactivarCuenta(clienteActual.id)
                                if (response.isSuccessful) {
                                    onCuentaEliminada()
                                } else {
                                    mensaje = "No se pudo desactivar la cuenta."
                                    mostrarDialogoEliminar = false
                                }
                            } catch (e: Exception) {
                                mensaje = "Error de conexión al eliminar."
                                mostrarDialogoEliminar = false
                            }
                        }
                    }
                ) {
                    Text("Sí, eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}