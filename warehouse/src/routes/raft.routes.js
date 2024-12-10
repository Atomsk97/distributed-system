import { Router } from "express";
import RaftNode from "../services/raft.service.js";

const router = Router();
const raftNode = new RaftNode();

router.post("/vote", (req, res) => {
  const { term, candidateId } = req.body;
  const result = raftNode.handleVoteRequest(term, candidateId);
  res.json(result);
});

router.post("/heartbeat", (req, res) => {
  const { term, leaderId, log } = req.body;
  const result = raftNode.handleAppendEntries(term, leaderId, log);
  res.json(result);
});

export default router;
