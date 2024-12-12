const express = require('express');
const fs = require('fs');
const path = require('path');
const axios = require('axios');

const app = express();
const PORT = 3000;
const dbPath = path.join(__dirname, '../db/sales.json');

// Middleware para procesar JSON y servir archivos estáticos
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// Ruta al archivo que contiene la IP del servidor de almacén
const almacenIpPath = path.join(__dirname, 'almacen_ip.txt');
// Ruta para enviar el archivo index.html
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'public', 'index.html'));
});
// Endpoint para obtener las ventas
app.get('/ventas', (req, res) => {
    try {
        const data = fs.readFileSync(dbPath, 'utf8');
        const sales = JSON.parse(data);
        res.status(200).json(sales);
    } catch (error) {
        console.error('Error al leer sales.json:', error);
        res.status(500).send('Error al leer la base de datos de ventas.');
    }
});
// Función para leer la IP del almacén desde el archivo
const obtenerIpAlmacen = () => {
    try {
        const ip = fs.readFileSync(almacenIpPath, 'utf8').trim();
        return ip;
    } catch (error) {
        console.error('Error al leer el archivo de IP del almacén:', error);
        return null;
    }
};

// Leer ventas desde sales.json
const readSales = () => {
    try {
        const data = fs.readFileSync(dbPath, 'utf8');
        return JSON.parse(data);
    } catch (error) {
        console.error('Error leyendo el archivo de ventas:', error);
        return [];
    }
};

// Guardar ventas en sales.json
const writeSales = (sales) => {
    try {
        fs.writeFileSync(dbPath, JSON.stringify(sales, null, 2), 'utf8');
    } catch (error) {
        console.error('Error guardando las ventas:', error);
    }
};


// Ruta para recibir y guardar nuevas ventas
app.post('/ventas', async (req, res) => {
    const newSale = req.body;
    const sales = readSales();

    sales.push(newSale); // Agregar nueva venta
    writeSales(sales);   // Guardar en sales.json

    // Enviar la información de la venta al almacén para actualizar el stock
    const ipAlmacen = obtenerIpAlmacen();
    if (!ipAlmacen) {
        return res.status(500).send({ message: 'No se pudo obtener la IP del almacén' });
    }

    try {
        // Crear un arreglo con los productos y sus cantidades para enviar al almacén
        const productsToUpdate = newSale.details.map(item => ({
            product_id: item.product_id,
            amount: item.amount
        }));

        // Enviar la solicitud PUT al almacén para actualizar el stock
        const url = `http://${ipAlmacen}:4000/almacen/update-stock`;  // Suponiendo que el endpoint es '/almacen/update-stock'
        const data = { products: productsToUpdate };

        // Enviar la solicitud PUT para actualizar el stock del almacén
        await axios.put(url, data);

        res.status(200).send({ message: 'Venta registrada y stock actualizado' });
    } catch (error) {
        console.error('Error al actualizar el stock en el almacén:', error);
        res.status(500).send({ message: 'Error al actualizar el stock' });
    }
});

// Iniciar el servidor
app.listen(PORT, () => {
    console.log(`Servidor corriendo en http://localhost:${PORT}`);
});
