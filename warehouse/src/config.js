import dotenv from "dotenv";

dotenv.config();

export const PORT = process.env.PORT || 3000;
export const NODE_ID = process.env.NODE_ID || "node1";
export const PEERS = process.env.PEERS;
