// logicUI.js

// 1. Cargar los pedidos tan pronto como el HTML esté listo
document.addEventListener('DOMContentLoaded', cargarPedidos);

async function cargarPedidos() {
    try {
        // Hacemos la petición GET a la API existente en Go
        const respuesta = await fetch('/pedidos');
        
        if (!respuesta.ok) {
            throw new Error(`Error HTTP: ${respuesta.status}`);
        }
        
        const pedidos = await respuesta.json();
        renderizarTabla(pedidos);
    } catch (error) {
        console.error('Error al cargar pedidos desde Go:', error);
    }
}

// 2. Dibujar las filas de la tabla con los campos exactos del JSON de Go
function renderizarTabla(pedidos) {
    const tbody = document.querySelector('.tabla-pedidos tbody');
    tbody.innerHTML = ''; // Limpiar contenido previo

    if (!pedidos || pedidos.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7">No hay pedidos pendientes</td></tr>';
        return;
    }

    pedidos.forEach(pedido => {
        const fila = document.createElement('tr');

        // Insertar celdas con las propiedades del JSON que devuelve Go
        fila.innerHTML = `
            <td>${pedido.id}</td>
            <td>${pedido.nombre_cliente}</td>
            <td>${pedido.email_cliente || 'Sin email'}</td>
            <td>${pedido.producto_id}</td>
            <td>${pedido.cantidad}</td>
            <td>$${pedido.precio_total ? pedido.precio_total.toFixed(2) : '0.00'}</td>
            <td>
                <button class="btn-atender">Atender Pedido</button>
            </td>
        `;

        // 3. Asignar el listener al botón para atender el pedido
        const botonAtender = fila.querySelector('.btn-atender');
        botonAtender.addEventListener('click', () => atenderPedido(pedido, fila));

        tbody.appendChild(fila);
    });
}

// 4. Función de atención del pedido
async function atenderPedido(pedido, filaHTML) {
    const confirmado = confirm(`¿Deseas atender y procesar el pedido #${pedido.id}?`);
    
    if (!confirmado) return;

    // A. Mapeamos el objeto con las claves exactas que espera EventoPedidoCreado en Go
    const eventoRabbit = {
        pedido_id: pedido.id,
        cliente_id: pedido.cliente_id,
        email_cliente: pedido.email_cliente || "",
        nombre_cliente: pedido.nombre_cliente,
        producto_id: pedido.producto_id,
        cantidad: pedido.cantidad,
        total: pedido.precio_total
    };

    try {
        // B. Enviamos los datos al backend en Go
        const respuesta = await fetch('/pedidos', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(eventoRabbit)
        });

        if (respuesta.ok) {
            // C. Si Go logró publicar el mensaje en RabbitMQ, quitamos la fila de la interfaz
            filaHTML.remove();
            alert(`Pedido #${pedido.id} enviado exitosamente a RabbitMQ.`);
        } else {
            const errorTexto = await respuesta.text();
            alert(`Error al enviar a RabbitMQ: ${errorTexto}`);
        }

    } catch (error) {
        console.error("Error de red o comunicación con Go:", error);
        alert("Ocurrió un error al intentar comunicar con el servidor.");
    }
}

// Función para consultar e inyectar el email en cada pedido
async function agregarEmailAPedidos(pedidos) {
    return Promise.all(pedidos.map(async (pedido) => {
        // Si el JSON ya trae email_cliente, no hacemos la petición extra
        if (pedido.email_cliente) return pedido;

        try {
            const respuesta = await fetch(`/cliente/${pedido.cliente_id}`);
            if (respuesta.ok) {
                const cliente = await respuesta.json();
                // Asignamos la nueva propiedad email_cliente al objeto del pedido
                pedido.email_cliente = cliente.email;
            } else {
                pedido.email_cliente = 'Sin email';
            }
        } catch (error) {
            console.error(`Error al consultar el cliente ${pedido.cliente_id}:`, error);
            pedido.email_cliente = 'Sin email';
        }

        return pedido;
    }));
}