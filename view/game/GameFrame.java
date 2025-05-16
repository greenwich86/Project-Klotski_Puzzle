package view.game;

import controller.GameController;
import model.AISolver;
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
    private SelectionMenuFrame parentFrame; // Reference to the menu
    private boolean guestMode = false;
    private boolean timeAttackMode = false;
    private int timeLimit = 0; // Time limit in minutes
    private JLabel stepLabel;
    private JLabel timerLabel;
    private GamePanel gamePanel;
    private Timer countdownTimer;
    private PropPanel propPanel;

    public GameFrame(int width, int height, MapModel mapModel) {
        this(width, height, mapModel, null);
    }
    
    public GameFrame(int width, int height, MapModel mapModel, SelectionMenuFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.setTitle("2025 CS109 Project Demo");
        this.setLayout(new BorderLayout());
        this.setSize(Math.max(width, 900), Math.max(height, 750));
        this.setMinimumSize(new Dimension(900, 750));
        
        // Main panel with game board
        try {
            gamePanel = new GamePanel(mapModel);
            JScrollPane scrollPane = new JScrollPane(gamePanel);
            // Disable scrollbars to ensure entire board is visible without scrolling
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
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
        
        // Create props panel with more prominence and visibility
        propPanel = new PropPanel(controller, this);
        propPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        propPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(100, 100, 255), 3),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        propPanel.setBackground(new Color(230, 230, 255));
        propPanel.setOpaque(true);
        propPanel.setVisible(MapModel.LEVEL_PROPS_ALLOWED[controller.getCurrentLevel()]);
        // Set minimum size to ensure visibility
        propPanel.setMinimumSize(new Dimension(200, 180));
        propPanel.setPreferredSize(new Dimension(220, 200));
        
        // Step counter and timer
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        this.stepLabel = new JLabel("Step: 0");
        stepLabel.setFont(new Font("serif", Font.ITALIC, 22));
        stepLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        this.timerLabel = new JLabel("");
        timerLabel.setFont(new Font("serif", Font.BOLD, 22));
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timerLabel.setForeground(Color.RED);
        timerLabel.setVisible(false);
        
        statsPanel.add(stepLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(timerLabel);
        
        controlPanel.add(statsPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Add the props panel
        controlPanel.add(propPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        gamePanel.setStepLabel(stepLabel);

        // AI button to automatically solve puzzle
        JButton aiButton = new JButton("AI Solve");
        aiButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        aiButton.addActionListener(e -> {
            // Create a new AI solver and run it
            AISolver solver = new AISolver(controller.getModel(), controller);
            
            // Show loading message
            JLabel statusLabel = new JLabel("AI solving puzzle...");
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            
            JPanel progressPanel = new JPanel(new BorderLayout(10, 10));
            progressPanel.add(statusLabel, BorderLayout.NORTH);
            progressPanel.add(progressBar, BorderLayout.CENTER);
            
            JDialog loadingDialog = new JDialog(this, "AI Solver", false);
            loadingDialog.setLayout(new BorderLayout());
            loadingDialog.add(progressPanel, BorderLayout.CENTER);
            loadingDialog.setSize(250, 100);
            loadingDialog.setLocationRelativeTo(this);
            
            // Start solving in a separate thread to keep UI responsive
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return solver.findSolution();
                }
                
                @Override
                protected void done() {
                    try {
                        boolean solutionFound = get();
                        loadingDialog.dispose();
                        
                        if (solutionFound) {
                            int solutionLength = solver.getSolutionLength();
                            int option = JOptionPane.showConfirmDialog(
                                GameFrame.this,
                                "Solution found with " + solutionLength + " moves!\nExecute solution?",
                                "AI Solution",
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            
                            if (option == JOptionPane.YES_OPTION) {
                                solver.executeSolution();
                            }
                        } else {
                            JOptionPane.showMessageDialog(
                                GameFrame.this,
                                "No solution found for the current puzzle state.",
                                "AI Solver",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    } catch (Exception ex) {
                        loadingDialog.dispose();
                        JOptionPane.showMessageDialog(
                            GameFrame.this,
                            "Error solving puzzle: " + ex.getMessage(),
                            "AI Solver Error",
                            JOptionPane.ERROR_MESSAGE
                        );
                        ex.printStackTrace();
                    }
                    
                    gamePanel.requestFocusInWindow();
                }
            }.execute();
            
            loadingDialog.setVisible(true);
        });
        controlPanel.add(aiButton);
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
        
        // Return to Menu button
        JButton returnToMenuBtn = new JButton("Return to Menu");
        returnToMenuBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        returnToMenuBtn.addActionListener(e -> {
            returnToMenu();
        });
        
        controlPanel.add(restartBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(undoBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        controlPanel.add(loadBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(saveBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        controlPanel.add(returnToMenuBtn);
        
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

    /**
     * Returns to the main menu screen
     */
    private void returnToMenu() {
        if (parentFrame != null) {
            // Stop timer if running
            if (countdownTimer != null && countdownTimer.isRunning()) {
                countdownTimer.stop();
            }
            
            // Show the menu frame and hide this frame
            parentFrame.setVisible(true);
            this.setVisible(false);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Cannot return to menu: Menu reference not available.", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
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
    
    public void setParentFrame(SelectionMenuFrame parentFrame) {
        this.parentFrame = parentFrame;
    }
    
    /**
     * Updates the visibility of the prop panel based on the current level
     * 
     * @param level The current level (0-3)
     */
    public void updatePropPanelVisibility(int level) {
        if (propPanel != null) {
            boolean propsAllowed = MapModel.LEVEL_PROPS_ALLOWED[level];
            propPanel.setVisible(true); // Always show prop panel for UI consistency
            
            if (!propsAllowed) {
                // For levels with no props, display information but keep panel visible
                if (level == 3) { // Master level
                    propPanel.setToolTipText("Props are disabled in Master difficulty");
                    propPanel.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.RED, 2),
                        "Props (Disabled in Master)"
                    ));
                } else { // Easy level
                    propPanel.setToolTipText("Props are disabled in Easy difficulty");
                    propPanel.setBorder(BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(Color.GRAY, 2),
                        "Props (Disabled in Easy)"
                    ));
                }
            } else {
                // Show active prop panel for Hard and Expert levels
                propPanel.setToolTipText("Use props to help solve the puzzle");
                propPanel.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(100, 100, 255), 3),
                    "Available Props"
                ));
                
                // Reinitialize controller's props to ensure they're properly set up
                controller.initializeProps(level);
            }
            
            // Always update prop availability regardless of level
            propPanel.updatePropAvailability();
            
            // Force repaint and revalidation
            propPanel.revalidate();
            propPanel.repaint();
        }
    }
    
    // Store remaining time
    private int currentTimeLeft = 0;
    
    /**
     * Adds time to the countdown timer (for Time Bonus prop)
     * 
     * @param secondsToAdd Number of seconds to add to the timer
     * @return True if time was added successfully, false if not in time attack mode
     */
    public boolean addTimeToTimer(int secondsToAdd) {
        if (!timeAttackMode || countdownTimer == null || !countdownTimer.isRunning()) {
            return false;
        }
        
        // Add the time to our currentTimeLeft field
        currentTimeLeft += secondsToAdd;
        
        // Update the timer display
        updateTimerDisplay(currentTimeLeft);
        
        // Show notification
        JOptionPane.showMessageDialog(this,
            "Time Bonus: " + secondsToAdd + " seconds added!",
            "Time Bonus",
            JOptionPane.INFORMATION_MESSAGE);
        
        return true;
    }

    /**
     * Enables or disables time attack mode with a specified time limit.
     * 
     * @param enabled Whether time attack mode is enabled
     * @param minutes Time limit in minutes (3, 5, or 7)
     */
    public void setTimeAttackMode(boolean enabled, int minutes) {
        this.timeAttackMode = enabled;
        this.timeLimit = minutes;
        
        // Stop any existing timer
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        
        if (enabled) {
            // Show timer label
            timerLabel.setVisible(true);
            
            // Convert minutes to seconds
            final int[] secondsLeft = { minutes * 60 };
            
            // Format and display initial time
            updateTimerDisplay(secondsLeft[0]);
            
            // Create and start countdown timer
            countdownTimer = new Timer(1000, e -> {
                secondsLeft[0]--;
                updateTimerDisplay(secondsLeft[0]);
                
                // Change color when time is running low
                if (secondsLeft[0] <= 60) {
                    timerLabel.setForeground(Color.RED);
                } else {
                    timerLabel.setForeground(Color.BLACK);
                }
                
                // Game over when time runs out
                if (secondsLeft[0] <= 0) {
                    ((Timer)e.getSource()).stop();
                    timeAttackGameOver();
                }
            });
            
            countdownTimer.start();
        } else {
            // Hide timer label in normal mode
            timerLabel.setVisible(false);
        }
    }
    
    /**
     * Updates the timer display with formatted time
     */
    private void updateTimerDisplay(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        timerLabel.setText(String.format("Time: %02d:%02d", mins, secs));
    }
    
    /**
     * Handles game over when time runs out in Time Attack Mode
     */
    private void timeAttackGameOver() {
        // Flash timer label
        Timer flashTimer = new Timer(250, new ActionListener() {
            private int count = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (count % 2 == 0) {
                    timerLabel.setForeground(Color.WHITE);
                    timerLabel.setBackground(Color.RED);
                    timerLabel.setOpaque(true);
                } else {
                    timerLabel.setForeground(Color.RED);
                    timerLabel.setBackground(null);
                    timerLabel.setOpaque(false);
                }
                count++;
                if (count > 10) {
                    ((Timer)e.getSource()).stop();
                    showTimeAttackGameOver();
                }
            }
        });
        flashTimer.start();
    }
    
    /**
     * Shows game over dialog and returns to the menu
     */
    private void showTimeAttackGameOver() {
        JOptionPane.showMessageDialog(this,
                "<html><h2>TIME'S UP!</h2><br>You ran out of time!</html>",
                "Game Over",
                JOptionPane.ERROR_MESSAGE);
        
        // Instead of restarting, return to the menu
        if (parentFrame != null) {
            // Stop timer if running
            if (countdownTimer != null && countdownTimer.isRunning()) {
                countdownTimer.stop();
            }
            
            // Show the menu frame and hide this frame
            parentFrame.setVisible(true);
            this.setVisible(false);
        } else {
            // If no parent frame, just restart
            controller.restartGame();
            
            // Reset and restart timer
            if (timeAttackMode) {
                setTimeAttackMode(true, timeLimit);
            }
        }
    }
}
