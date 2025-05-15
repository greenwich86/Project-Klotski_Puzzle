package view.menu;

import view.FrameUtil;
import view.game.GameFrame;
import model.MapModel;

import javax.swing.*;
import java.awt.*;

/**
 * This frame allows players to select game settings before starting:
 * - Difficulty level (1-3)
 * - Time Attack Mode (on/off)
 */
public class GameSettingsFrame extends JFrame {
    private final GameFrame gameFrame;
    private final SelectionMenuFrame parentFrame;
    
    private JRadioButton[] levelButtons;
    private JCheckBox timerCheckbox;
    
    public GameSettingsFrame(int width, int height, GameFrame gameFrame, SelectionMenuFrame parentFrame) {
        this.gameFrame = gameFrame;
        this.parentFrame = parentFrame;
        
        this.setTitle("Game Settings");
        this.setLayout(new BorderLayout());
        this.setSize(width, height);
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Game Settings");
        titleLabel.setFont(new Font("serif", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        
        // Settings panel
        JPanel settingsPanel = new JPanel();
        settingsPanel.setLayout(new BoxLayout(settingsPanel, BoxLayout.Y_AXIS));
        settingsPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        // Difficulty selection
        JPanel difficultyPanel = new JPanel();
        difficultyPanel.setLayout(new BoxLayout(difficultyPanel, BoxLayout.Y_AXIS));
        difficultyPanel.setBorder(BorderFactory.createTitledBorder("Difficulty Level"));
        difficultyPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Create level buttons
        levelButtons = new JRadioButton[MapModel.LEVELS.length];
        ButtonGroup levelGroup = new ButtonGroup();
        
        for (int i = 0; i < MapModel.LEVELS.length; i++) {
            levelButtons[i] = new JRadioButton("Level " + (i + 1));
            levelButtons[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            if (i == 0) {
                levelButtons[i].setSelected(true);
            }
            levelGroup.add(levelButtons[i]);
            difficultyPanel.add(levelButtons[i]);
            difficultyPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }
        
        // Time Attack mode
        JPanel timerPanel = new JPanel();
        timerPanel.setLayout(new BoxLayout(timerPanel, BoxLayout.Y_AXIS));
        timerPanel.setBorder(BorderFactory.createTitledBorder("Game Mode"));
        timerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        this.timerCheckbox = new JCheckBox("Time Attack Mode");
        timerCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        timerPanel.add(timerCheckbox);
        
        // Info label
        JLabel timerInfoLabel = new JLabel("<html>Challenge yourself to complete<br>the puzzle as quickly as possible!</html>");
        timerInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timerInfoLabel.setFont(new Font("serif", Font.ITALIC, 12));
        timerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        timerPanel.add(timerInfoLabel);
        
        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(e -> startGame());
        
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> goBack());
        
        buttonPanel.add(backButton);
        buttonPanel.add(startButton);
        
        // Add panels to settings
        settingsPanel.add(difficultyPanel);
        settingsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        settingsPanel.add(timerPanel);
        
        // Add panels to frame
        this.add(titlePanel, BorderLayout.NORTH);
        this.add(settingsPanel, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);
        
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        // Handle window close event
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                parentFrame.setVisible(true);
            }
        });
    }
    
    private void startGame() {
        // Get selected level
        int selectedLevel = 0;
        for (int i = 0; i < levelButtons.length; i++) {
            if (levelButtons[i].isSelected()) {
                selectedLevel = i;
                break;
            }
        }
        
        // Configure time attack mode
        boolean timeAttackMode = timerCheckbox.isSelected();
        
        // Set game level
        gameFrame.getController().setLevel(selectedLevel);
        
        // Enable or disable timer
        gameFrame.setTimeAttackMode(timeAttackMode);
        
        // Show game frame
        gameFrame.setVisible(true);
        this.dispose();
    }
    
    private void goBack() {
        parentFrame.setVisible(true);
        this.dispose();
    }
}
