package view.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.MapModel;
import model.Prop;
import controller.GameController;

/**
 * UI Panel that displays available props and allows the player to use them
 */
public class PropPanel extends JPanel {
    private final GameController controller;
    private final GameFrame gameFrame;
    
    private JButton hintButton;
    private JButton timeBonusButton;
    private JButton obstacleRemoverButton;
    
    private JLabel hintCountLabel;
    private JLabel timeBonusCountLabel;
    private JLabel obstacleRemoverCountLabel;
    
    private Timer updateTimer;
    private boolean obstacleRemoverActive = false;
    
    public PropPanel(GameController controller, GameFrame gameFrame) {
        this.controller = controller;
        this.gameFrame = gameFrame;
        
        initializeUI();
        startUpdateTimer();
    }
    
    private void initializeUI() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createTitledBorder("Props"));
        
        // Hint prop
        JPanel hintPanel = createPropPanel(
            "Hint", 
            "Shows the next best move", 
            e -> useHintProp());
        hintButton = (JButton) hintPanel.getComponent(0);
        hintCountLabel = (JLabel) hintPanel.getComponent(1);
        this.add(hintPanel);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Time Bonus prop
        JPanel timeBonusPanel = createPropPanel(
            "Time +30s", 
            "Adds 30 seconds to the timer", 
            e -> useTimeBonusProp());
        timeBonusButton = (JButton) timeBonusPanel.getComponent(0);
        timeBonusCountLabel = (JLabel) timeBonusPanel.getComponent(1);
        this.add(timeBonusPanel);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Obstacle Remover prop
        JPanel obstacleRemoverPanel = createPropPanel(
            "Obstacle Remover", 
            "Temporarily removes an obstacle", 
            e -> toggleObstacleRemoverMode());
        obstacleRemoverButton = (JButton) obstacleRemoverPanel.getComponent(0);
        obstacleRemoverCountLabel = (JLabel) obstacleRemoverPanel.getComponent(1);
        this.add(obstacleRemoverPanel);
        
        updatePropAvailability();
    }
    
    private JPanel createPropPanel(String name, String tooltip, ActionListener action) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setMaximumSize(new Dimension(200, 50));
        
        JButton button = new JButton(name);
        button.setToolTipText(tooltip);
        button.addActionListener(action);
        
        JLabel countLabel = new JLabel("0");
        countLabel.setHorizontalAlignment(SwingConstants.CENTER);
        countLabel.setPreferredSize(new Dimension(30, 20));
        countLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        panel.add(button, BorderLayout.CENTER);
        panel.add(countLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void startUpdateTimer() {
        updateTimer = new Timer(500, e -> updatePropAvailability());
        updateTimer.start();
    }
    
    public void updatePropAvailability() {
        boolean anyPropsAvailable = false;
        
        // Get current level info
        int currentLevel = controller.getCurrentLevel();
        boolean propsAllowed = MapModel.LEVEL_PROPS_ALLOWED[currentLevel];
        
//        System.out.println("PropPanel updating prop availability - level: " +
//                         currentLevel + ", props allowed: " + propsAllowed);
        
        // Update Hint prop
        int hintCount = controller.getPropCount(Prop.PropType.HINT);
        hintCountLabel.setText(String.valueOf(hintCount));
        hintButton.setEnabled(hintCount > 0);
        if (hintCount > 0) anyPropsAvailable = true;
        
        // Update Time Bonus prop
        int timeBonusCount = controller.getPropCount(Prop.PropType.TIME_BONUS);
        timeBonusCountLabel.setText(String.valueOf(timeBonusCount));
        timeBonusButton.setEnabled(timeBonusCount > 0);
        if (timeBonusCount > 0) anyPropsAvailable = true;
        
        // Update Obstacle Remover prop
        int obstacleRemoverCount = controller.getPropCount(Prop.PropType.OBSTACLE_REMOVER);
        obstacleRemoverCountLabel.setText(String.valueOf(obstacleRemoverCount));
        obstacleRemoverButton.setEnabled(obstacleRemoverCount > 0 || obstacleRemoverActive);
        if (obstacleRemoverCount > 0) anyPropsAvailable = true;
        
        // Update button appearance based on active state
        if (obstacleRemoverActive) {
            obstacleRemoverButton.setBackground(new Color(255, 200, 200));
            obstacleRemoverButton.setText("Select Obstacle");
        } else {
            obstacleRemoverButton.setBackground(null);
            obstacleRemoverButton.setText("Obstacle Remover");
        }
        
        // Set overall panel border based on whether props are enabled
        if (propsAllowed) {
            if (anyPropsAvailable) {
                this.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(100, 100, 255), 3),
                    "Available Props"
                ));
            } else {
                this.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(new Color(100, 100, 255), 2),
                    "Props (All Used)"
                ));
            }
        } else {
            // Easy or Master level - props disabled
            if (currentLevel == 0) { // Easy
                this.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 2),
                    "Props (Disabled in Easy)"
                ));
            } else { // Master
                this.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.RED, 2),
                    "Props (Disabled in Master)"
                ));
            }
        }
        
        // Force repaint to reflect changes
        this.revalidate();
        this.repaint();
    }
    
    private void useHintProp() {
        boolean success = controller.useHintProp();
        if (success) {
            updatePropAvailability();
        }
    }
    
    private void useTimeBonusProp() {
        boolean success = controller.useTimeBonusProp(gameFrame);
        if (success) {
            updatePropAvailability();
        }
    }
    
    private void toggleObstacleRemoverMode() {
        // Use new direct approach through GameFrame
        if (gameFrame != null) {
            // Call GameFrame's direct obstacle removal method
            System.out.println("PropPanel: Using direct obstacle removal through GameFrame");
            gameFrame.handleObstacleRemoval();
        } else {
            // Fallback to old approach if GameFrame is not available
            System.out.println("PropPanel: WARNING - GameFrame is null, cannot use direct approach");
            
            if (obstacleRemoverActive) {
                // Deactivate obstacle remover mode
                obstacleRemoverActive = false;
                JOptionPane.showMessageDialog(this, 
                    "Obstacle remover mode canceled.",
                    "Obstacle Remover", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Activate obstacle remover mode
                obstacleRemoverActive = true;
                JOptionPane.showMessageDialog(this,
                    "Obstacle remover activated. Click on an obstacle to remove it temporarily.",
                    "Obstacle Remover",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        }
        updatePropAvailability();
    }
    
    public boolean isObstacleRemoverActive() {
        return obstacleRemoverActive;
    }
    
    /**
     * Resets the obstacle remover mode to inactive
     */
    public void resetObstacleRemoverMode() {
        System.out.println("PropPanel: Resetting obstacle remover mode to inactive");
        this.obstacleRemoverActive = false;
        updatePropAvailability();
    }
    
    public void useObstacleRemoverAt(int row, int col) {
        System.out.println("PropPanel: useObstacleRemoverAt called for [" + row + "," + col + "]");
        System.out.println("PropPanel: obstacleRemoverActive = " + obstacleRemoverActive);
        
        if (obstacleRemoverActive) {
            System.out.println("PropPanel: Attempting to remove obstacle at [" + row + "," + col + "]");
            
            // Get the block type at the position from the model
            int blockType = controller.getModel().getId(row, col);
            System.out.println("PropPanel: Block type at [" + row + "," + col + "] = " + blockType);
            
            // Force the blockType to BLOCKED if it's in the valid range
            // Only call obstacle remover if we're sure it's a blocked piece
            if (blockType == MapModel.BLOCKED) {
                // Direct call to controller with debug output
                boolean success = controller.useObstacleRemoverProp(row, col);
                System.out.println("PropPanel: Obstacle removal result: " + success);
                
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Obstacle successfully removed temporarily!",
                        "Obstacle Remover",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    System.out.println("PropPanel: Failed to remove obstacle at [" + row + "," + col + "]");
                    JOptionPane.showMessageDialog(this,
                        "Could not remove obstacle. Make sure you've clicked on a gray obstacle piece.",
                        "Obstacle Remover",
                        JOptionPane.WARNING_MESSAGE);
                }
            } else {
                // Not a valid obstacle
                System.out.println("PropPanel: Not a valid obstacle at [" + row + "," + col + "]");
                JOptionPane.showMessageDialog(this,
                    "The obstacle remover can only be used on gray obstacle pieces.",
                    "Obstacle Remover",
                    JOptionPane.WARNING_MESSAGE);
            }
            
            // Always reset obstacle remover mode
            obstacleRemoverActive = false;
            updatePropAvailability();
        } else {
            System.out.println("PropPanel: Obstacle remover not active, ignoring click");
        }
    }
    
    public void dispose() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }
}
