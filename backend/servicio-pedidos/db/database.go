package db

import (
	"database/sql"
	"fmt"
	"log"
	"os"
	"time"

	_ "github.com/lib/pq"
)

var DB *sql.DB

func ConectarBD() {
	host := os.Getenv("DB_HOST")
	port := os.Getenv("DB_PORT")
	user := os.Getenv("DB_USER")
	password := os.Getenv("DB_PASSWORD")
	dbname := os.Getenv("DB_NAME")

	dsn := fmt.Sprintf("host=%s port=%s user=%s password=%s dbname=%s sslmode=disable",
		host, port, user, password, dbname)

	var err error
	DB, err = sql.Open("postgres", dsn)
	if err != nil {
		log.Fatalf("Error al abrir la conexión: %v", err)
	}

	// Reintentar conexión durante 30 segundos si la BD está iniciando
	for i := 1; i <= 10; i++ {
		err = DB.Ping()
		if err == nil {
			break
		}
		fmt.Printf("Esperando a la base de datos... (intento %d/10)\n", i)
		time.Sleep(3 * time.Second)
	}

	if err != nil {
		log.Fatalf("No se pudo conectar a la base de datos tras varios intentos: %v", err)
	}

	fmt.Println("¡Conexión exitosa a la Base de Datos!")
	CrearTabla()
}

func CrearTabla() {
    query := `
    CREATE TABLE IF NOT EXISTS pedidos (
        id SERIAL PRIMARY KEY,
        cliente_id VARCHAR(50) NOT NULL,
        nombre_cliente VARCHAR(100) NOT NULL,
        producto_id VARCHAR(50) NOT NULL,
        nombre_prod VARCHAR(100) NOT NULL,
        cantidad INT NOT NULL,
        precio_total NUMERIC(10, 2) NOT NULL,
        fecha TIMESTAMP NOT NULL
    );`

    _, err := DB.Exec(query)
    if err != nil {
        log.Fatalf("Error al crear la tabla pedidos: %v", err)
    }
}