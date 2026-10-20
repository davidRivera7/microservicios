package app;

// Importa nuestra clase DTO que contiene la estructura del pedido
import app.dto.PedidoDTO;

// Importa la clase de servicio que se encargará del envío real del email
import app.service.EmailService;

// Importa la herramienta de Jackson necesaria para deserializar cadenas JSON a Objetos Java
import com.fasterxml.jackson.databind.ObjectMapper;

// Importa la anotación para suscribir un método a una cola específica de RabbitMQ
import org.springframework.amqp.rabbit.annotation.RabbitListener;

// Importa la anotación para inyectar automáticamente dependencias administradas por Spring
import org.springframework.beans.factory.annotation.Autowired;

// Importa la anotación que registra esta clase como un Bean de Spring en el contexto de la app
import org.springframework.stereotype.Component;

/**
 * Escucha la cola de RabbitMQ y gestiona la conversión de mensajes a DTO.
 */
@Component // Le indica a Spring Boot que instancie y gestione esta clase automáticamente
public class NotificacionListener {

    // Inyecta el servicio de email para delegarle la tarea de envío
    @Autowired
    private EmailService emailService;

    // Instancia de Jackson para procesar lecturas de datos JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Método 1: Escucha la cola 'pedidos_creados' de RabbitMQ.
     * Recibe la cadena de texto cruda (JSON) de la cola y la redirige para procesar.
     */
    @RabbitListener(queues = "pedidos_creados") // Reacciona a cada nuevo mensaje en la cola
    public void recibirNotificacion(String mensajeJson) {
        // Pasa el mensaje JSON directamente al método de procesamiento
        procesarYEnviar(mensajeJson);
    }

    /**
     * Método 2: Transforma el JSON a un objeto PedidoDTO y llama a EmailService.
     */
    public void procesarYEnviar(String mensajeJson) {
        try {
            // Convierte el texto JSON en una instancia de la clase PedidoDTO
            PedidoDTO pedido = objectMapper.readValue(mensajeJson, PedidoDTO.class);

            // Envía la instancia deserializada a la capa de servicio de correo
            emailService.enviarCorreoPedido(pedido);

        } catch (Exception e) {
            // Muestra un error descriptivo si el JSON viene malformado o falla la lectura
            System.err.println("❌ Error al convertir el JSON a objeto Java: " + e.getMessage());
        }
    }
}