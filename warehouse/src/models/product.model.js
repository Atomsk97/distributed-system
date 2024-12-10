import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

import parse from "csv-parser";
import { writeToPath } from "fast-csv";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const filePath = path.join(__dirname, "../../db/warehouse.csv");

export const readData = async () => {
  return new Promise((resolve, reject) => {
    const data = [];
    fs.createReadStream(filePath)
      .pipe(parse({ columns: true, trim: true }))
      .on("data", (row) => data.push(row))
      .on("end", () => resolve(data))
      .on("error", (err) => reject(err));
  });
};

export const writeData = async (data) => {
  return new Promise((resolve, reject) => {
    writeToPath(filePath, data, { headers: true })
      .on("finish", resolve)
      .on("error", reject);
  });
};
