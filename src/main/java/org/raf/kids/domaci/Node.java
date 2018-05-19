package org.raf.kids.domaci;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Node implements Runnable{

    private static Logger logger = LoggerFactory.getLogger(Node.class);

    private int id;
    private String ip;
    private int port;
    private List<Node> neighbours;
    private NodeStatus status;
    private List<Socket> socketList;

    public Node(int id, String ip, int port, List<Node> neighbours) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.neighbours = neighbours;
        this.status = NodeStatus.NOT_STARTED;
        socketList = new ArrayList<>();
    }

    public Node(int id, String ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
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
            NodeListener nodeListener = new NodeListener(port);
            nodeListener.startNodeListener();
            logger.info("Started node listener for node {}, {} on port {}", id, ip, port);
        } catch (Exception e) {
            logger.error("Error opening node listener socket for node {}, {} on port {}, error: {}", id, ip, port, e.getMessage());
        }

       /* for(Node node: neighbours) {
            int otherPort = node.getPort();
            int id = node.getId();
            String ip = node.getIp();
            try {
                Socket socket = new Socket(ip, otherPort);
                socketList.add(socket);
                logger.info("Started node {} client, {} on port {} on node {}", id, ip, otherPort, getId());
            } catch (IOException e) {
                logger.error("Error opening socket for node {}, {} on port {}, error: {}", id, ip, otherPort, e.getMessage());
            }
        }

        while (true) {
            if(!socketList.isEmpty()) {
                for(Socket socket: socketList) {
                    SocketUtils.writeLine(socket, "ping");
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

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
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

    @Override
    public String toString() {
        return "\nNode{" +
                "id='" + id + '\'' +
                ", ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                ", status=" + status + '\'' +
                ", neighbours=" + neighbours +
                '}';
    }
}
