package com.techmart.producto.model
import jakarta.persistence.*

@Entity
@Table(name = "productos")
data class Producto(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long = 0,
    @Column(nullable = false)
        var nombre: String,
    @Column(nullable = false)
        var precio: Double,
    @Column(nullable = false)
        var stock: Int
){ constructor() : this(0, "", 0.0, 0) }