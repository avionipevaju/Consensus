package org.raf.kids.domaci.workers;


import org.raf.kids.domaci.listeners.MessageListener;
import org.raf.kids.domaci.listeners.StatusListener;
import org.raf.kids.domaci.vo.Message;
import org.raf.kids.domaci.vo.MessageType;
import org.raf.kids.domaci.vo.NodeStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private List<Message> proposalList;
    private int ackNumber = 0;
    private Node checkingNode;
    ExecutorService executorService;

    private int round = 1;
    private Object proposal;

    public Node(int id, String ip, int communicationPort, int statusCheckPort, List<Node> neighbours, int checkingNode) {
        this.id = id;
        this.ip = ip;
        this.communicationPort = communicationPort;
        this.statusCheckPort = statusCheckPort;
        this.neighbours = neighbours;
        this.checkingNode = getNodeNeighbourById(checkingNode);
        this.receivedMessages = new HashMap<>();
        this.proposalList = new ArrayList<>();
    }

    public Node(int id, String ip, int communicationPort, int statusCheckPort) {
        this.id = id;
        this.ip = ip;
        this.communicationPort = communicationPort;
        this.statusCheckPort = statusCheckPort;
        this.status = NodeStatus.NOT_STARTED;
        this.receivedMessages = new HashMap<>();
        this.proposalList = new ArrayList<>();
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

    public void announceActive(Node node) {
        logger.info("Active Announce called by node {}", id);
        Message message = new Message(this.getId(), MessageType.NODE_ACTIVE, node.getId());
        broadcastMessage(message);
    }

    public void announceFailure(Node node) {
        logger.info("Failure Announce called by node {}", id);
        Message message = new Message(this.getId(), MessageType.NODE_FAILURE, node.getId());
        broadcastMessage(message);
    }

    public void suspectFailure(Node node) {
        logger.info("Suspect Failure called by node {}", id);
        Message message = new Message(this.getId(), MessageType.SUSPECT_FAILURE, node.getId());
        broadcastMessage(message);
    }

    @Override
    public void run() {
        this.status = NodeStatus.ACTIVE;
        for (Node node: neighbours) {
            node.setStatus(NodeStatus.ACTIVE);
        }
        executorService = Executors.newCachedThreadPool();
        try {
            MessageListener messageListener = new MessageListener(this);
            messageListener.startListener();
            StatusListener statusListener = new StatusListener(this);
            statusListener.startListener();
            logger.info("Started node listener for node {}, {} on communicationPort {}", id, ip, communicationPort);
        } catch (Exception e) {
            logger.error("Error opening node listener socket for node {}, {} on communicationPort {}, error: {}", id, ip, communicationPort, e.getMessage());
        }

        executorService.submit(new StatusChecker(checkingNode, this));

        /*proposal = id;
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        announceActive(this);
        while(!areNeighboursActive()){}
        executorService.submit(new RoundExecutor(this));*/

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

    public Node getNodeNeighbourById(int nodeId) {
        for (Node node: neighbours) {
            if (node.getId() == nodeId){
                return node;
            }
        }
        return null;
    }

    public List<Message> getProposalList() {
        return proposalList;
    }

    public void addProposal(Message message) {
        proposalList.add(message);
    }

    public void moveToNextRound() {
        round++;
        executorService.submit(new RoundExecutor(this));
    }

    public void addAck() {
        ackNumber++;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setProposal(Object proposal) {
        this.proposal = proposal;
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

    public int getRound() {
        return round;
    }

    public Object getProposal() {
        return proposal;
    }

    public boolean propose(Object proposal) {
        Node node = getNodeNeighbourById(round);
        if (node.getStatus().equals(NodeStatus.SUSPECTED_FAILURE) || node.getStatus().equals(NodeStatus.FAILED))
            return false;
        this.proposal = proposal;
        return true;
    }

    public boolean areNeighboursActive() {
        for (Node node: neighbours) {
            if (node.getStatus() != NodeStatus.ACTIVE) {
                return false;
            }
        }
        return true;
    }

    public void decide() {
        logger.info("Node {} decided on {}", id, proposal);
    }

    public int getAckNumber() {
        return ackNumber;
    }

    public void setAckNumber(int ackNumber) {
        this.ackNumber = ackNumber;
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
