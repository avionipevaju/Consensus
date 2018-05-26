package org.raf.kids.domaci.gui;

import org.raf.kids.domaci.vo.Message;
import org.raf.kids.domaci.vo.MessageType;
import org.raf.kids.domaci.workers.Node;
import org.raf.kids.domaci.vo.NodeStatus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ControlBoard extends JFrame implements Runnable {

    private Node node;
    private JButton startButton;
    private JPanel centerPanel;
    private HashMap<Integer, JLabel> labelList;

    public ControlBoard(String title, Node node) throws HeadlessException, UnknownHostException {
        super(title);
        this.node = node;
        this.labelList = new HashMap<>();
        initFrame();
        initComponents();
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                node.sendMessage(node.getCheckingNode(), new Message(node.getCheckingNode().getId(), MessageType.CLOSE_STATUS_CHECK, "close"));
                node.deactivateNode();
                System.exit(0);
            }
        });
    }

    private void initFrame() {
        setSize(400,150);
        setBackground(Color.white);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initComponents() throws UnknownHostException {
        centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setMaximumSize(new Dimension(200,100));
        centerPanel.setBackground(Color.gray);
        String name = String.format("Node %d, ip: %s port: %d", node.getId(), Inet4Address.getLocalHost().getHostAddress(), node.getCommunicationPort());
        JLabel label = new JLabel(name);
        centerPanel.add(label);
        startButton = new JButton("Start Node");
        startButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                node.activateNode();
                centerPanel.setBackground(Color.GREEN);
            }
        });
        centerPanel.add(startButton, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        JPanel neighbourPanel = new JPanel();
        neighbourPanel.setLayout(new GridLayout(10,1));
        neighbourPanel.setBackground(Color.white);
        neighbourPanel.add(new JLabel("Neighbours"));
        for(Node node: node.getNeighbours()) {
            String marker = String.format("Node %s, ip: %s port: %s", node.getId(), node.getIp(), node.getCommunicationPort());
            JLabel neighbourLabel = new JLabel(marker);
            neighbourLabel.setForeground(Color.gray);
            labelList.put(node.getId(), neighbourLabel);
            neighbourPanel.add(neighbourLabel);

        }
        add(neighbourPanel, BorderLayout.EAST);
    }

    @Override
    public void run() {
        List<Node> nodes = node.getNeighbours();
        while (true) {
            for (Node node: nodes) {
                NodeStatus nodeStatus = node.getStatus();
                JLabel label = labelList.get(node.getId());
                switch (nodeStatus) {
                    case NOT_STARTED:
                        label.setForeground(Color.gray);
                        break;
                    case ACTIVE:
                        label.setForeground(Color.green);
                        break;
                    case SUSPECTED_FAILURE:
                        label.setForeground(Color.yellow);
                        break;
                    case FAILED:
                        label.setForeground(Color.red);
                        break;
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
