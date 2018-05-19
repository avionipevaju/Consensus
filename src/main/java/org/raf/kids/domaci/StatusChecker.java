package org.raf.kids.domaci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.Callable;

public class StatusChecker implements Callable<Node>{

    private Node nodeToCheck;
    private static Logger logger = LoggerFactory.getLogger(StatusChecker.class);
    private static int timeout = 3000;

    public StatusChecker(Node nodeToCheck) {
        this.nodeToCheck = nodeToCheck;
    }

    @Override
    public Node call() throws Exception {
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
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        logger.info("DEAD");
        nodeToCheck.setStatus(NodeStatus.FAILED);
        return nodeToCheck;
    }
}
