package org.raf.kids.domaci;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Node implements Runnable{

    private static Logger logger = LoggerFactory.getLogger(Node.class);

    private int id;
    private String ip;
    private int communicationPort;
    private int statusCheckPort;
    private List<Node> neighbours;
    private NodeStatus status;
    private List<Socket> socketList;

    public Node(int id, String ip, int communicationPort, int statusCheckPort, List<Node> neighbours) {
        this.id = id;
        this.ip = ip;
        this.communicationPort = communicationPort;
        this.statusCheckPort = statusCheckPort;
        this.neighbours = neighbours;
    }

    public Node(int id, String ip, int communicationPort, int statusCheckPort) {
        this.id = id;
        this.ip = ip;
        this.communicationPort = communicationPort;
        this.statusCheckPort = statusCheckPort;
        this.status = NodeStatus.NOT_STARTED;
        socketList = new ArrayList<>();
    }

    public void activateNode() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        this.status = NodeStatus.ACTIVE;
        try {
            NodeListener communicationListener = new NodeListener(communicationPort);
            communicationListener.startNodeListener();
            NodeListener statusListener = new NodeListener(statusCheckPort);
            statusListener.startNodeListener();
            logger.info("Started node listener for node {}, {} on communicationPort {}", id, ip, communicationPort);
        } catch (Exception e) {
            logger.error("Error opening node listener socket for node {}, {} on communicationPort {}, error: {}", id, ip, communicationPort, e.getMessage());
        }

        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Future<Node>> resultList = new ArrayList<>();
        for (Node node: neighbours) {
            Future<Node> result = executorService.submit(new StatusChecker(node));
            resultList.add(result);
        }

        /*while (true) {
            for(Future<Node> result: resultList) {
                if(result.isDone()) {
                    try {
                        Node node = result.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }*/

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
