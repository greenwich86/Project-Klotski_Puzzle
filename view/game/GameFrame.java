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
        this.setLayout(new BorderLayout());
        this.setSize(width, height);
        this.setMinimumSize(new Dimension(800, 600));
        
        // Main panel with game board
        try {
            gamePanel = new GamePanel(mapModel);
            JScrollPane scrollPane = new JScrollPane(gamePanel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            this.add(scrollPane, BorderLayout.CENTER);
            
            this.controller = new GameController(gamePanel, mapModel);
            this.controller.restartGame();
            gamePanel.requestFocusInWindow();
        } catch (Exception e) {
            System.err.println("Error creating GamePanel:");
            e.printStackTrace();
            throw e;
        }

        // Control panel on the right
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Step counter
        this.stepLabel = new JLabel("Start");
        stepLabel.setFont(new Font("serif", Font.ITALIC, 22));
        stepLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(stepLabel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        gamePanel.setStepLabel(stepLabel);

        // Level buttons
        JPanel levelPanel = new JPanel();
        levelPanel.setLayout(new BoxLayout(levelPanel, BoxLayout.Y_AXIS));
        levelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        for (int i = 0; i < 3; i++) {
            final int level = i;
            JButton levelBtn = new JButton("Level " + (i+1));
            levelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            levelBtn.addActionListener(e -> {
                controller.setLevel(level);
                gamePanel.requestFocusInWindow();
            });
            levelPanel.add(levelBtn);
            levelPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        controlPanel.add(levelPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Action buttons
        this.restartBtn = new JButton("Restart");
        restartBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        restartBtn.addActionListener(e -> {
            controller.restartGame();
            gamePanel.requestFocusInWindow();
        });
        
        JButton undoBtn = new JButton("Undo");
        undoBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        undoBtn.addActionListener(e -> {
            controller.undoMove();
            gamePanel.requestFocusInWindow();
        });
        
        this.loadBtn = new JButton("Load");
        loadBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadBtn.addActionListener(e -> {
            if (guestMode) {
                JOptionPane.showMessageDialog(this, "Guest users cannot load games");
            } else {
                controller.loadGame();
                gamePanel.requestFocusInWindow();
            }
        });
        
        JButton saveBtn = new JButton("Save");
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.addActionListener(e -> {
            if (guestMode) {
                JOptionPane.showMessageDialog(this, "Guest users cannot save games");
            } else {
                controller.saveGame();
                gamePanel.requestFocusInWindow();
            }
        });
        
        controlPanel.add(restartBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(undoBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        controlPanel.add(loadBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(saveBtn);
        
        this.add(controlPanel, BorderLayout.EAST);
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
