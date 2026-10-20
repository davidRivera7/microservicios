package app;

// Anotación que combina @Configuration, @EnableAutoConfiguration y @ComponentScan.
// Activa el escaneo de componentes dentro del paquete 'app' y la auto-configuración del entorno.
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Clase principal utilizada para lanzar y arrancar la aplicación Spring desde el método main.
import org.springframework.boot.SpringApplication;

/**
 * Clase principal de entrada al microservicio de Notificaciones.
 * La anotación @SpringBootApplication le indica a Spring que esta clase es el centro
 * de configuración y activación del microservicio.
 */
@SpringBootApplication
public class App {

    /**
     * Punto de entrada principal para la ejecución de la aplicación en la máquina virtual de Java (JVM).
     * 
     * @param args Argumentos de la línea de comandos pasados al iniciar el proceso.
     */
    public static void main(String[] args) {
        // Inicializa el contenedor de Spring Boot, procesa la configuración y levanta el servidor en el puerto 8084.
        SpringApplication.run(App.class, args);
    }

}