const express = require('express');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = 3000;
const dbPath = path.join(__dirname, '../db/sales.json');

// Middleware para procesar JSON y servir archivos estáticos
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

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

// Ruta para obtener todas las ventas
app.get('/ventas', (req, res) => {
    const sales = readSales();
    res.status(200).json(sales);
});

// Ruta para recibir y guardar nuevas ventas
app.post('/ventas', (req, res) => {
    const newSale = req.body;
    const sales = readSales();

    sales.push(newSale); // Agregar nueva venta
    writeSales(sales);   // Guardar en sales.json

    res.status(200).send({ message: 'Venta registrada con éxito' });
});

// Iniciar el servidor
app.listen(PORT, () => {
    console.log(`Servidor corriendo en http://localhost:${PORT}`);
});
