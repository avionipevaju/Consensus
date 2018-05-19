package org.raf.kids.domaci.listeners;

import org.raf.kids.domaci.workers.Node;
import org.raf.kids.domaci.utils.SocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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

    @Override
    public void run() {
        try {
            ServerSocket nodeListenerSocket = new ServerSocket(node.getCommunicationPort());
            while (true) {
                Socket clientSocket = nodeListenerSocket.accept();
                String received = SocketUtils.readLine(clientSocket);
                logger.info("Node {} has received a message {}",node.getId(), received);

            }
        } catch (IOException e) {
            logger.error("Error starting message listener socket at port: {} ", node.getCommunicationPort(), e);
        }

    }
}
