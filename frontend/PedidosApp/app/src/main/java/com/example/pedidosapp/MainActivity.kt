    package com.example.pedidosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.pedidosapp.model.ClienteResponse
import com.example.pedidosapp.ui.AuthScreen
import com.example.pedidosapp.ui.MisPedidosScreen
import com.example.pedidosapp.ui.ProductosScreen
import  com.example.pedidosapp.ui.PerfilScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppPedidosNavegacion()
                }
            }
        }
    }
}

@Composable
fun AppPedidosNavegacion() {
    // Estado de la pantalla actual (Comienza en Auth / Login)
    var pantallaActual by remember { mutableStateOf(Pantalla.AUTH) }

    // Estado para guardar la información del cliente una vez autenticado
    var clienteAutenticado by remember { mutableStateOf<ClienteResponse?>(null) }

    when (pantallaActual) {
        Pantalla.AUTH -> {
            AuthScreen(
                onLoginSuccess = { cliente ->
                    clienteAutenticado = cliente
                    pantallaActual = Pantalla.PRODUCTOS
                }
            )
        }

        Pantalla.PRODUCTOS -> {
            clienteAutenticado?.let { cliente ->
                ProductosScreen(
                    cliente = cliente,
                    onVerMisPedidosClick = {
                        pantallaActual = Pantalla.MIS_PEDIDOS
                    },
                    onPerfilClick = { pantallaActual = Pantalla.PERFIL  },
                    onCerrarSesionClick = {
                        clienteAutenticado = null
                        pantallaActual = Pantalla.AUTH
                    }
                )
            }
        }

        Pantalla.MIS_PEDIDOS -> {
            clienteAutenticado?.let { cliente ->
                MisPedidosScreen(
                    clienteId = cliente.id,
                    onVolverClick = {
                        pantallaActual = Pantalla.PRODUCTOS
                    }
                )
            }
        }

        Pantalla.PERFIL -> {
            clienteAutenticado?.let { cliente ->
                PerfilScreen(
                    clienteActual = cliente,
                    onVolverClick = {
                        pantallaActual = Pantalla.PRODUCTOS
                    },
                    onCuentaModificada = { clienteActualizado ->
                        clienteAutenticado = clienteActualizado
                    },
                    onCuentaEliminada = {
                        clienteAutenticado = null
                        pantallaActual = Pantalla.AUTH
                    }
                )
            }
        }
    }
}

enum class Pantalla {
    AUTH,          // Login y Registro
    PRODUCTOS,     // Catálogo y creación de pedido
    MIS_PEDIDOS,    // Historial de pedidos

    PERFIL // Modificacion de perfil
}