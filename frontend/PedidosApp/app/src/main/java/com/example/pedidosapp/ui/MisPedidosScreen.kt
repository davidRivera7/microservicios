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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pedidosapp.RetrofitClient
import com.example.pedidosapp.model.DetallePedidoResponse

@Composable
fun MisPedidosScreen(
    clienteId: Long,
    onVolverClick: () -> Unit
) {
    var pedidos by remember { mutableStateOf<List<DetallePedidoResponse>>(emptyList()) }
    var mensajeEstado by remember { mutableStateOf("") }

    LaunchedEffect(clienteId) {
        try {
            val response = RetrofitClient.instance.obtenerPedidosPorCliente(clienteId)
            if (response.isSuccessful) {
                pedidos = response.body() ?: emptyList()
                if (pedidos.isEmpty()) {
                    mensajeEstado = "No tienes pedidos registrados."
                }
            } else {
                mensajeEstado = "Error al obtener historial de pedidos (${response.code()})."
            }
        } catch (e: Exception) {
            mensajeEstado = "Error al conectar con la API de Pedidos."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Mis Pedidos", style = MaterialTheme.typography.titleLarge)
            OutlinedButton(onClick = onVolverClick) {
                Text("Volver")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (mensajeEstado.isNotEmpty() && pedidos.isEmpty()) {
            Text(text = mensajeEstado, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn {
            items(pedidos) { pedido ->
                ItemPedidoCard(pedido = pedido)
            }
        }
    }
}

@Composable
fun ItemPedidoCard(pedido: DetallePedidoResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pedido #${pedido.id}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Total: $${pedido.precioTotal}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Producto: ${pedido.nombreProducto ?: "Producto ID: ${pedido.productoId}"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Cantidad: ${pedido.cantidad}",
                style = MaterialTheme.typography.bodyMedium
            )

            pedido.fecha?.let { fecha ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fecha: $fecha",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}