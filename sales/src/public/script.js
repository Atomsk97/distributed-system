document.addEventListener('DOMContentLoaded', async () => {
    const tableBody = document.getElementById('sales-table-body');

    // Función para obtener ventas del servidor
    async function fetchSales() {
        const response = await fetch('/ventas');
        return response.json();
    }

    // Función para renderizar ventas
    function renderSales(sales) {
        tableBody.innerHTML = '';
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

    // Cargar ventas al cargar la página
    const sales = await fetchSales();
    renderSales(sales);
});
