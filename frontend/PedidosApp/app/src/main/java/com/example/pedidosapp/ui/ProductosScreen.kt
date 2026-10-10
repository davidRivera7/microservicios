package com.example.pedidosapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import com.example.pedidosapp.model.ClienteResponse
import com.example.pedidosapp.model.CrearPedidoRequest
import com.example.pedidosapp.model.Producto
import com.example.pedidosapp.RetrofitClient

@Composable
fun ProductosScreen(
    cliente: ClienteResponse,
    onVerMisPedidosClick: () -> Unit,
    onCerrarSesionClick: () -> Unit
) {
    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var mensajeEstado by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    // Cargar productos al abrir la pantalla
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.instance.obtenerProductos()
            if (response.isSuccessful) {
                productos = response.body() ?: emptyList()
            } else {
                mensajeEstado = "Error al obtener productos (${response.code()})"
            }
        } catch (e: Exception) {
            mensajeEstado = "Error al cargar catálogo."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        // Cabecera superior
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Hola,", style = MaterialTheme.typography.bodyMedium)
                Text(text = cliente.nombre, style = MaterialTheme.typography.titleLarge)
            }

            Row {
                OutlinedButton(onClick = onCerrarSesionClick) {
                    Text("Salir")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onVerMisPedidosClick) {
                    Text("Mis Pedidos")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (mensajeEstado.isNotEmpty()) {
            Text(text = mensajeEstado, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn {
            items(productos) { producto ->
                ItemProductoCard(
                    producto = producto,
                    onComprar = { cantidad ->
                        coroutineScope.launch {
                            try {
                                val req = CrearPedidoRequest(
                                    clienteId = cliente.id.toString(),
                                    productoId = producto.id.toString(),
                                    cantidad = cantidad
                                )
                                val res = RetrofitClient.instance.crearPedido(req)
                                if (res.isSuccessful) {
                                    val pedidoCreado = res.body()
                                    mensajeEstado = "¡Pedido #${pedidoCreado?.id ?: ""} creado con éxito!"
                                } else {
                                    mensajeEstado = "Error al crear pedido (Stock insuficiente o cliente no válido)."
                                }
                            } catch (e: Exception) {
                                mensajeEstado = "Error al conectar con la API de Pedidos."
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ItemProductoCard(
    producto: Producto,
    onComprar: (Int) -> Unit
) {
    var cantidad by remember { mutableStateOf("1") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = producto.nombre, style = MaterialTheme.typography.titleMedium)
            Text(text = "Precio: $${producto.precio} | Stock: ${producto.stock}")
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it },
                    label = { Text("Cant.") },
                    modifier = Modifier.width(80.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        val cantInt = cantidad.toIntOrNull() ?: 1
                        onComprar(cantInt)
                    },
                    enabled = producto.stock > 0
                ) {
                    Text("Realizar Pedido")
                }
            }
        }
    }
}