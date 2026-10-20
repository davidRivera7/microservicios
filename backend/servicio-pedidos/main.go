package main

import (
	"fmt"
	"log"
	"net/http"

	"servicio-pedidos/db"
	"servicio-pedidos/handlers"
)

func main() {
	db.ConectarBD()

	// Servir archivos estáticos (tu página web HTML/CSS/JS)
    fs := http.FileServer(http.Dir("/frontend/PedidosAtendidos"))
    http.Handle("/", fs)

	// Handler para /pedidos (POST para crear, GET para listar todos)
	http.HandleFunc("/pedidos", func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodPost:
			handlers.CrearPedidoHandler(w, r)
		case http.MethodGet:
			handlers.ObtenerPedidosHandler(w, r)
		case http.MethodDelete:
        	handlers.ProcesarPedidoRabbitHandler(w, r)
		default:
			http.Error(w, "Método no permitido", http.StatusMethodNotAllowed)
		}
	})

	// Handler para /cliente/{id} (GET para historial de un cliente específico)
	http.HandleFunc("/cliente/", func(w http.ResponseWriter, r *http.Request) {
		if r.Method == http.MethodGet {
			handlers.ObtenerPedidosPorClienteHandler(w, r)
		} else {
			http.Error(w, "Método no permitido", http.StatusMethodNotAllowed)
		}
	})

	puerto := ":8082"
	fmt.Printf("Servicio de Pedidos ejecutándose en el puerto %s...\n", puerto)
	log.Fatal(http.ListenAndServe(puerto, nil))
}