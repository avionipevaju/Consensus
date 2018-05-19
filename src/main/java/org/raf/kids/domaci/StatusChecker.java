package org.raf.kids.domaci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;

public class StatusChecker implements Runnable{

    private Node nodeToCheck;
    private static Logger logger = LoggerFactory.getLogger(StatusChecker.class);
    private int timeout = 3000;
    private int suscpectedTimeout = 1500;

    public StatusChecker(Node nodeToCheck) {
        this.nodeToCheck = nodeToCheck;
    }

    @Override
    public void run() {
        int id = nodeToCheck.getId();
        int port = nodeToCheck.getStatusCheckPort();
        String ip = nodeToCheck.getIp();
        int elapsed = 0;
        long started;
        while (true) {
            started = new Date().getTime();
            try {
                Socket socket = new Socket(ip, port);
                SocketUtils.writeLine(socket, "status");
                String response =SocketUtils.readLine(socket);
                logger.info(response);
            } catch (IOException e) {
                elapsed += new Date().getTime() - started;
                if(elapsed > timeout)
                    break;
                if(elapsed > suscpectedTimeout)
                    nodeToCheck.setStatus(NodeStatus.SUSPECTED_FAILURE);
                    logger.warn("Node {} suscpected of failure", id);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        logger.error("Node {} has failed", id);
        nodeToCheck.setStatus(NodeStatus.FAILED);
    }
}
