package com.example.pedidosapp.model

import com.google.gson.annotations.SerializedName

// --- SOLICITUD DE CREACIÓN DE PEDIDO ---
data class CrearPedidoRequest(
    @SerializedName("cliente_id") val clienteId: String,
    @SerializedName("producto_id") val productoId: String,
    @SerializedName("cantidad") val cantidad: Int
)

// --- RESPUESTA DE PEDIDO REGISTRADO / HISTORIAL ---
data class DetallePedidoResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("cliente_id") val clienteId: String,
    @SerializedName("nombre_cliente") val nombreCliente: String?,
    @SerializedName("producto_id") val productoId: String,
    @SerializedName("nombre_producto") val nombreProducto: String?,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precio_total") val precioTotal: Double,
    @SerializedName("fecha") val fecha: String?
)