package com.example.pedidosapp.model

// --- PRODUCTO ---
data class Producto(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val stock: Int
)