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

    public StatusListener(Node node) {
        this.node = node;
    }

    public void startListener() {
        Thread thread =  new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            ServerSocket nodeListenerSocket = new ServerSocket(node.getStatusCheckPort());
            while (true) {
                Socket clientSocket = nodeListenerSocket.accept();
                String received = SocketUtils.readLine(clientSocket);
                if (received.equals("status")) {
                    SocketUtils.writeLine(clientSocket, "ok");
                } else {
                    logger.info("Node {} received unknown request. Message: {}", node.getId(), received);
                }
                clientSocket.close();

            }
        } catch (IOException e) {
            logger.error("Error starting status listener socket at port: {} ", node.getStatusCheckPort(), e);
        }

    }
}
