const express = require("express");
const fs = require("fs");
const path = require("path");

const app = express();
const PORT = 4000;
const dbPath = path.join(__dirname, "../db/warehouse.json");

//servicio del html
app.use(express.static(path.join(__dirname, "public")));
app.use(express.json());
// Ruta para la página de administración (/admin)
app.get("/admin", (req, res) => {
  res.sendFile(path.join(__dirname, "public", "admin.html"));
});
// Leer productos del almacén
const readWarehouse = () => {
  try {
    const data = fs.readFileSync(dbPath, "utf8");
    return JSON.parse(data);
  } catch (error) {
    console.error("Error leyendo el almacén:", error);
    return [];
  }
};

// Guardar productos en el almacén
const writeWarehouse = (products) => {
  try {
    fs.writeFileSync(dbPath, JSON.stringify(products, null, 2), "utf8");
  } catch (error) {
    console.error("Error guardando el almacén:", error);
  }
};

// Obtener todos los productos
app.get("/almacen", (req, res) => {
  const products = readWarehouse();
  res.status(200).json(products);
});

// Ruta para actualizar el stock de múltiples productos
app.put("/almacen/update-stock", (req, res) => {
  const { products } = req.body; // Recibe un arreglo de productos con product_id y amount

  // Validar que 'products' sea un arreglo
  if (!Array.isArray(products) || products.length === 0) {
    return res
      .status(400)
      .send({ message: "Se debe enviar un arreglo de productos." });
  }

  // Leer el almacén actual
  const warehouse = readWarehouse();
  const results = [];

  // Procesar cada producto
  for (let item of products) {
    const { product_id, amount } = item;

    // Asegúrate de que 'amount' sea un número
    const parsedAmount = Number(amount);
    //console.log(amount);
    // Validar que 'amount' sea un número positivo
    if (isNaN(parsedAmount) || parsedAmount <= 0) {
      results.push({
        product_id,
        status: "error",
        message: "La cantidad debe ser un número positivo.",
      });
      continue;
    }

    // Buscar el producto en el almacén
    const product = warehouse.find((p) => p.product_id === product_id);
    if (!product) {
      results.push({
        product_id,
        status: "error",
        message: `Producto con ID ${product_id} no encontrado.`,
      });
      continue;
    }

    // Verificar que haya suficiente stock disponible
    if (product.stock < amount) {
      results.push({
        product_id,
        status: "error",
        message: `Stock insuficiente para el producto con ID ${product_id}.`,
        current_stock: product.stock,
      });
      continue;
    }

    // Restar el stock solicitado
    product.stock -= amount;
    results.push({
      product_id,
      status: "success",
      message: `Stock actualizado. Stock restante: ${product.stock}`,
    });
  }

  // Guardar los cambios en el almacén
  writeWarehouse(warehouse);

  // Responder con un resumen de resultados
  res.status(200).send({
    message: "Proceso completado.",
    results,
  });
});

// Ruta para modificar un producto
app.put("/almacen/:id", (req, res) => {
  const { id } = req.params;
  const { stock, price } = req.body; // Solo actualizamos stock y precio

  const products = readWarehouse();
  const product = products.find((p) => p.product_id === parseInt(id));

  if (!product) {
    return res.status(404).send({ message: "Producto no encontrado." });
  }

  product.stock = stock;
  product.price = price;
  writeWarehouse(products);

  res.status(200).send({ message: "Producto actualizado." });
});

// Ruta para agregar un nuevo producto
app.post("/almacen", (req, res) => {
  const newProduct = req.body;
  const products = readWarehouse();
  products.push(newProduct);
  writeWarehouse(products);
  res.status(201).send({ message: "Producto agregado correctamente." });
});

// Ruta para eliminar productos
app.delete("/almacen", (req, res) => {
  const { product_ids } = req.body;

  let products = readWarehouse();
  products = products.filter(
    (product) => !product_ids.includes(product.product_id)
  );

  writeWarehouse(products);
  res.status(200).send({ message: "Productos eliminados." });
});

app.listen(PORT, () => {
  console.log(`Servidor de almacén corriendo en http://localhost:${PORT}`);
});
