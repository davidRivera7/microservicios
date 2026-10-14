package com.example.pedidosapp

import com.example.pedidosapp.model.ClienteRequest
import com.example.pedidosapp.model.ClienteResponse
import com.example.pedidosapp.model.CrearPedidoRequest
import com.example.pedidosapp.model.DetallePedidoResponse
import com.example.pedidosapp.model.Producto
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- CLIENTES ---
    @POST("api/Clientes/Autenticar/{correo}/{pass}")
    suspend fun loginCliente(
        @Path("correo") correo: String,
        @Path("pass") pass:String
    ): Response<ClienteResponse>

    @POST("api/Clientes/Insertar")
    suspend fun registrarCliente(
        @Body cliente: ClienteRequest
    ): Response<Map<String, String>>

    @PUT("api/Clientes/Actualizar/{id}")
    suspend fun modificarCliente(
        @Path("id") id: Long,
        @Body cliente: ClienteRequest
    ): Response<Map<String, String>>

    @PUT("api/Clientes/ActualizarContrasena/{id}/{passActual}")
    suspend fun actualizarContrasena(
        @Path("id") id: Long,
        @Path("passActual") passActual: String,
        @Body cliente: ClienteRequest
    ): Response<Map<String, String>>

    @DELETE("api/Clientes/DarDeBaja/{id}")
    suspend fun desactivarCuenta(
        @Path("id") id: Long
    ): Response<Unit>

    // --- PRODUCTOS ---
    @GET("api/productos")
    suspend fun obtenerProductos(): Response<List<Producto>>

    // --- PEDIDOS ---
    @POST("api/pedidos")
    suspend fun crearPedido(
        @Body pedido: CrearPedidoRequest
    ): Response<DetallePedidoResponse>

    @GET("api/pedidos/cliente/{clienteId}")
    suspend fun obtenerPedidosPorCliente(
        @Path("clienteId") clienteId: Long
    ): Response<List<DetallePedidoResponse>>


}