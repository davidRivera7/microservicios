// logicUI.js

// ======================================================
// 1. INICIAR LA CARGA DE PEDIDOS
// ======================================================

// Cuando el HTML termine de cargarse, ejecutamos cargarPedidos().
document.addEventListener("DOMContentLoaded", cargarPedidos);

// ======================================================
// 2. OBTENER LOS PEDIDOS DESDE EL SERVICIO DE GO
// ======================================================

async function cargarPedidos() {
    try {
        // Consultar la API de Go para obtener todos los pedidos
        const respuesta = await fetch("http://localhost:8082/pedidos");

        // Si el servidor responde con un error HTTP, lo reportamos.
        if (!respuesta.ok) {
            throw new Error(`Error HTTP: ${respuesta.status}`);
        }

        // Convertimos la respuesta JSON en un arreglo de pedidos.
        const pedidos = await respuesta.json();

        // Enviamos los pedidos a la función que dibuja la tabla.
        renderizarTabla(pedidos);
    } catch (error) {
        // Mostramos errores de conexión o de respuesta del servidor.
        console.error("Error al cargar pedidos desde Go:", error);
    }
}

// ======================================================
// 3. MOSTRAR LOS PEDIDOS EN LA TABLA HTML
// ======================================================

function renderizarTabla(pedidos) {
    // Buscamos el cuerpo de la tabla donde se mostrarán las filas.
    const tbody = document.querySelector(".tabla-pedidos tbody");

    // Comprobamos que la tabla exista en el HTML.
    if (!tbody) {
        console.error("No se encontró el tbody de la tabla de pedidos");
        return;
    }

    // Eliminamos las filas anteriores para evitar duplicados.
    tbody.innerHTML = "";

    // Si no hay pedidos, mostramos un mensaje.
    if (!pedidos || pedidos.length === 0) {
        tbody.innerHTML =
            '<tr><td colspan="6">No hay pedidos pendientes</td></tr>';
        return;
    }

    // Recorremos el arreglo para crear una fila por cada pedido.
    pedidos.forEach((pedido) => {
        // Creamos una nueva fila HTML.
        const fila = document.createElement("tr");

        // Mostramos los datos que ya están guardados en el pedido.
        // El correo no se muestra en la tabla.
        fila.innerHTML = `
            <td>${pedido.id}</td>
            <td>${pedido.nombre_cliente || "Sin nombre"}</td>
            <td>${pedido.nombre_producto || "Sin producto"}</td>
            <td>${pedido.cantidad}</td>
            <td>$${Number(pedido.precio_total || 0).toFixed(2)}</td>
            <td>
                <button class="btn-atender">Atender Pedido</button>
            </td>
        `;

        // Buscamos el botón que acabamos de crear.
        const botonAtender = fila.querySelector(".btn-atender");

        // Al presionar el botón, enviamos el pedido a atenderPedido().
        botonAtender.addEventListener("click", () =>
            atenderPedido(pedido, fila),
        );

        // Agregamos la fila terminada al cuerpo de la tabla.
        tbody.appendChild(fila);
    });
}

// ======================================================
// 4. ATENDER UN PEDIDO
// ======================================================

async function atenderPedido(pedido, filaHTML) {
    // Pedimos confirmación antes de procesar el pedido.
    const confirmado = confirm(
        `¿Deseas atender y procesar el pedido #${pedido.id}?`,
    );

    // Si el usuario cancela, no hacemos ninguna petición.
    if (!confirmado) return;

    try {
        // --------------------------------------------------
        // 4.1. CONSULTAR EL CORREO DEL CLIENTE
        // --------------------------------------------------
        console.log("Consultando cliente:", pedido.cliente_id);

        // Consultar la API de clientes usando el ID del pedido.
        // El endpoint recibe el ID del cliente al final de la URL.
        const respuestaCliente = await fetch(
            `http://localhost:8083/api/Clientes/ObtenerPorID/${encodeURIComponent(pedido.cliente_id)}`,
        );

        // Si la consulta falla, detenemos el proceso.
        if (!respuestaCliente.ok) {
            throw new Error(
                `No se pudo obtener el cliente: HTTP ${respuestaCliente.status}`,
            );
        }

        // Convertimos la respuesta del servicio de clientes a JSON.
        const cliente = await respuestaCliente.json();

        console.log("Cliente recibido:", cliente);

        // Comprobamos que exista un correo para la notificación.
        if (!cliente.email) {
            throw new Error("El cliente no tiene un correo disponible");
        }

        // Obtenemos el correo del cliente.
        const emailCliente = cliente.email;

        // --------------------------------------------------
        // 4.2. CONSTRUIR EL EVENTO PARA RABBITMQ
        // --------------------------------------------------

        // Preparamos los campos que espera el backend de Go
        // para publicar el evento de pedido creado.
        const eventoRabbit = {
            pedido_id: pedido.id,
            cliente_id: pedido.cliente_id,
            email_cliente: emailCliente,
            nombre_cliente: pedido.nombre_cliente,
            producto_id: pedido.producto_id,
            nombre_producto: pedido.nombre_producto,
            cantidad: pedido.cantidad,
            total: pedido.precio_total,
        };

        console.log("Enviando evento a Go:", eventoRabbit);
        // --------------------------------------------------
        // 4.3. ENVIAR EL EVENTO AL BACKEND DE GO
        // --------------------------------------------------

        // Enviamos el objeto JSON al endpoint /pedidos usando DELETE.
        // El backend de Go debe recibirlo y publicarlo en RabbitMQ.
        const respuesta = await fetch("/pedidos", {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(eventoRabbit),
        });

        // Si Go devuelve un error, lo mostramos.
        if (!respuesta.ok) {
            const errorTexto = await respuesta.text();
            throw new Error(errorTexto || `Error HTTP: ${respuesta.status}`);
        }

        // --------------------------------------------------
        // 4.4. ACTUALIZAR LA INTERFAZ
        // --------------------------------------------------

        // Quitamos la fila únicamente si el backend confirma el éxito.
        filaHTML.remove();

        // Informamos al usuario que Go aceptó el evento.
        alert(`Pedido #${pedido.id} enviado exitosamente a RabbitMQ.`);
    } catch (error) {
        // Registramos el detalle técnico en la consola.
        console.error("Error al atender el pedido:", error);

        // Informamos del problema sin eliminar el pedido de la tabla.
        alert(`No se pudo procesar el pedido: ${error.message}`);
    }
}
