const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');

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

// Función para replicar sales.json a un nodo usando cURL
const replicateToNodeWithCurl = (nodeIP, data) => {
    return new Promise((resolve, reject) => {
        const curlCommand = `curl -X POST -H "Content-Type: application/json" -d '${data}' http://${nodeIP}:3000/replicate`;

        exec(curlCommand, (error, stdout, stderr) => {
            if (error) {
                reject(`Error replicando en ${nodeIP}: ${stderr || error.message}`);
            } else {
                resolve(`Replicación exitosa en ${nodeIP}`);
            }
        });
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
                const result = await replicateToNodeWithCurl(nodeIP, salesData);
                console.log(result);
            } catch (error) {
                console.error(error);
            }
        }
    } catch (error) {
        console.error('Error replicando ventas:', error);
    }
};

// Vigilar cambios en sales.json y replicar automáticamente
fs.watch(salesPath, (eventType) => {
    if (eventType === 'change') {
        console.log('sales.json ha sido modificado, replicando...');
        replicateSales();
    }
});

module.exports = { replicateSales };
