package models

import "time"

// PeticionPedido es el JSON que envía el cliente
type PeticionPedido struct {
	ClienteID  string `json:"cliente_id"`
	ProductoID string `json:"producto_id"`
	Cantidad   int    `json:"cantidad"`
}

// RespuestaProducto representa la respuesta del Servicio de Productos
type RespuestaProducto struct {
	ID     string  `json:"id"`
	Nombre string  `json:"nombre"`
	Precio float64 `json:"precio"`
	Stock  int     `json:"stock"`
}

// RespuestaCliente representa la respuesta del Servicio de Clientes
type RespuestaCliente struct {
	ID    string `json:"id"`
	Nombre string `json:"nombre"`
	Email string `json:"email"`
}

// Pedido es la entidad que guardamos en la base de datos
type Pedido struct {
	ID            int    	`json:"id" db:"id"`
	ClienteID     string    `json:"cliente_id"`
	NombreCliente string    `json:"nombre_cliente"`
	ProductoID    string    `json:"producto_id"`
	NombreProd    string    `json:"nombre_producto"`
	Cantidad      int       `json:"cantidad"`
	PrecioTotal   float64   `json:"precio_total"`
	Fecha         time.Time `json:"fecha"`
}

//Es la estructura que valida para rabbit
type PedidoNotificacion struct {
	ID            int     `json:"id"`
	ClienteID     int     `json:"cliente_id"`
	NombreCliente string  `json:"nombre_cliente"`
	EmailCliente  string  `json:"email_cliente"`
	ProductoID    int     `json:"producto_id"`
	NombreProd    string  `json:"nombre_prod,omitempty"`
	Cantidad      int     `json:"cantidad"`
	PrecioTotal   float64 `json:"precio_total"`
}