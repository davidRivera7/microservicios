// logicUI.js

// ======================================================
// 0. NAVEGACIÓN Y AUTENTICACIÓN
// ======================================================

function verificarAccesoPedidos() {
    const isLoggedIn = sessionStorage.getItem('isLoggedIn') === 'true';
    if (!isLoggedIn) {
        alert("Acceso restringido: Debes iniciar sesión en Productos antes de acceder al módulo de Pedidos.");
        navegarA('productos');
        return false;
    }
    return true;
}

function cerrarSesion() {
    sessionStorage.removeItem('isLoggedIn');
    sessionStorage.removeItem('authenticatedUser');
    navegarA('productos');
}

function navegarA(destino) {
    if (destino === 'productos') {
        if (window.location.pathname.startsWith('/pedidos')) {
            window.location.href = '/productos/';
        } else {
            window.location.href = '../Productos/index.html';
        }
    } else if (destino === 'pedidos') {
        if (sessionStorage.getItem('isLoggedIn') !== 'true') {
            alert("Acceso restringido: Debes iniciar sesión primero.");
            navegarA('productos');
            return;
        }
        if (window.location.pathname.startsWith('/productos')) {
            window.location.href = '/pedidos/';
        } else {
            window.location.href = '../PedidosAtendidos/index.html';
        }
    }
}

// ======================================================
// 1. INICIAR LA CARGA DE PEDIDOS (CON PROTECCIÓN)
// ======================================================

document.addEventListener("DOMContentLoaded", () => {
    if (verificarAccesoPedidos()) {
        cargarPedidos();
    }
});

// ======================================================
// 2. OBTENER LOS PEDIDOS DESDE EL SERVICIO DE GO
// ======================================================

async function cargarPedidos() {
    try {
        const respuesta = await fetch("/api/pedidos");

        if (!respuesta.ok) {
            throw new Error(`Error HTTP: ${respuesta.status}`);
        }

        const pedidos = await respuesta.json();
        renderizarTabla(pedidos);
    } catch (error) {
        console.error("Error al cargar pedidos desde Go:", error);

        // Si estamos probando vía file:// o sin conexión al backend de Go
        if (window.location.protocol === 'file:') {
            renderizarTabla([
                { id: 101, cliente_id: 1, nombre_cliente: "Juan Pérez (Ejemplo Local)", producto_id: 2, nombre_producto: "Monitor 24 pulgadas", cantidad: 1, precio_total: 149.50 },
                { id: 102, cliente_id: 2, nombre_cliente: "María Gómez (Ejemplo Local)", producto_id: 1, nombre_producto: "Laptop HP ProBook", cantidad: 2, precio_total: 1599.98 }
            ]);
        }
    }
}

// ======================================================
// 3. MOSTRAR LOS PEDIDOS EN LA TABLA HTML
// ======================================================

function renderizarTabla(pedidos) {
    const tbody = document.querySelector(".tabla-pedidos tbody");

    if (!tbody) {
        console.error("No se encontró el tbody de la tabla de pedidos");
        return;
    }

    tbody.innerHTML = "";

    if (!pedidos || pedidos.length === 0) {
        tbody.innerHTML =
            '<tr><td colspan="6" style="text-align: center; color: #7f8c8d; padding: 20px;">No hay pedidos pendientes por atender.</td></tr>';
        return;
    }

    pedidos.forEach((pedido) => {
        const fila = document.createElement("tr");

        fila.innerHTML = `
            <td><strong>#${pedido.id}</strong></td>
            <td>${escapeHtml(pedido.nombre_cliente || "Sin nombre")}</td>
            <td>${escapeHtml(pedido.nombre_producto || "Sin producto")}</td>
            <td>${pedido.cantidad}</td>
            <td>$${Number(pedido.precio_total || 0).toFixed(2)}</td>
            <td style="text-align: center;">
                <button class="btn-atender">Atender Pedido</button>
            </td>
        `;

        const botonAtender = fila.querySelector(".btn-atender");
        botonAtender.addEventListener("click", () =>
            atenderPedido(pedido, fila),
        );

        tbody.appendChild(fila);
    });
}

// ======================================================
// 4. ATENDER UN PEDIDO
// ======================================================

async function atenderPedido(pedido, filaHTML) {
    const confirmado = confirm(
        `¿Deseas atender y procesar el pedido #${pedido.id}?`,
    );

    if (!confirmado) return;

    if (window.location.protocol === 'file:') {
        filaHTML.remove();
        alert(`Pedido #${pedido.id} atendido exitosamente en modo prueba local.`);
        return;
    }

    try {
        console.log("Consultando cliente:", pedido.cliente_id);

        const respuestaCliente = await fetch(
            `/api/Clientes/ObtenerPorID/${encodeURIComponent(pedido.cliente_id)}`,
        );

        if (!respuestaCliente.ok) {
            throw new Error(
                `No se pudo obtener el cliente: HTTP ${respuestaCliente.status}`,
            );
        }

        const cliente = await respuestaCliente.json();

        if (!cliente.email) {
            throw new Error("El cliente no tiene un correo disponible");
        }

        const emailCliente = cliente.email;

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

        const respuesta = await fetch("/api/pedidos", {
            method: "DELETE",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(eventoRabbit),
        });

        if (!respuesta.ok) {
            const errorTexto = await respuesta.text();
            throw new Error(errorTexto || `Error HTTP: ${respuesta.status}`);
        }

        filaHTML.remove();
        alert(`Pedido #${pedido.id} enviado exitosamente a RabbitMQ.`);
    } catch (error) {
        console.error("Error al atender el pedido:", error);
        alert(`No se pudo procesar el pedido: ${error.message}`);
    }
}

function escapeHtml(text) {
    if (!text) return '';
    return String(text)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
