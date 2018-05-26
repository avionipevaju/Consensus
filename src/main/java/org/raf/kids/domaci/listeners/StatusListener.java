package org.raf.kids.domaci.listeners;

import org.raf.kids.domaci.workers.Node;
import org.raf.kids.domaci.utils.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class StatusListener implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(StatusListener.class);

    private Node node;
    private ServerSocket serverSocket;

    public StatusListener(Node node, ServerSocket serverSocket) {
        this.node = node;
        this.serverSocket = serverSocket;
    }

    public void startListener() {
        Thread thread =  new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        Socket clientSocket = null;
        try {
            clientSocket = serverSocket.accept();
            while (true) {
                if(serverSocket == null) {
                    break;
                }
                String received = SocketUtils.readLine(clientSocket);
                if (received.equals("status")) {
                    SocketUtils.writeLine(clientSocket, "ok");
                } else {
                    logger.info("Node {} received unknown request. Message: {}", node.getId(), received);
                }
            }
        } catch (IOException e) {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            logger.error("Error starting status listener socket at port: {} ", node.getStatusCheckPort(), e);
        } finally {
            logger.info("Closing status listener socket on Node {}", node.getId());
            try {
                if(serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
