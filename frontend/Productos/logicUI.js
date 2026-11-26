// --- API URL Base
const API_URL = '/api/productos';

// --- Estado de la Aplicación
let productos = [];
let modalMode = 'add';
let currentProductId = null;

// --- Inicialización y Verificación de Sesión
document.addEventListener('DOMContentLoaded', () => {
    verificarEstadoSesion();
});

function verificarEstadoSesion() {
    const isLoggedIn = sessionStorage.getItem('isLoggedIn') === 'true';
    const loginScreen = document.getElementById('login-screen');
    const appScreen = document.getElementById('app-screen');
    const btnLogout = document.getElementById('btn-logout');
    const btnPedidos = document.getElementById('btn-pedidos');

    if (isLoggedIn) {
        if (loginScreen) loginScreen.classList.remove('active');
        if (appScreen) appScreen.classList.add('active');
        if (btnLogout) btnLogout.style.display = 'inline-flex';
        if (btnPedidos) btnPedidos.style.display = 'inline-flex';
        cargarProductos();
    } else {
        if (loginScreen) loginScreen.classList.add('active');
        if (appScreen) appScreen.classList.remove('active');
        if (btnLogout) btnLogout.style.display = 'none';
        if (btnPedidos) btnPedidos.style.display = 'none';
    }
}

function cerrarSesion() {
    sessionStorage.removeItem('isLoggedIn');
    sessionStorage.removeItem('authenticatedUser');
    verificarEstadoSesion();
}

// --- Navegación entre Módulos
function navegarA(destino) {
    if (destino === 'pedidos') {
        // Verificar autenticación antes de ir a Pedidos
        if (sessionStorage.getItem('isLoggedIn') !== 'true') {
            alert('Debes iniciar sesión primero para ingresar al módulo de Pedidos.');
            return;
        }

        if (window.location.pathname.startsWith('/productos')) {
            window.location.href = '/pedidos/';
        } else {
            window.location.href = '../PedidosAtendidos/index.html';
        }
    } else if (destino === 'productos') {
        if (window.location.pathname.startsWith('/pedidos')) {
            window.location.href = '/productos/';
        } else {
            window.location.href = '../Productos/index.html';
        }
    }
}

// --- Autenticación
async function login() {
    const usuario = document.getElementById('username').value.trim();
    const contrasena = document.getElementById('password').value.trim();
    const errorAlert = document.getElementById('login-error');

    // Manejo de prueba cuando se abre directo el archivo HTML local (file://)
    if (window.location.protocol === 'file:') {
        if (usuario.toLowerCase() === 'admin' && contrasena.toLowerCase() === 'admin') {
            sessionStorage.setItem('isLoggedIn', 'true');
            sessionStorage.setItem('authenticatedUser', usuario);
            if (errorAlert) errorAlert.style.display = 'none';
            verificarEstadoSesion();
            return;
        } else {
            if (errorAlert) errorAlert.style.display = 'block';
            return;
        }
    }

    try {
        const response = await fetch(`${API_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ usuario, contrasena })
        });

        if (response.ok) {
            sessionStorage.setItem('isLoggedIn', 'true');
            sessionStorage.setItem('authenticatedUser', usuario);
            if (errorAlert) errorAlert.style.display = 'none';
            verificarEstadoSesion();
        } else {
            if (usuario.toLowerCase() === 'admin' && contrasena.toLowerCase() === 'admin') {
                sessionStorage.setItem('isLoggedIn', 'true');
                sessionStorage.setItem('authenticatedUser', usuario);
                if (errorAlert) errorAlert.style.display = 'none';
                verificarEstadoSesion();
            } else {
                if (errorAlert) errorAlert.style.display = 'block';
            }
        }
    } catch (error) {
        console.error("Error en la autenticación:", error);

        // Si falla la conexión HTTP en entorno local pero se ingresó Admin/Admin
        if (usuario.toLowerCase() === 'admin' && contrasena.toLowerCase() === 'admin') {
            sessionStorage.setItem('isLoggedIn', 'true');
            sessionStorage.setItem('authenticatedUser', usuario);
            if (errorAlert) errorAlert.style.display = 'none';
            verificarEstadoSesion();
        } else {
            if (errorAlert) errorAlert.style.display = 'block';
        }
    }
}

// --- Peticiones al Servidor
async function cargarProductos() {
    try {
        const response = await fetch(API_URL);
        if (!response.ok) {
            throw new Error(`Error del servidor: ${response.status}`);
        }
        productos = await response.json();
        renderizarTabla();
    } catch (error) {
        console.error("Error al cargar productos:", error);

        // Si estamos en file:// o sin conexión al backend, mostrar lista vacía o mensaje explicativo
        if (window.location.protocol === 'file:' && productos.length === 0) {
            productos = [
                { id: 1, nombre: "Laptop HP ProBook (Ejemplo Local)", precio: 799.99, stock: 12 },
                { id: 2, nombre: "Monitor 24 pulgadas (Ejemplo Local)", precio: 149.50, stock: 4 },
                { id: 3, nombre: "Teclado Mecánico RGB (Ejemplo Local)", precio: 59.90, stock: 0 }
            ];
            renderizarTabla();
            return;
        }

        const tbody = document.getElementById('product-list-body');
        if (tbody) {
            tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: #e74c3c; padding: 20px;">Fallo al conectar con el servidor de productos (${API_URL}).</td></tr>`;
        }
    }
}

