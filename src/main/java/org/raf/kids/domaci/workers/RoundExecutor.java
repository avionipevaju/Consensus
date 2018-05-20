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

    @Override
    public void run() {
        Random random = new Random();
        if (node.getRound() == node.getId()) {
            while (node.getProposalList().size() < (StartNode.NODE_COUNT - 1 ) / 2) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            logger.info("Proposal list {}", node.getProposalList());
            Object proposal = node.getProposalList().get(random.nextInt(node.getProposalList().size())).getContent();
            logger.info("Proposal: {}", proposal);
            node.broadcastMessage(new Message(node.getId(), MessageType.PROPOSAL, proposal));
        } else {
            ExecutorService executorService = Executors.newCachedThreadPool();
            Node sendTo = node.getNodeNeighbourById(node.getRound());
            Message message = new Message(node.getId(), MessageType.INITIAL_PROPOSAL, node.getProposal());
            executorService.submit(new MessageSender(sendTo, node, message));
        }
    }
}
