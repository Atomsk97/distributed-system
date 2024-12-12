const fs = require('fs');
const path = require('path');
const http = require('http');

// Ruta de archivos
const salesPath = path.join(__dirname, '../db/sales.json');
const ipFilePath = path.join(__dirname, 'ip_raft.txt');

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

// Función para replicar sales.json a un nodo
const replicateToNode = (nodeIP, data) => {
    return new Promise((resolve, reject) => {
        const options = {
            hostname: nodeIP,
            port: 3000, // Asegúrate de que los nodos escuchen en este puerto
            path: '/replicate',
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Content-Length': Buffer.byteLength(data)
            }
        };

        const req = http.request(options, (res) => {
            if (res.statusCode === 200) {
                resolve(`Replicación exitosa en ${nodeIP}`);
            } else {
                reject(`Error replicando en ${nodeIP}: ${res.statusCode}`);
            }
        });

        req.on('error', (error) => {
            reject(`Error al conectar con ${nodeIP}: ${error.message}`);
        });

        req.write(data);
        req.end();
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
                const result = await replicateToNode(nodeIP, salesData);
                console.log(result);
            } catch (error) {
                console.error(error);
            }
        }
    } catch (error) {
        console.error('Error replicando ventas:', error);
    }
};

module.exports = { replicateSales };