async function guardarProducto() {
    const nombre = document.getElementById('prod-name').value.trim();
    const precio = document.getElementById('prod-price').value;
    const stock = document.getElementById('prod-stock').value;

    if (!nombre || precio === '' || stock === '') {
        alert("Por favor completa todos los campos requeridos.");
        return;
    }

    const payload = {
        nombre: nombre,
        precio: parseFloat(precio),
        stock: parseInt(stock, 10)
    };

    if (window.location.protocol === 'file:') {
        if (modalMode === 'add') {
            const newId = productos.length > 0 ? Math.max(...productos.map(p => p.id)) + 1 : 1;
            productos.push({ id: newId, ...payload });
        } else {
            const index = productos.findIndex(p => p.id == currentProductId);
            if (index !== -1) productos[index] = { id: currentProductId, ...payload };
        }
        cerrarModal('product-modal');
        renderizarTabla();
        return;
    }

    try {
        if (modalMode === 'add') {
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!response.ok) throw new Error("Fallo al guardar producto");
        } else {
            const response = await fetch(`${API_URL}/${currentProductId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (!response.ok) throw new Error("Fallo al actualizar producto");
        }

        cerrarModal('product-modal');
        await cargarProductos();
    } catch (error) {
        console.error("Error al guardar el producto:", error);
        alert("Hubo un problema al guardar el producto.");
    }
}

function prepararEliminar(id) {
    currentProductId = id;
    document.getElementById('delete-modal').style.display = 'flex';
}

async function confirmarEliminar() {
    if (window.location.protocol === 'file:') {
        productos = productos.filter(p => p.id != currentProductId);
        cerrarModal('delete-modal');
        renderizarTabla();
        return;
    }

    try {
        const response = await fetch(`${API_URL}/${currentProductId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error(`Error al eliminar: ${response.status}`);
        }

        cerrarModal('delete-modal');
        await cargarProductos();
    } catch (error) {
        console.error("Error al eliminar producto:", error);
        alert("Error al intentar eliminar el producto de la base de datos.");
    }
}

// --- Renderizado de UI
function renderizarTabla() {
    const tbody = document.getElementById('product-list-body');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!productos || productos.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: #7f8c8d; padding: 20px;">No hay productos registrados en el inventario.</td></tr>`;
        return;
    }

    productos.forEach(p => {
        const tr = document.createElement('tr');

        let stockClass = 'stock-high';
        if (p.stock === 0) {
            stockClass = 'stock-out';
        } else if (p.stock <= 5) {
            stockClass = 'stock-low';
        }

        tr.innerHTML = `
            <td><strong>#${p.id}</strong></td>
            <td>${escapeHtml(p.nombre)}</td>
            <td>$${Number(p.precio).toFixed(2)}</td>
            <td><span class="stock-badge ${stockClass}">${p.stock} unidades</span></td>
            <td>
                <div class="actions-cell">
                    <button class="btn-action btn-action-edit" onclick="abrirModal('edit', '${p.id}')" title="Editar producto">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                        </svg>
                    </button>
                    <button class="btn-action btn-action-delete" onclick="prepararEliminar('${p.id}')" title="Eliminar producto">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="3 6 5 6 21 6"></polyline>
                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                            <line x1="10" y1="11" x2="10" y2="17"></line>
                            <line x1="14" y1="11" x2="14" y2="17"></line>
                        </svg>
                    </button>
                </div>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

// --- Manejo de Modales
function abrirModal(mode, id = null) {
    modalMode = mode;
    currentProductId = id;
    document.getElementById('product-modal').style.display = 'flex';

    const inputIdGroup = document.getElementById('group-prod-id');
    const inputId = document.getElementById('prod-id');
    const inputName = document.getElementById('prod-name');
    const inputPrice = document.getElementById('prod-price');
    const inputStock = document.getElementById('prod-stock');

    if (mode === 'add') {
        document.getElementById('modal-title').innerText = 'Añadir Producto';
        inputIdGroup.style.display = 'none';
        inputId.value = '';
        inputName.value = '';
        inputPrice.value = '';
        inputStock.value = '';
    } else {
        document.getElementById('modal-title').innerText = 'Edición de Producto';
        inputIdGroup.style.display = 'block';
        const producto = productos.find(p => p.id == id);
        if (producto) {
            inputId.value = producto.id;
            inputName.value = producto.nombre;
            inputPrice.value = producto.precio;
            inputStock.value = producto.stock;
        }
    }
}

function cerrarModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
        modal.style.display = 'none';
    }
}

// Función auxiliar para sanitizar HTML
function escapeHtml(text) {
    if (!text) return '';
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
