package view.game;

import controller.GameController;
import model.MapModel;
import view.FrameUtil;
import view.menu.SelectionMenuFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameFrame extends JFrame {

    private GameController controller;
    private JButton restartBtn;
    private JButton loadBtn;
    private boolean guestMode = false;
    private boolean timeAttackMode = false;

    private JLabel stepLabel;
    private JLabel timerLabel;
    private GamePanel gamePanel;
    
    // Timer components
    private Timer gameTimer;
    private long startTime;
    private long elapsedTime;
    private boolean timerRunning = false;

    public GameFrame(int width, int height, MapModel mapModel) {
        this.setTitle("Klotski Puzzle");
        this.setLayout(new BorderLayout());
        this.setSize(width, height);
        this.setMinimumSize(new Dimension(800, 600));
        
        // Initialize timer
        initializeTimer();
        
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
        
        // Stats panel for steps and timer
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Step counter
        this.stepLabel = new JLabel("Moves: 0");
        stepLabel.setFont(new Font("serif", Font.PLAIN, 18));
        stepLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(stepLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        gamePanel.setStepLabel(stepLabel);
        
        // Timer display
        this.timerLabel = new JLabel("Time: 00:00");
        timerLabel.setFont(new Font("serif", Font.PLAIN, 18));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsPanel.add(timerLabel);
        
        controlPanel.add(statsPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Action buttons
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        this.restartBtn = new JButton("Restart");
        restartBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        restartBtn.addActionListener(e -> {
            controller.restartGame();
            resetTimer();
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
                if (controller.loadGame()) {
                    // Update timer if in time attack mode
                    if (timeAttackMode) {
                        resetTimer();
                        startTimer();
                    }
                    gamePanel.requestFocusInWindow();
                }
            }
        });
        
        JButton saveBtn = new JButton("Save");
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.addActionListener(e -> {
            if (guestMode) {
                JOptionPane.showMessageDialog(this, "Guest users cannot save games");
            } else {
                controller.saveGame(timeAttackMode ? elapsedTime : 0);
                gamePanel.requestFocusInWindow();
            }
        });
        
        // Back to menu button
        JButton menuBtn = new JButton("Main Menu");
        menuBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuBtn.addActionListener(e -> {
            returnToMenu();
        });
        
        actionPanel.add(restartBtn);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        actionPanel.add(undoBtn);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        actionPanel.add(loadBtn);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        actionPanel.add(saveBtn);
        actionPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        actionPanel.add(menuBtn);
        
        controlPanel.add(actionPanel);
        
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
        // Set default close operation to return to menu
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
    
    private void initializeTimer() {
        elapsedTime = 0;
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                elapsedTime += 1000; // Increment by 1 second
                updateTimerDisplay();
            }
        });
    }
    
    public void startTimer() {
        if (!timerRunning && timeAttackMode) {
            timerRunning = true;
            startTime = System.currentTimeMillis() - elapsedTime;
            gameTimer.start();
        }
    }
    
    public void stopTimer() {
        if (timerRunning) {
            timerRunning = false;
            gameTimer.stop();
            elapsedTime = System.currentTimeMillis() - startTime;
        }
    }
    
    public void resetTimer() {
        stopTimer();
        elapsedTime = 0;
        updateTimerDisplay();
    }
    
    private void updateTimerDisplay() {
        long seconds = elapsedTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }
    
    public long getElapsedTime() {
        if (timerRunning) {
            return System.currentTimeMillis() - startTime;
        }
        return elapsedTime;
    }
    
    public void setTimeAttackMode(boolean timeAttackMode) {
        this.timeAttackMode = timeAttackMode;
        timerLabel.setVisible(timeAttackMode);
        
        if (timeAttackMode) {
            resetTimer();
            startTimer();
        } else {
            stopTimer();
        }
    }
    
    public boolean isTimeAttackMode() {
        return timeAttackMode;
    }
    
    private void returnToMenu() {
        // Stop timer if it's running
        stopTimer();
        
        // Create and show selection menu
        SelectionMenuFrame menuFrame = new SelectionMenuFrame(400, 400, 
                this.controller.getCurrentUser() != null ? this.controller.getCurrentUser() : "");
        menuFrame.setGameFrame(this);
        menuFrame.setVisible(true);
        
        // Hide game frame
        this.setVisible(false);
    }

    public void setGuestMode(boolean guestMode) {
        this.guestMode = guestMode;
        if (guestMode) {
            this.setTitle("Klotski Puzzle (Guest Mode)");
            this.loadBtn.setEnabled(false);
            
            // Find and disable Save button
            Component[] components = this.getContentPane().getComponents();
            for (Component c : components) {
                if (c instanceof JPanel) {
                    findAndSetButtonState((JPanel) c, "Save", false);
                }
            }
        } else {
            this.setTitle("Klotski Puzzle");
            this.loadBtn.setEnabled(true);
            
            // Find and enable Save button
            Component[] components = this.getContentPane().getComponents();
            for (Component c : components) {
                if (c instanceof JPanel) {
                    findAndSetButtonState((JPanel) c, "Save", true);
                }
            }
        }
    }
    
    private void findAndSetButtonState(JPanel panel, String buttonText, boolean enabled) {
        Component[] components = panel.getComponents();
        for (Component c : components) {
            if (c instanceof JButton && ((JButton)c).getText().equals(buttonText)) {
                ((JButton)c).setEnabled(enabled);
            } else if (c instanceof JPanel) {
                findAndSetButtonState((JPanel) c, buttonText, enabled);
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
