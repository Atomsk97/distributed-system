import express from "express";

import productsRoutes from "./routes/product.routes.js";
import raftRoutes from "./routes/raft.routes.js";

const app = express();
app.disable("x-powered-by");

//Middleware
app.use(express.json());

//Routes
app.use("/api/products", productsRoutes);
app.use("/raft", raftRoutes);

//Default Route
app.all("*", (req, res) => {
  return res.json({ message: "Ruta no existente." });
});

export default app;
