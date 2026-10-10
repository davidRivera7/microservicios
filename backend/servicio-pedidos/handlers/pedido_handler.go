package handlers

import (
    "encoding/json"
    "fmt"
    "net/http"
    "os"
    "time"
    "log"

    "servicio-pedidos/db"     
    "servicio-pedidos/messaging" 
    "servicio-pedidos/models"
)

func CrearPedidoHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodPost {
		http.Error(w, "Método no permitido", http.StatusMethodNotAllowed)
		return
	}

	var req models.PeticionPedido
	err := json.NewDecoder(r.Body).Decode(&req)
	if err != nil || req.Cantidad <= 0 {
		http.Error(w, "Datos del pedido inválidos", http.StatusBadRequest)
		return
	}
    
	// Obtener URLs de los servicios desde variables de entorno (con valores por defecto)
	urlClientes := obtenerEnv("CLIENTES_SERVICE_URL", "http://localhost:8083/api")
	urlProductos := obtenerEnv("PRODUCTOS_SERVICE_URL", "http://localhost:8081/api")
    
	// 1. Validar Cliente en Servicio de Clientes	
    // 1. Validar Cliente en Servicio de Clientes
	urlClienteFinal := fmt.Sprintf("%s/clientes/ObtenerPorID/%s", urlClientes, req.ClienteID)    
    log.Printf("Consultando cliente en URL: %s\n", urlClienteFinal)
    
    respCliente, err := http.Get(urlClienteFinal)
	if err != nil || respCliente.StatusCode != http.StatusOK {
		http.Error(w, "El cliente especificado no existe o no responde", http.StatusNotFound)
		return
	}
	defer respCliente.Body.Close()

	var cliente models.RespuestaCliente
	json.NewDecoder(respCliente.Body).Decode(&cliente)

	// 2. Validar Producto y Precio en Servicio de Productos
	respProd, err := http.Get(fmt.Sprintf("%s/productos/%s", urlProductos, req.ProductoID))
	if err != nil || respProd.StatusCode != http.StatusOK {
		http.Error(w, "El producto especificado no existe o no responde", http.StatusNotFound)
		return
	}
	defer respProd.Body.Close()

	var producto models.RespuestaProducto
	json.NewDecoder(respProd.Body).Decode(&producto)

	if producto.Stock < req.Cantidad {
		http.Error(w, "Stock insuficiente para realizar el pedido", http.StatusBadRequest)
		return
	}

	// 3. Actualizar stock del producto en la BD productos
	urlDescuento := fmt.Sprintf("%s/productos/%s/descontar-stock?cantidad=%d", urlProductos, req.ProductoID, req.Cantidad)
	reqUpdate, _ := http.NewRequest(http.MethodPut, urlDescuento, nil)

	client := &http.Client{}
	respUpdate, err := client.Do(reqUpdate)

	if err != nil || respUpdate.StatusCode != http.StatusOK {
		http.Error(w, "No se pudo actualizar el stock del producto", http.StatusInternalServerError)
		return
	}
	defer respUpdate.Body.Close()
	
	// 4. Crear y guardar el pedido en PostgreSQL
    precioTotal := producto.Precio * float64(req.Cantidad)    

    pedido := models.Pedido{        
        ClienteID:     req.ClienteID,
        NombreCliente: cliente.Nombre,
        ProductoID:    req.ProductoID,
        NombreProd:    producto.Nombre,
        Cantidad:      req.Cantidad,
        PrecioTotal:   precioTotal,
        Fecha:         time.Now(),
    }

    // Insertar con RETURNING id para capturar el ID de la BD (1, 2, 3...)
	queryInsert := `
		INSERT INTO pedidos (cliente_id, nombre_cliente, producto_id, nombre_prod, cantidad, precio_total, fecha)
		VALUES ($1, $2, $3, $4, $5, $6, $7)
		RETURNING id`

    errDB := db.DB.QueryRow(
		queryInsert,
		pedido.ClienteID,
		pedido.NombreCliente,
		pedido.ProductoID,
		pedido.NombreProd,
		pedido.Cantidad,
		pedido.PrecioTotal,
		pedido.Fecha,
	).Scan(&pedido.ID) 

    if errDB != nil {
		log.Printf("Error al insertar pedido en BD: %v\n", errDB)
		http.Error(w, "Error al guardar el pedido en la base de datos", http.StatusInternalServerError)
		return
	}

	// 5. Emitir Evento Asíncrono hacia RabbitMQ
	go func() {
		evento := messaging.EventoPedidoCreado{
			PedidoID:      pedido.ID,
			ClienteID:     req.ClienteID,
			EmailCliente:  cliente.Email,
			NombreCliente: cliente.Nombre,
			ProductoID:    req.ProductoID,
			Cantidad:      req.Cantidad,
			Total:         precioTotal,
		}
		messaging.PublicarPedidoCreado(evento)
	}()

	// 6. Responder inmediatamente al cliente con el pedido creado
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusCreated)
	json.NewEncoder(w).Encode(pedido)
}

