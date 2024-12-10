import app from "./app.js";
import { PORT, NODE_ID, PEERS } from "./config.js";
import RaftNode from "./services/raft.service.js";

const nodeId = NODE_ID;
const peers = PEERS ? PEERS.split(",") : [];
const raftNode = new RaftNode(nodeId, peers);

app.listen(PORT, "0.0.0.0", () => {
  //console.log(`Servicio de almacen ejecutandose en el puerto ${PORT}`);
  console.log(`Nodo Raft ${nodeId} escuchando en el puerto ${PORT}`);
  setTimeout(() => raftNode.startElection(), raftNode.randomElectionTimeout());
});
