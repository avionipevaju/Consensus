package org.raf.kids.domaci.workers;

import org.raf.kids.domaci.utils.SocketUtils;
import org.raf.kids.domaci.vo.NodeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

public class StatusChecker implements Runnable {

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
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(ip, port), 5000);
        } catch (IOException e) {
            logger.error("Node {} says: Failed to open status socket to Node {}", nodeChecking.getId(), id, e);
            nodeToCheck.setStatus(NodeStatus.FAILED);
            nodeChecking.announceFailure(nodeToCheck);
            nodeChecking.addNodeToCheck(nodeChecking.getNodeNeighbourById(nodeToCheck.getCheckingNodeId()));
            nodeChecking.rebroadcastMessagesForNode(nodeToCheck);
            return;
        }
        while (true) {
            started = new Date().getTime();
            try {
                SocketUtils.writeLine(socket, "status");
                SocketUtils.readLine(socket);
                if (nodeToCheck.getStatus() != NodeStatus.ACTIVE) {
                    nodeChecking.announceActive(nodeToCheck);
                }
                elapsed = 0;
                nodeToCheck.setStatus(NodeStatus.ACTIVE);
            } catch (IOException e) {
                elapsed += new Date().getTime() - started;
                if (elapsed > timeout)
                    break;
                if (elapsed > suspectedTimeout)
                    if (nodeToCheck.getStatus() != NodeStatus.SUSPECTED_FAILURE) {
                        nodeToCheck.setStatus(NodeStatus.SUSPECTED_FAILURE);
                        logger.warn("Node {} says: Node {} suspected of failure", nodeChecking.getId(), id);
                        nodeChecking.suspectFailure(nodeToCheck);
                    }
            }
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("Error closing status socket on Node {}", nodeChecking.getId(), e);
            }
        }

        logger.error("Node {} says: Node {} has failed", nodeChecking.getId(), id);
        nodeToCheck.setStatus(NodeStatus.FAILED);
        nodeChecking.announceFailure(nodeToCheck);
        nodeChecking.addNodeToCheck(nodeChecking.getNodeNeighbourById(nodeToCheck.getCheckingNodeId()));
        nodeChecking.rebroadcastMessagesForNode(nodeToCheck);
    }
}
