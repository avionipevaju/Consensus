package org.raf.kids.domaci.workers;


import org.raf.kids.domaci.listeners.MessageListener;
import org.raf.kids.domaci.listeners.StatusListener;
import org.raf.kids.domaci.utils.SocketUtils;
import org.raf.kids.domaci.vo.Message;
import org.raf.kids.domaci.vo.NodeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Node implements Runnable{

    private static Logger logger = LoggerFactory.getLogger(Node.class);

    private int id;
    private String ip;
    private int communicationPort;
    private int statusCheckPort;
    private List<Node> neighbours;
    private NodeStatus status;
    private Thread nodeThread;
    private HashMap<Integer, List<Message>> receivedMessages;

    public Node(int id, String ip, int communicationPort, int statusCheckPort, List<Node> neighbours) {
        this.id = id;
        this.ip = ip;
        this.communicationPort = communicationPort;
        this.statusCheckPort = statusCheckPort;
        this.neighbours = neighbours;
        this.receivedMessages = new HashMap<>();
    }

    public Node(int id, String ip, int communicationPort, int statusCheckPort) {
        this.id = id;
        this.ip = ip;
        this.communicationPort = communicationPort;
        this.statusCheckPort = statusCheckPort;
        this.status = NodeStatus.NOT_STARTED;
        this.receivedMessages = new HashMap<>();
    }

    public void activateNode() {
        nodeThread= new Thread(this);
        nodeThread.start();
    }

    public void broadcastMessage(Message message) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (Node node: neighbours) {
            executorService.submit(new MessageSender(node, this, message));
        }
    }

    public void rebroadcastMessagesForNode(Node node) {
        logger.info("Rebroadcast called by node {}", id);
        List<Message> messages = getNodeMessageHistory(node.getId());
        for(Message message: messages) {
            broadcastMessage(message);
        }
    }

    @Override
    public void run() {
        this.status = NodeStatus.ACTIVE;
        try {
            MessageListener messageListener = new MessageListener(this);
            messageListener.startListener();
            StatusListener statusListener = new StatusListener(this);
            statusListener.startListener();
            logger.info("Started node listener for node {}, {} on communicationPort {}", id, ip, communicationPort);
        } catch (Exception e) {
            logger.error("Error opening node listener socket for node {}, {} on communicationPort {}, error: {}", id, ip, communicationPort, e.getMessage());
        }

        ExecutorService executorService = Executors.newCachedThreadPool();
        for (Node node: neighbours) {
           executorService.submit(new StatusChecker(node, this));
        }

    }

    public List<Message> getNodeMessageHistory(int nodeId) {
        List<Message> messageHistory = receivedMessages.get(nodeId);
        if (messageHistory == null) {
            messageHistory = new ArrayList<>();
            receivedMessages.put(nodeId, messageHistory);
        }
        return messageHistory;
    }

    public void addMessageToNodeHistory(int nodeId, Message message) {
        getNodeMessageHistory(nodeId).add(message);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getCommunicationPort() {
        return communicationPort;
    }

    public void setCommunicationPort(int communicationPort) {
        this.communicationPort = communicationPort;
    }

    public List<Node> getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(List<Node> neighbours) {
        this.neighbours = neighbours;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(NodeStatus status) {
        this.status = status;
    }

    public int getStatusCheckPort() {
        return statusCheckPort;
    }

    public void setStatusCheckPort(int statusCheckPort) {
        this.statusCheckPort = statusCheckPort;
    }

    @Override
    public String toString() {
        return "\nNode{" +
                "id='" + id + '\'' +
                ", ip='" + ip + '\'' +
                ", communicationPort='" + communicationPort + '\'' +
                ", status=" + status + '\'' +
                ", neighbours=" + neighbours +
                '}';
    }
}
