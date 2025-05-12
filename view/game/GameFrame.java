package view.game;

import controller.GameController;
import model.MapModel;
import view.FrameUtil;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {

    private GameController controller;
    private JButton restartBtn;
    private JButton loadBtn;
    private boolean guestMode = false;

    private JLabel stepLabel;
    private GamePanel gamePanel;

    public GameFrame(int width, int height, MapModel mapModel) {
        this.setTitle("2025 CS109 Project Demo");
        this.setLayout(null);
        this.setSize(width, height);
        
        // Debug model dimensions before creating GamePanel
        System.out.println("GameFrame - Model dimensions: " + 
            mapModel.getHeight() + "x" + mapModel.getWidth());
        
        try {
            gamePanel = new GamePanel(mapModel);
            gamePanel.setLocation(30, height / 2 - gamePanel.getHeight() / 2);
            this.add(gamePanel);
            System.err.println("Creating GameController...");
            this.controller = new GameController(gamePanel, mapModel);
            System.err.println("Controller created: " + controller);
            this.controller.restartGame(); // Initialize game state
            gamePanel.requestFocusInWindow(); // Ensure focus for key events
        } catch (Exception e) {
            System.err.println("Error creating GamePanel:");
            e.printStackTrace();
            throw e;
        }

        // Create undo button next to restart button
        int buttonX = gamePanel.getWidth() + 30;
        this.restartBtn = FrameUtil.createButton(this, "Restart", new Point(buttonX, 120), 80, 50);
        JButton undoBtn = FrameUtil.createButton(this, "Undo", new Point(buttonX + 100, 120), 80, 50);
        undoBtn.addActionListener(e -> {
            controller.undoMove();
            gamePanel.requestFocusInWindow();
        });
        this.loadBtn = FrameUtil.createButton(this, "Load", new Point(gamePanel.getWidth() + 80, 210), 80, 50);
        this.stepLabel = FrameUtil.createJLabel(this, "Start", new Font("serif", Font.ITALIC, 22), new Point(gamePanel.getWidth() + 80, 70), 180, 50);
        gamePanel.setStepLabel(stepLabel);

        this.restartBtn.addActionListener(e -> {
            controller.restartGame();
            gamePanel.requestFocusInWindow();//enable key listener
        });
        this.loadBtn.addActionListener(e -> {
            if (guestMode) {
                JOptionPane.showMessageDialog(this, "Guest users cannot load games");
            } else {
                String string = JOptionPane.showInputDialog(this, "Input path:");
                System.out.println(string);
                gamePanel.requestFocusInWindow();//enable key listener
            }
        });
        //todo: add other button here
        this.setLocationRelativeTo(null);
        
        // Add window listener to handle focus when frame becomes active
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent e) {
                gamePanel.requestFocusInWindow();
            }
        });
        
        // Initial focus requests
        this.requestFocusInWindow();
        gamePanel.requestFocusInWindow();
    }

    public void setGuestMode(boolean guestMode) {
        this.guestMode = guestMode;
        if (guestMode) {
            this.setTitle("2025 CS109 Project Demo (Guest Mode)");
            this.loadBtn.setEnabled(false);
        }
    }

    public boolean isGuestMode() {
        return guestMode;
    }
}
