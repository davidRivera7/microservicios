package app.service;

// Importa nuestra clase DTO que contiene la estructura de los datos del pedido
import app.dto.PedidoDTO;

// Importa la anotación de Spring para inyectar automáticamente dependencias (JavaMailSender)
import org.springframework.beans.factory.annotation.Autowired;

// Importa la clase de Spring Mail para construir mensajes sencillos de texto plano
import org.springframework.mail.SimpleMailMessage;

// Importa la interfaz principal de Spring que gestiona y envía correos por el servidor SMTP
import org.springframework.mail.javamail.JavaMailSender;

// Importa la anotación que registra esta clase como un servicio de lógica de negocio en Spring
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de gestionar la construcción y lógica de los correos
 * electrónicos.
 */
@Service // Registra esta clase como un Bean de servicio en Spring
public class EmailService {

    // Inyección de la herramienta de correo de Spring Boot
    @Autowired(required = false)
    private JavaMailSender mailSender;

    /**
     * Procesa la información del pedido y simula/envía la notificación.
     * 
     * @param pedido Objeto con la información del evento recibido desde RabbitMQ.
     */
    public void enviarCorreoPedido(PedidoDTO pedido) {

        // Construcción del contenido del correo
        String destinatario = pedido.getEmailCliente();
        String asunto = "Confirmación de Pedido #" + pedido.getPedidoId();

        String cuerpo = String.format(
                "Hola %s,\n\n" +
                        "¡Tu pedido #%d ha sido recibido con éxito!\n\n" +
                        "Detalles:\n" +
                        "- Producto: %s\n" +
                        "- Cantidad: %d\n" +
                        "- Total: $%.2f\n\n" +
                        "Gracias por tu compra.",
                pedido.getNombreCliente(),
                pedido.getPedidoId(),
                pedido.getProductoId(),
                pedido.getCantidad(),
                pedido.getTotal());

        // 3. Envío real a través de JavaMailSender
        try {
            if (mailSender != null) {
                SimpleMailMessage email = new SimpleMailMessage();
                email.setTo(destinatario);
                email.setSubject(asunto);
                email.setText(cuerpo);

                mailSender.send(email);
                System.out.println("Correo enviado con éxito a: " + destinatario);
            }
        } catch (Exception e) {
            System.err.println("Error al intentar enviar el correo a " + destinatario + ": " + e.getMessage());
        }
    }
}
