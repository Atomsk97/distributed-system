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
        // Enviar cada producto de la venta al servidor de almacén
        for (let item of newSale.details) {
            const url = `http://${ipAlmacen}:4000/almacen/${item.product_id}`;
            const data = { stock: item.amount };

            // Enviar la solicitud PUT para actualizar el stock del producto
            await axios.put(url, data);
        }

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