func ObtenerPedidosHandler(w http.ResponseWriter, r *http.Request) {
    if r.Method != http.MethodGet {
        http.Error(w, "Método no permitido", http.StatusMethodNotAllowed)
        return
    }

    // Consultar todos los pedidos desde PostgreSQL
    querySelect := `
        SELECT id, cliente_id, nombre_cliente, producto_id, nombre_prod, cantidad, precio_total, fecha 
        FROM pedidos
    `
    rows, err := db.DB.Query(querySelect)
    if err != nil {
        log.Println("Error SQL en ObtenerPedidosHandler:", err) 
        http.Error(w, "Error al consultar los pedidos de la base de datos", http.StatusInternalServerError)
        return        
    }
    defer rows.Close()

    pedidosList := make([]models.Pedido, 0)

    for rows.Next() {
        var p models.Pedido
        err := rows.Scan(
            &p.ID, 
            &p.ClienteID, 
            &p.NombreCliente, 
            &p.ProductoID, 
            &p.NombreProd, 
            &p.Cantidad, 
            &p.PrecioTotal, 
            &p.Fecha,
        )
        if err != nil {
            continue
        }
        pedidosList = append(pedidosList, p)
    }

    w.Header().Set("Content-Type", "application/json")
    json.NewEncoder(w).Encode(pedidosList)
}

func obtenerEnv(clave, valorPorDefecto string) string {
	if valor := os.Getenv(clave); valor != "" {
		return valor
	}
	return valorPorDefecto
}

func ObtenerPedidosPorClienteHandler(w http.ResponseWriter, r *http.Request) {
	if r.Method != http.MethodGet {
		http.Error(w, "Método no permitido", http.StatusMethodNotAllowed)
		return
	}

	// Extrae el cliente_id removiendo el prefijo "/cliente/" de la URL
	path := r.URL.Path
	prefix := "/cliente/"
	if len(path) <= len(prefix) {
		http.Error(w, "ID de cliente no proporcionado", http.StatusBadRequest)
		return
	}
	clienteID := path[len(prefix):]

	querySelect := `
		SELECT id, cliente_id, nombre_cliente, producto_id, nombre_prod, cantidad, precio_total, fecha 
		FROM pedidos
		WHERE cliente_id = $1
		ORDER BY fecha DESC
	`
	rows, err := db.DB.Query(querySelect, clienteID)
	if err != nil {
		log.Println("Error SQL en ObtenerPedidosPorClienteHandler:", err)
		http.Error(w, "Error al consultar los pedidos de la base de datos", http.StatusInternalServerError)
		return
	}
	defer rows.Close()

	pedidosList := make([]models.Pedido, 0)

	for rows.Next() {
		var p models.Pedido
		err := rows.Scan(
			&p.ID,
			&p.ClienteID,
			&p.NombreCliente,
			&p.ProductoID,
			&p.NombreProd,
			&p.Cantidad,
			&p.PrecioTotal,
			&p.Fecha,
		)
		if err != nil {
			continue
		}
		pedidosList = append(pedidosList, p)
	}

	w.Header().Set("Content-Type", "application/json")
	json.NewEncoder(w).Encode(pedidosList)
}