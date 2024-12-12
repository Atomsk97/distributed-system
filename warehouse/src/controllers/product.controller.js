import { readData, writeData } from "../models/product.model.js";

export const getAllProducts = async (req, res) => {
  try {
    const response = await readData();
    return res.status(200).json(response);
  } catch (error) {
    return res
      .status(500)
      .json({ message: `Error al obtener los productos:\n ${error.message}` });
  }
};

export const createProduct = async (req, res) => {
  try {
    const { name, stock, price } = req.body;
    const products = await readData();
    const id = products.length;
    const newProduct = {
      ID: id,
      name,
      stock,
      price,
    };
    products.push(newProduct);
    await writeData(products);
    return res.status(201).json({ message: "Producto creado: ", newProduct });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Error al crear producto:\n", error });
  }
};

export const updateProduct = async (req, res) => {
  try {
    const { id } = req.params;
    const { name, stock, price } = req.body;
    if (!id) {
      return res
        .status(400)
        .json({ message: "Producto a actualizar no especificado" });
    }

    if (!name || !stock || !price) {
      return res.status(400).json({ message: "Actualizacion vacia" });
    }

    const products = await readData();
    const index = products.findIndex((p) => p.product_id === id);

    if (index === -1) {
      return res.status(404).json({ message: "Producto no encontrado" });
    }

    const updateProduct = {
      ID: id,
      name,
      stock,
      price,
    };

    products[index] = { ...products[index], ...updateProduct };
    await writeData(products);
    return res
      .status(200)
      .json({ message: "Producto actualizado", updateProduct });
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Error al eliminar el producto:\n", error });
  }
};

export const deleteProduct = async (req, res) => {
  try {
    const { id } = req.params;
    if (!id) {
      return res
        .status(400)
        .json({ message: "Producto a eliminar no especificado" });
    }
    const products = await readData();
    const filteredProducts = products.filter((p) => p.product_id !== id);

    if (products.length === filteredProducts.length) {
      return res.status(404).json({ message: "Producto no encontrado" });
    }

    await writeData(products);
    return res.sendStatus(204);
  } catch (error) {
    return res
      .status(500)
      .json({ message: "Error al eliminar el producto:\n", error });
  }
};
