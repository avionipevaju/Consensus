package org.raf.kids.domaci.listeners;

import org.raf.kids.domaci.vo.Message;
import org.raf.kids.domaci.vo.MessageType;
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
        try {
            ServerSocket nodeListenerSocket = new ServerSocket(node.getCommunicationPort());
            while (true) {
                Socket clientSocket = nodeListenerSocket.accept();
                Message received = SocketUtils.readMessage(clientSocket);
                MessageType type = received.getMessageType();
                switch (type) {
                    case INITIAL_PROPOSAL:
                        node.addProposal(received);
                        logger.info("Node {} has received a message {} ",node.getId(), received);
                        break;
                    case PROPOSAL:
                        logger.info("Node {} has received a message {} ",node.getId(), received);
                        node.setProposal(received.getContent());
                        SocketUtils.writeLine(clientSocket, "ACK");
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
                }
            }
        } catch (IOException e) {
            logger.error("Error starting message listener socket at port: {} ", node.getCommunicationPort(), e);
        }

    }
}
