const express = require('express');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = 4000;
const dbPath = path.join(__dirname, '../db/warehouse.json');

app.use(express.json());

// Leer productos del almacén
const readWarehouse = () => {
    try {
        const data = fs.readFileSync(dbPath, 'utf8');
        return JSON.parse(data);
    } catch (error) {
        console.error('Error leyendo el almacén:', error);
        return [];
    }
};

// Guardar productos en el almacén
const writeWarehouse = (products) => {
    try {
        fs.writeFileSync(dbPath, JSON.stringify(products, null, 2), 'utf8');
    } catch (error) {
        console.error('Error guardando el almacén:', error);
    }
};

// Obtener todos los productos
app.get('/almacen', (req, res) => {
    const products = readWarehouse();
    res.status(200).json(products);
});

// Actualizar el stock de un producto
app.put('/almacen/:id', (req, res) => {
    const productId = req.params.id;
    const { stock } = req.body;
    const products = readWarehouse();

    const product = products.find(p => p.ID === productId);
    if (!product) {
        return res.status(404).send({ message: 'Producto no encontrado' });
    }

    product.stock = stock;
    writeWarehouse(products);
    res.status(200).send({ message: 'Stock actualizado', product });
});

app.listen(PORT, () => {
    console.log(`Servidor de almacén corriendo en http://localhost:${PORT}`);
});
