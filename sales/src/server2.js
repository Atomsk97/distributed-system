const express = require('express');
const fs = require('fs');
const path = require('path');
const axios = require('axios');
const { exec } = require('child_process');

const app = express();
const PORT = 3000;
const dbPath = path.join(__dirname, '../db/sales.json');
// Ruta de archivos
const salesPath = path.join(__dirname, '../db/sales.json');
const ipFilePath = path.join(__dirname, 'ip_raft.txt');
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
        console.error('Error al leer el archivo de IP del almacén:', error.message);
        return null;
    }
};
// Vigilar cambios en sales.json y replicar automáticamente
fs.watch(salesPath, (eventType) => {
    if (eventType === 'change') {
        console.log('sales.json ha sido modificado, replicando...');
        replicateSales();
    }
});
// Leer ventas desde sales.json
const readSales = () => {
    try {
        const data = fs.readFileSync(dbPath, 'utf8');
        return JSON.parse(data);
    } catch (error) {
        console.error('Error leyendo el archivo de ventas:', error.message);
        return [];
    }
};

// Guardar ventas en sales.json
const writeSales = (sales) => {
    try {
        fs.writeFileSync(dbPath, JSON.stringify(sales, null, 2), 'utf8');
    } catch (error) {
        console.error('Error guardando las ventas:', error.message);
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
            amount: item.amount,
            price: item.price
        }));

        // Enviar la solicitud PUT al almacén para actualizar el stock
        const url = `http://${ipAlmacen}:4000/almacen/update-stock`;  // Suponiendo que el endpoint es '/almacen/update-stock'
        const data = { products: productsToUpdate };

        // Enviar la solicitud PUT para actualizar el stock del almacén
        await axios.put(url, data);

        res.status(200).send({ message: 'Venta registrada y stock actualizado' });
    } catch (error) {
        console.error('Error al actualizar el stock en el almacén:', error.message);
        res.status(500).send({ message: 'Error al actualizar el stock' });
    }
});

app.get('/ventas/boleta/:client_id', (req, res) => {
    const { client_id } = req.params;

    try {
        const sales = readSales(); // Leer ventas de sales.json
        const sale = sales.find(s => s.client_id === client_id); // Buscar la venta

        if (!sale) {
            return res.status(404).send('<h1>Venta no encontrada</h1>');
        }

        const boletaHtml = `
            <html>
            <head>
                <title>Boleta</title>
            </head>
            <body>
                <h1>Boleta de Venta</h1>
                <p>Cliente ID: ${sale.client_id}</p>
                <ul>
                    ${sale.details.map(item => `
                        <li>
                            Producto: ${item.product_id}, Cantidad: ${item.amount}, Precio: ${item.price}
                        </li>
                    `).join('')}
                </ul>
                <p>Total: ${sale.details.reduce((sum, item) => sum + item.price * item.amount, 0)}</p>
            </body>
            </html>
        `;
        res.status(200).send(boletaHtml);
    } catch (error) {
        console.error('Error al generar la boleta:', error);
        res.status(500).send('<h1>Error al generar la boleta</h1>');
    }
});


// Leer las IPs del archivo ip_raft.txt
const readNodeIPs = () => {
    try {
        const data = fs.readFileSync(ipFilePath, 'utf8');
        return data.split('\n').map(ip => ip.trim()).filter(ip => ip !== '');
    } catch (error) {
        console.error('Error leyendo ip_raft.txt:', error);
        return [];
    }
};

// Función para replicar sales.json a un nodo usando cURL
const replicateToNodeWithCurl = (nodeIP, data) => {
    return new Promise((resolve, reject) => {
        const curlCommand = `curl -X POST -H "Content-Type: application/json" -d '${data}' http://${nodeIP}:3000/replicate`;

        exec(curlCommand, (error, stdout, stderr) => {
            if (error) {
                reject(`Error replicando en ${nodeIP}: ${stderr || error.message}`);
            } else {
                resolve(`Replicación exitosa en ${nodeIP}`);
            }
        });
    });
};

// Función principal para replicar la base de datos
const replicateSales = async () => {
    try {
        const nodes = readNodeIPs(); // Leer IPs
        const salesData = fs.readFileSync(salesPath, 'utf8'); // Leer sales.json

        // Replicar datos a cada nodo
        for (const nodeIP of nodes) {
            try {
                const result = await replicateToNodeWithCurl(nodeIP, salesData);
                console.log(result);
            } catch (error) {
                console.error(error);
            }
        }
    } catch (error) {
        console.error('Error replicando ventas:', error);
    }
};


// Iniciar el servidor
app.listen(PORT, () => {
    console.log(`Servidor corriendo en http://localhost:${PORT}`);
});
