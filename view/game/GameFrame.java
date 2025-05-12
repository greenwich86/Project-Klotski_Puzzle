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
//            System.err.println("Creating GameController...");
            this.controller = new GameController(gamePanel, mapModel);
//            System.err.println("Controller created: " + controller);
            this.controller.restartGame(); // Initialize game state
            gamePanel.requestFocusInWindow(); // Ensure focus for key events
        } catch (Exception e) {
            System.err.println("Error creating GamePanel:");
            e.printStackTrace();
            throw e;
        }

        // Create level selection buttons
        int buttonX = gamePanel.getWidth() + 80;
        int buttonY = 30;
        for (int i = 0; i < 3; i++) {
            final int level = i;
            JButton levelBtn = FrameUtil.createButton(this, "Level " + (i+1), 
                new Point(buttonX+180, buttonY), 80, 30);
            levelBtn.addActionListener(e -> {
                controller.setLevel(level);
                gamePanel.requestFocusInWindow();
            });
            buttonY += 35;
        }

        // Create undo button next to restart button
        buttonY = 150;
        this.restartBtn = FrameUtil.createButton(this, "Restart", new Point(buttonX, buttonY), 80, 50);
        JButton undoBtn = FrameUtil.createButton(this, "Undo", new Point(buttonX + 100, buttonY), 80, 50);
        undoBtn.addActionListener(e -> {
            controller.undoMove();
            gamePanel.requestFocusInWindow();
        });
        // Create Save button next to Load button
        this.loadBtn = FrameUtil.createButton(this, "Load", new Point(gamePanel.getWidth() + 80, 220), 80, 50);
        JButton saveBtn = FrameUtil.createButton(this, "Save", new Point(gamePanel.getWidth() + 80, 290), 80, 50);
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
                controller.loadGame();
                gamePanel.requestFocusInWindow();
            }
        });
        saveBtn.addActionListener(e -> {
            if (guestMode) {
                JOptionPane.showMessageDialog(this, "Guest users cannot save games");
            } else {
                controller.saveGame();
                gamePanel.requestFocusInWindow();
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
            // Also disable Save button in guest mode
            for (Component c : this.getContentPane().getComponents()) {
                if (c instanceof JButton && ((JButton)c).getText().equals("Save")) {
                    ((JButton)c).setEnabled(false);
                }
            }
        } else {
            this.setTitle("2025 CS109 Project Demo");
            this.loadBtn.setEnabled(true);
            // Enable Save button when not in guest mode
            for (Component c : this.getContentPane().getComponents()) {
                if (c instanceof JButton && ((JButton)c).getText().equals("Save")) {
                    ((JButton)c).setEnabled(true);
                }
            }
        }
    }

    public boolean isGuestMode() {
        return guestMode;
    }
    
    public GameController getController() {
        return controller;
    }
}
