package com.example.pedidosapp.model

import com.google.gson.annotations.SerializedName

// --- CLIENTE ---
data class ClienteRequest(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("direccion") val direccion: String,
    @SerializedName("status") val status: Int = 1
)

data class ClienteResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("email") val email: String,
    @SerializedName("direccion") val direccion: String,
    @SerializedName("status") val status: Int? = 1
)