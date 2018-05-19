package org.raf.kids.domaci;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.Inet4Address;
import java.net.UnknownHostException;

public class ControlBoard extends JFrame {

    private Node node;
    private JButton startButton, addNeighbourButton;
    private JPanel centerPanel;

    public ControlBoard(String title, Node node) throws HeadlessException, UnknownHostException {
        super(title);
        this.node = node;
        initFrame();
        initComponents();
    }

    private void initFrame() {
        setSize(400,400);
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
        String name = String.format("Node %d, ip: %s port: %d", node.getId(), Inet4Address.getLocalHost().getHostAddress(), node.getPort());
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
        addNeighbourButton =  new JButton("Add neighbour node");
        centerPanel.add(addNeighbourButton, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);

        JPanel neighbourPanel = new JPanel();
        neighbourPanel.setLayout(new GridLayout(10,1));
        neighbourPanel.setBackground(Color.white);
        neighbourPanel.add(new JLabel("Neighbours"));
        neighbourPanel.add(new JLabel(node.getNeighbours().get(0).getIp()));
        add(neighbourPanel, BorderLayout.EAST);
    }
}
