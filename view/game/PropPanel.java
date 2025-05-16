package view.game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        // Update Hint prop
        int hintCount = controller.getPropCount(Prop.PropType.HINT);
        hintCountLabel.setText(String.valueOf(hintCount));
        hintButton.setEnabled(hintCount > 0);
        
        // Update Time Bonus prop
        int timeBonusCount = controller.getPropCount(Prop.PropType.TIME_BONUS);
        timeBonusCountLabel.setText(String.valueOf(timeBonusCount));
        timeBonusButton.setEnabled(timeBonusCount > 0);
        
        // Update Obstacle Remover prop
        int obstacleRemoverCount = controller.getPropCount(Prop.PropType.OBSTACLE_REMOVER);
        obstacleRemoverCountLabel.setText(String.valueOf(obstacleRemoverCount));
        obstacleRemoverButton.setEnabled(obstacleRemoverCount > 0 || obstacleRemoverActive);
        
        // Update button appearance based on active state
        if (obstacleRemoverActive) {
            obstacleRemoverButton.setBackground(new Color(255, 200, 200));
            obstacleRemoverButton.setText("Select Obstacle");
        } else {
            obstacleRemoverButton.setBackground(null);
            obstacleRemoverButton.setText("Obstacle Remover");
        }
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
        updatePropAvailability();
    }
    
    public boolean isObstacleRemoverActive() {
        return obstacleRemoverActive;
    }
    
    public void useObstacleRemoverAt(int row, int col) {
        if (obstacleRemoverActive) {
            boolean success = controller.useObstacleRemoverProp(row, col);
            obstacleRemoverActive = false;
            updatePropAvailability();
        }
    }
    
    public void dispose() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }
}
