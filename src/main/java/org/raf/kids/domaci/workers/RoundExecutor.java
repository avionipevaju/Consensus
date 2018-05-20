package org.raf.kids.domaci.workers;

import org.raf.kids.domaci.StartNode;
import org.raf.kids.domaci.vo.Message;
import org.raf.kids.domaci.vo.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class RoundExecutor implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(RoundExecutor.class);

    private Node node;

    public RoundExecutor(Node node) {
        this.node = node;
    }

    public void nodeWait(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (node.getRound() > StartNode.NODE_COUNT) {
            return;
        }
        Random random = new Random();
        logger.info("Started round {}", node.getRound());
        if (node.getRound() == node.getId()) {
            logger.info("Node {} is leader", node.getId());
            while (node.getProposalList().size() < (StartNode.NODE_COUNT) / 2) {
                nodeWait(1);
            }
            logger.info("Proposal list {}", node.getProposalList());
            Object proposal = node.getProposalList().get(random.nextInt(node.getProposalList().size())).getContent();
            logger.info("Proposal: {}", proposal);
            node.broadcastMessage(new Message(node.getId(), MessageType.PROPOSAL, proposal));
            while (node.getAckNumber() < (StartNode.NODE_COUNT) / 2) {
                nodeWait(1);
            }
            node.decide(proposal);
            node.broadcastMessage(new Message(node.getId(), MessageType.DECISION, proposal));
            node.moveToNextRound();
        } else {
            ExecutorService executorService = Executors.newCachedThreadPool();
            Node sendTo = node.getNodeNeighbourById(node.getRound());
            Message message = new Message(node.getId(), MessageType.INITIAL_PROPOSAL, node.getProposal());
            executorService.submit(new MessageSender(sendTo, node, message));
        }
    }
}
