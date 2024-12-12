import axios from "axios";

export default class RaftNode {
  constructor(nodeId, peers) {
    this.nodeId = nodeId;
    this.peers = peers;
    this.currentTerm = 0;
    this.votedFor = null;
    this.log = [];
    this.commitIndex = -1;
    this.role = "follower"; //follower, candidate, leader
    this.electionTimeout = this.randomElectionTimeout();
    this.timer = null;
  }

  // Between 1.5s and 3s
  randomElectionTimeout() {
    return Math.floor(Math.random() * (3000 - 1500) + 1500);
  }

  // Request votes to the other nodes
  startElection() {
    this.role = "candidate";
    this.currentTerm++;
    this.votedFor = this.nodeId;
    let votes = 1;

    console.log(
      `Nodo ${this.nodeId} inicio una eleccion en el termino ${this.currentTerm}`
    );

    // Request votes form other nodes
    this.peers.forEach(async (peer) => {
      try {
        const response = await axios.post(`http://${peer}/raft/vote`, {
          term: this.currentTerm,
          candidateId: this.nodeId,
        });

        if (response.data.voteGranted) {
          votes++;
          if (votes > Math.floor(this.peers.length / 2)) {
            this.becomeLeader();
          }
        }
      } catch (error) {
        console.log(`Error solicitando voto de ${peer}`, error.message);
      }
    });

    // Restart election if no consensus was reached
    setTimeout(() => {
      if (this.role === "candidate") {
        this.startElection();
      }
    }, this.randomElectionTimeout());
  }

  becomeLeader() {
    this.role = "leader";
    console.log(`Nodo ${this.nodeId} se convirtió en líder`);
    this.sendHeartbeat();
  }

  sendHeartbeat() {
    if (this.role !== "leader") return;

    this.peers.forEach((peer) => {
      axios
        .post(`http://${peer}/raft/heartbeat`, {
          term: this.currentTerm,
          leaderId: this.nodeId,
          log: this.log,
        })
        .catch((err) =>
          console.log(`Error enviando latido a ${peer}`, err.message)
        );
    });

    // Send Heartbeat
    setTimeout(() => this.sendHeartbeat(), 1000);
  }

  handleVoteRequest(term, candidateId) {
    if (term > this.currentTerm) {
      this.currentTerm = term;
      this.votedFor = null;
    }

    if (!this.votedFor && term == this.currentTerm) {
      this.votedFor = candidateId;
      return { voteGranted: true };
    }
    return { voteGranted: false };
  }

  handleAppendEntries(term, leaderId, entries) {
    if (term >= this.currentTerm) {
      this.currentTerm = term;
      this.role = "follower";
      this.log.push(...entries);
      return { success: true };
    }
    return { success: false };
  }


}
