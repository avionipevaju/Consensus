package org.raf.kids.domaci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NodeListener implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(NodeListener.class);

    private int port;

    public NodeListener(int port) {
        this.port = port;
    }

    public void startNodeListener() {
        Thread thread =  new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        try {
            ServerSocket nodeListenerSocket = new ServerSocket(port);
            while (true) {
                Socket clientSocket = nodeListenerSocket.accept();
                String received = SocketUtils.readLine(clientSocket);
                if (received.equals("ping")) {
                    SocketUtils.writeLine(clientSocket, "pong");
                } else {
                    logger.info(received);
                }

            }
        } catch (IOException e) {
            logger.error("Error starting Node listener socket at port: {} ", port, e);
        }

    }
}
