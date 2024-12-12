document.addEventListener('DOMContentLoaded', () => {
    const tableBody = document.getElementById('sales-table-body');
    const generateBoletaButton = document.getElementById('generate-boleta');
    const ventaIdInput = document.getElementById('venta-id');

    // Función para obtener todas las ventas del backend
    async function fetchSales() {
        try {
            const response = await fetch('/ventas');
            const sales = await response.json();
            return sales;
        } catch (error) {
            console.error('Error al obtener ventas:', error);
            return [];
        }
    }

    // Función para renderizar todas las ventas en la tabla
    function renderSales(sales) {
        tableBody.innerHTML = ''; // Limpiar tabla
        sales.forEach((sale, index) => {
            sale.id_venta = index + 1;  // Asignamos el id_venta (inicia desde 1)
            sale.details.forEach(detail => {
                const row = `
                    <tr>
                        <td>${sale.id_venta}</td>
                        <td>${sale.client_id}</td>
                        <td>${detail.product_id}</td>
                        <td>${detail.amount}</td>
                        <td>${detail.price}</td>
                        <td>${(detail.amount * detail.price).toFixed(2)}</td>
                    </tr>
                `;
                tableBody.insertAdjacentHTML('beforeend', row);
            });
        });
    }

    // Función para generar la boleta
    function generateBoleta(ventaId) {
        window.location.href = `/ventas/boleta/${ventaId}`; // Redirigir a la URL de boleta
    }

    // Inicializar la aplicación
    async function init() {
        const sales = await fetchSales(); // Obtener ventas desde el servidor
        renderSales(sales); // Renderizar ventas en la tabla

        // Manejar el evento de generar boleta
        generateBoletaButton.addEventListener('click', () => {
            const ventaId = parseInt(ventaIdInput.value, 10);
            if (isNaN(ventaId) || ventaId < 1) {
                alert('Por favor ingrese un ID de venta válido.');
                return;
            }
            generateBoleta(ventaId); // Generar boleta para la venta seleccionada
        });
    }

    init(); // Cargar ventas al inicio
});
