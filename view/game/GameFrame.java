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
        this.setTitle("Klotski Puzzle - 2025 CS109 Project Demo");
        this.setLayout(null);
        this.setSize(width, height);
        
        // Set window icon
        ImageIcon icon = new ImageIcon("resources/klotski_icon.png");
        this.setIconImage(icon.getImage());
        
        // Set window background
        this.getContentPane().setBackground(new Color(245, 235, 200));
        
        // Debug model dimensions before creating GamePanel
        System.out.println("GameFrame - Model dimensions: " + 
            mapModel.getHeight() + "x" + mapModel.getWidth());
        
        try {
            gamePanel = new GamePanel(mapModel);
            gamePanel.setLocation(150, height / 2 - gamePanel.getHeight() / 2); // Increased spacing to 150px
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

        // Create control panel for buttons with more spacing
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(0, 1, 0, 15));
        controlPanel.setBounds(gamePanel.getWidth() + 150, 70, 180, 300); // Increased spacing to 150px
        controlPanel.setOpaque(false);
        
        // Step counter
        this.stepLabel = new JLabel("Steps: 0", SwingConstants.CENTER);
        this.stepLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        this.stepLabel.setForeground(new Color(101, 67, 33));
        // Ensure label can expand for larger numbers
        this.stepLabel.setPreferredSize(new Dimension(180, 30));
        controlPanel.add(stepLabel);
        
        // Game control buttons
        JPanel gameControls = new JPanel(new GridLayout(1, 2, 10, 0));
        gameControls.setOpaque(false);
        this.restartBtn = createThemedButton("Restart", new Point(0, 0), 80, 40);
        JButton undoBtn = createThemedButton("Undo", new Point(0, 0), 80, 40);
        undoBtn.addActionListener(e -> {
            controller.undoMove();
            gamePanel.requestFocusInWindow();
        });
        gameControls.add(restartBtn);
        gameControls.add(undoBtn);
        controlPanel.add(gameControls);
        
        // Save/Load buttons
        this.loadBtn = createThemedButton("Load Game", new Point(0, 0), 160, 40);
        JButton saveBtn = createThemedButton("Save Game", new Point(0, 0), 160, 40);
        controlPanel.add(loadBtn);
        controlPanel.add(saveBtn);
        
        this.add(controlPanel);
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

    private JButton createThemedButton(String text, Point location, int width, int height) {
        JButton button = new JButton(text);
        button.setBounds(location.x, location.y, width, height);
        button.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        button.setForeground(new Color(101, 67, 33));
        button.setBackground(new Color(210, 180, 140));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(139, 69, 19), 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        button.setFocusPainted(false);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(220, 190, 150));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(210, 180, 140));
            }
        });
        
        this.add(button);
        return button;
    }

    public void setGuestMode(boolean guestMode) {
        this.guestMode = guestMode;
        if (guestMode) {
            this.setTitle("Klotski Puzzle - 2025 CS109 Project Demo (Guest Mode)");
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
