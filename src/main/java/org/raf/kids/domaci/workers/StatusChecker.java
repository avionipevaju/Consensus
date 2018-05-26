package org.raf.kids.domaci.workers;

import org.raf.kids.domaci.utils.SocketUtils;
import org.raf.kids.domaci.vo.NodeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;

public class StatusChecker implements Runnable{

    private Node nodeToCheck;
    private Node nodeChecking;
    private static Logger logger = LoggerFactory.getLogger(StatusChecker.class);
    private long timeout = 5000;
    private long suspectedTimeout = 500;

    public StatusChecker(Node nodeToCheck, Node nodeChecking) {
        this.nodeToCheck = nodeToCheck;
        this.nodeChecking = nodeChecking;
    }

    @Override
    public void run() {
        int id = nodeToCheck.getId();
        int port = nodeToCheck.getStatusCheckPort();
        String ip = nodeToCheck.getIp();
        int elapsed = 0;
        long started;
        while (true) {
            started = new Date().getTime();
            try {
                Socket socket = new Socket(ip, port);
                SocketUtils.writeLine(socket, "status");
                SocketUtils.readLine(socket);
                if (nodeToCheck.getStatus() == NodeStatus.SUSPECTED_FAILURE) {
                    nodeChecking.announceActive(nodeToCheck);
                }
                elapsed = 0;
                nodeToCheck.setStatus(NodeStatus.ACTIVE);
            } catch (IOException e) {
                elapsed += new Date().getTime() - started;
                if(elapsed > timeout)
                    break;
                if(elapsed > suspectedTimeout)
                    if(nodeToCheck.getStatus() != NodeStatus.SUSPECTED_FAILURE) {
                        nodeToCheck.setStatus(NodeStatus.SUSPECTED_FAILURE);
                        nodeChecking.suspectFailure(nodeToCheck);
                        logger.warn("Node {} says: Node {} suspected of failure",nodeChecking.getId(), id);
                    }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        logger.error("Node {} says: Node {} has failed", nodeChecking.getId(), id);
        nodeToCheck.setStatus(NodeStatus.FAILED);
        nodeChecking.announceFailure(nodeToCheck);
        nodeChecking.addNodeToCheck(nodeChecking.getNodeNeighbourById(nodeToCheck.getCheckingNodeId()));
        /*for (Node temp: nodeToCheck.getCheckingNodes()) {
            if(nodeChecking.getId() != temp.getId())
                nodeChecking.addNodeToCheck(nodeToCheck.getNodeNeighbourById(temp.getCheckingNodeId()));
        }*/
        nodeChecking.rebroadcastMessagesForNode(nodeToCheck);
    }
}
