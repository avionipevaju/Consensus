package org.raf.kids.domaci.listeners;

import org.raf.kids.domaci.vo.Message;
import org.raf.kids.domaci.vo.MessageType;
import org.raf.kids.domaci.vo.NodeStatus;
import org.raf.kids.domaci.workers.Node;
import org.raf.kids.domaci.utils.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class MessageListener implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(MessageListener.class);

    private Node node;

    public MessageListener(Node node) {
        this.node = node;
    }

    public void startListener() {
        Thread thread =  new Thread(this);
        thread.start();
    }

    public boolean checkMessage(List<Message> messages, Message message) {
        for(Message m: messages) {
            if (m.getuId() == message.getuId())
                return true;
        }
        return false;
    }

    @Override
    public void run() {
        ServerSocket nodeListenerSocket = null;
        Socket clientSocket = null;
        try {
            nodeListenerSocket = new ServerSocket(node.getCommunicationPort());
            while (true) {
                clientSocket = nodeListenerSocket.accept();
                Message received = SocketUtils.readMessage(clientSocket);
                MessageType type = received.getMessageType();
                switch (type) {
                    case INITIAL_PROPOSAL:
                        node.addProposal(received);
                        logger.info("Node {} has received a message {} ",node.getId(), received);
                        break;
                    case PROPOSAL:
                        logger.info("Node {} has received a message {} ",node.getId(), received);
                        boolean success = node.propose(received);
                        if(success) {
                            node.moveToNextRound();
                        }
                        else {
                            node.moveToNextRound(); //NACK
                        }
                        break;
                    case DECISION:
                        List<Message> messageList = node.getNodeMessageHistory(received.getTraceId());
                        if (checkMessage(messageList, received)) {
                            logger.warn("Node {} has already received message {}", node.getId(), received);
                        } else {
                            node.addMessageToNodeHistory(received.getTraceId(), received);
                            logger.info("Node {} has received a message {} ",node.getId(), received);
                        }
                        break;
                    case NODE_FAILURE:
                        int failedNodeId = (int) received.getContent();
                        if (node.getId() == failedNodeId)
                            break;
                        Node failedNode = node.getNodeNeighbourById(failedNodeId);
                        failedNode.setStatus(NodeStatus.FAILED);
                        node.rebroadcastMessagesForNode(failedNode);
                        break;
                    case SUSPECT_FAILURE:
                        int suspectedNodeId = (int) received.getContent();
                        if (node.getId() == suspectedNodeId)
                            break;
                        Node suspectedNode = node.getNodeNeighbourById(suspectedNodeId);
                        suspectedNode.setStatus(NodeStatus.SUSPECTED_FAILURE);
                        break;
                    case NODE_ACTIVE:
                        int activeNodeId = (int) received.getContent();
                        if (node.getId() == activeNodeId)
                            break;
                        Node activeNode = node.getNodeNeighbourById(activeNodeId);
                        activeNode.setStatus(NodeStatus.ACTIVE);
                        break;
                    case CLOSE_STATUS_CHECK:
                        logger.info("Received close status check from Node {} on Node {}", received.getTraceId(), node.getId());
                        node.deactivateNode();
                        node.startStatusListener();
                        break;
                    default:
                        logger.info("MADRFAKR");
                }
            }
        } catch (Exception e) {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            logger.error("Error starting message listener socket at port: {} ", node.getCommunicationPort(), e);
        } finally {
            logger.info("Closing message listener socket on Node {}", node.getId());
            try {
                if(clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
