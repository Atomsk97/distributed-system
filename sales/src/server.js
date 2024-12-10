const express = require('express');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = 3000;
const dbPath = path.join(__dirname, '../db/sales.json');

// Middleware para procesar JSON y servir archivos estáticos
app.use(express.json());
app.use(express.static(path.join(__dirname, 'public')));

// Ruta para recibir ventas desde el cliente
app.post('/ventas', (req, res) => {
    const newSale = req.body;

    // Leer el archivo JSON existente
    fs.readFile(dbPath, 'utf8', (err, data) => {
        if (err) return res.status(500).send({ message: 'Error leyendo la base de datos' });

        const sales = JSON.parse(data);
        sales.push(newSale);

        // Escribir los datos actualizados en el archivo JSON
        fs.writeFile(dbPath, JSON.stringify(sales, null, 2), 'utf8', (err) => {
            if (err) return res.status(500).send({ message: 'Error guardando la venta' });
            res.status(200).send({ message: 'Venta registrada con éxito' });
        });
    });
});

// Ruta para obtener las ventas
app.get('/ventas', (req, res) => {
    fs.readFile(dbPath, 'utf8', (err, data) => {
        if (err) return res.status(500).send({ message: 'Error leyendo la base de datos' });

        res.status(200).json(JSON.parse(data));
    });
});

// Iniciar el servidor
app.listen(PORT, () => {
    console.log(`Servidor corriendo en http://localhost:${PORT}`);
});
