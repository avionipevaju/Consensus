package org.raf.kids.domaci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.net.UnknownHostException;

public class Main {

    private static Logger logger = LoggerFactory.getLogger(Main.class);
    private static Node node;

    public static void main(String[] args) {
        loadNode();
        //node.activateNode();
    }

    private static void loadNode() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    node = ConfigurationUtils.loadJsonConfiguration("src/main/resources/configuration.json");
                    logger.info("Loaded node: {} ", node);
                    ControlBoard controlBoard =  new ControlBoard("Node " + String.valueOf(node.getId()), node);
                } catch (IOException e) {
                    logger.error("Error loading and running node: ", e.getMessage());
                }
            }
        });
}
}
