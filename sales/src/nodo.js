const express = require('express');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = 3000;  // Asegúrate de que cada nodo tenga un puerto único
const dbPath = path.join(__dirname, '../db/sales.json');

// Middleware para procesar JSON
app.use(express.json());

// Endpoint para recibir datos y actualizar el archivo sales.json
app.post('/replicate', (req, res) => {
    const salesData = req.body;

    try {
        // Guardar los datos recibidos en el archivo sales.json
        fs.writeFileSync(dbPath, JSON.stringify(salesData, null, 2), 'utf8');
        console.log('sales.json actualizado en el nodo');
        res.status(200).send({ message: 'sales.json actualizado correctamente' });
    } catch (error) {
        console.error('Error al actualizar sales.json:', error);
        res.status(500).send({ message: 'Error al actualizar sales.json' });
    }
});

// Iniciar el servidor
app.listen(PORT, () => {
    console.log(`Servidor del nodo corriendo en http://localhost:${PORT}`);
});
