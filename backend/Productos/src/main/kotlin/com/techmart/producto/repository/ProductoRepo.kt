package com.techmart.producto.repository

import com.techmart.producto.model.Producto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface ProductoRepo : JpaRepository<Producto, Long>