package org.raf.kids.domaci.workers;

import org.raf.kids.domaci.utils.SocketUtils;
import org.raf.kids.domaci.vo.Message;
import org.raf.kids.domaci.vo.MessageType;
import org.raf.kids.domaci.vo.NodeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.spec.ECField;

public class MessageSender implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(MessageSender.class);

    private Node sendTo;
    private Node sendFrom;
    private Message message;

    public MessageSender(Node sendTo, Node sendFrom, Message message) {
        this.sendTo = sendTo;
        this.sendFrom = sendFrom;
        this.message = message;
    }

    @Override
    public void run() {
        try {
            while(sendTo.getStatus().equals(NodeStatus.SUSPECTED_FAILURE)) {
                Thread.sleep(333);
            }
            if(sendTo.getStatus().equals(NodeStatus.ACTIVE)) {
                MessageType type = message.getMessageType();
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(sendTo.getIp(),sendTo.getCommunicationPort()), 10);
                SocketUtils.writeMessage(socket, message);
                logger.info("Message: {} sent form Node {} to Node {}", message, sendFrom.getId(), sendTo.getId());
                switch (type) {
                    case INITIAL_PROPOSAL:
                        break;
                    case PROPOSAL:
                        String response = SocketUtils.readLine(socket);
                        if(response.equals("ACK"))
                            sendFrom.addAck();
                        else
                            sendFrom.moveToNextRound(); //NACK next round
                        logger.info("Node {} says: {}", sendTo.getId(), response);
                        break;
                    case DECISION:
                        break;
                }
                socket.close();
            } else {
                logger.error("Failed to send message form Node {} to Node {}. Error: Node inactive", sendFrom.getId(), sendTo.getId());
            }
        } catch (Exception e) {
            logger.error("Failed to send message form Node {} to Node {}. Error: {}", sendFrom.getId(), sendTo.getId(), e.getMessage());
        }

    }

    public Node getSendTo() {
        return sendTo;
    }

    public void setSendTo(Node sendTo) {
        this.sendTo = sendTo;
    }

    public Node getSendFrom() {
        return sendFrom;
    }

    public void setSendFrom(Node sendFrom) {
        this.sendFrom = sendFrom;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
