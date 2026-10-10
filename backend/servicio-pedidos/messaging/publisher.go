package messaging

import (
	"context"
	"encoding/json"
	"log"
	"os"
	"time"

	amqp "github.com/rabbitmq/amqp091-go"
)

type EventoPedidoCreado struct {
	PedidoID      int     `json:"pedido_id"`
	ClienteID     string  `json:"cliente_id"`
	EmailCliente  string  `json:"email_cliente"`
	NombreCliente string  `json:"nombre_cliente"`
	ProductoID    string  `json:"producto_id"`
	Cantidad      int     `json:"cantidad"`
	Total         float64 `json:"total"`
}

func ObtenerRabbitURL() string {
	url := os.Getenv("RABBITMQ_URL")
	if url == "" {		
		url = "amqp://guest:guest@rabbitmq:5672/"
	}
	return url
}

func PublicarPedidoCreado(evento EventoPedidoCreado) error {	
	conn, err := amqp.Dial(ObtenerRabbitURL())
	if err != nil {
		log.Printf("Error al conectar con RabbitMQ: %v", err)
		return err
	}
	defer conn.Close()

	ch, err := conn.Channel()
	if err != nil {
		return err
	}
	defer ch.Close()

	// Declarar cola "pedidos_creados"
	q, err := ch.QueueDeclare(
		"pedidos_creados", // nombre de la cola
		true,              // durable
		false,             // auto-delete
		false,             // exclusive
		false,             // no-wait
		nil,
	)
	if err != nil {
		return err
	}

	body, err := json.Marshal(evento)
	if err != nil {
		return err
	}

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	// Publicar mensaje
	err = ch.PublishWithContext(ctx,
		"",     // exchange
		q.Name, // routing key
		false,  // mandatory
		false,  // immediate
		amqp.Publishing{
			ContentType: "application/json",
			Body:        body,
		})

	if err != nil {
		log.Printf("Error publicando evento: %v", err)
		return err
	}

	log.Println("Evento PedidoCreado publicado exitosamente en RabbitMQ")
	return nil
}