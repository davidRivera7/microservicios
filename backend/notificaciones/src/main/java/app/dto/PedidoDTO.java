package app.dto;

// Importa la anotación que vincula el nombre de la clave en el JSON de Go con la variable en Java
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO (Data Transfer Object) para mapear los eventos de pedidos recibidos desde Go.
 */
public class PedidoDTO {

    // Mapea "pedido_id" del JSON al campo pedidoId
    @JsonProperty("pedido_id")
    private int pedidoId;

    // Mapea "cliente_id" del JSON al campo clienteId
    @JsonProperty("cliente_id")
    private String clienteId;

    // Mapea "email_cliente" del JSON al campo emailCliente
    @JsonProperty("email_cliente")
    private String emailCliente;

    // Mapea "nombre_cliente" del JSON al campo nombreCliente
    @JsonProperty("nombre_cliente")
    private String nombreCliente;

    // Mapea "producto_id" del JSON al campo productoId
    @JsonProperty("producto_id")
    private String productoId;

    // Mapea "cantidad" del JSON al campo cantidad
    @JsonProperty("cantidad")
    private int cantidad;

    // Mapea "total" del JSON al campo total
    @JsonProperty("total")
    private double total;

    // Mapea "nombre_producto" del JSON al campo nombreProducto
    @JsonProperty("nombre_producto")
    private String nombreProducto;

    // --- MÉTODOS GETTER (Solo lectura) ---

    // Retorna el ID único del pedido
    public int getPedidoId() { 
        return pedidoId; 
    }

    // Retorna el ID interno del cliente
    public String getClienteId() { 
        return clienteId; 
    }

    // Retorna el correo del cliente para enviar la notificación
    public String getEmailCliente() { 
        return emailCliente; 
    }

    // Retorna el nombre completo del cliente
    public String getNombreCliente() { 
        return nombreCliente; 
    }

    // Retorna el ID del producto comprado
    public String getProductoId() { 
        return productoId; 
    }

    // Retorna las unidades compradas
    public int getCantidad() { 
        return cantidad; 
    }

    // Retorna el importe total del pedido
    public double getTotal() { 
        return total; 
    }

    // Retorna el nombre del producto comprado
    public String getNombreProducto() {
        return nombreProducto;
    }
}