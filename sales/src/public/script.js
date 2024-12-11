document.addEventListener('DOMContentLoaded', () => {
    const tableBody = document.getElementById('sales-table-body');

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
        sales.forEach(sale => {
            sale.details.forEach(detail => {
                const row = `
                    <tr>
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

    // Inicializar la aplicación
    async function init() {
        const sales = await fetchSales(); // Obtener ventas desde el servidor
        renderSales(sales); // Renderizar ventas en la tabla
    }

    init(); // Cargar ventas al inicio
});
