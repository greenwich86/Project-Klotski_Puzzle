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
    private JRadioButton normalModeButton;
    private JRadioButton timeAttackButton;
    private JRadioButton timer3MinButton;
    private JRadioButton timer5MinButton;
    private JRadioButton timer7MinButton;
    
    // Add the missing field
    private int selectedTimeLimit;
    
    // UI components
    private JPanel buttonPanel; // Panel for timer selection buttons

    public GameSettingsFrame(int width, int height, GameFrame gameFrame, SelectionMenuFrame parentFrame) {
        this.gameFrame = gameFrame;
        this.parentFrame = parentFrame;

        this.setTitle("Game Settings");
        this.setLayout(new BorderLayout());
        
        // Increase window size to fit all elements
        this.setSize(Math.max(width, 600), Math.max(height, 700));
        this.setPreferredSize(new Dimension(600, 700));

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

        // Create level buttons using level names from MapModel
        levelButtons = new JRadioButton[MapModel.LEVELS.length];
        ButtonGroup levelGroup = new ButtonGroup();

        for (int i = 0; i < MapModel.LEVELS.length; i++) {
            levelButtons[i] = new JRadioButton(MapModel.LEVEL_NAMES[i]);
            levelButtons[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            
            // Set font based on difficulty
            if (i == MapModel.LEVELS.length - 1) { // Master level
                levelButtons[i].setFont(new Font("serif", Font.BOLD, 16));
                levelButtons[i].setForeground(Color.RED);
            } else if (i == 0) { // Easy level
                levelButtons[i].setFont(new Font("serif", Font.PLAIN, 14));
                levelButtons[i].setForeground(Color.BLUE);
            } else { // Other levels
                levelButtons[i].setFont(new Font("serif", Font.PLAIN, 14));
            }
            
            if (i == 0) {
                levelButtons[i].setSelected(true);
            }
            
            // Add tooltips for each level
            switch (i) {
                case 0: // Easy
                    levelButtons[i].setToolTipText("Classic level - No props available");
                    break;
                case 1: // Hard
                    levelButtons[i].setToolTipText("Harder puzzles - Props allowed");
                    break;
                case 2: // Expert
                    levelButtons[i].setToolTipText("Expert difficulty - Props allowed");
                    break;
                case 3: // Master
                    levelButtons[i].setToolTipText("Master difficulty - 5 minute time limit, No props, Military camps restrict soldier movement");
                    break;
            }
            
            final int level = i;
            levelButtons[i].addActionListener(e -> {
                // If Master level selected, enforce time attack mode
                if (level == MapModel.LEVELS.length - 1) { // Master level
                    timeAttackButton.setSelected(true);
                    timeAttackButton.setEnabled(false);
                    normalModeButton.setEnabled(false);
                    selectedTimeLimit = MapModel.DEFAULT_MASTER_TIME_LIMIT;
                    
                    // Update time buttons
                    for (Component comp : buttonPanel.getComponents()) {
                        if (comp instanceof JButton) {
                            JButton btn = (JButton)comp;
                            String btnText = btn.getText();
                            
                            if (btnText.equals("5 MINUTES")) {
                                btn.setBackground(new Color(100, 150, 255));
                                btn.setForeground(Color.WHITE);
                            } else {
                                btn.setBackground(new Color(220, 220, 220));
                                btn.setForeground(Color.GRAY);
                                btn.setEnabled(false);
                            }
                        }
                    }
                    
                    // Add warning label
                    JOptionPane.showMessageDialog(
                        this,
                        "Master difficulty enforces a 5-minute time limit and contains military camps\n" +
                        "that soldiers cannot step on. No props are available in this mode.",
                        "Master Difficulty",
                        JOptionPane.WARNING_MESSAGE
                    );
                } else {
                    // Re-enable mode selection for non-Master levels
                    timeAttackButton.setEnabled(true);
                    normalModeButton.setEnabled(true);
                    
                    // Re-enable all timer buttons
                    for (Component comp : buttonPanel.getComponents()) {
                        if (comp instanceof JButton) {
                            comp.setEnabled(true);
                        }
                    }
                }
            });
            
            levelGroup.add(levelButtons[i]);
            difficultyPanel.add(levelButtons[i]);
            difficultyPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // Game Mode panel - Normal vs Time Attack
        JPanel gameModePanel = new JPanel();
        gameModePanel.setLayout(new BoxLayout(gameModePanel, BoxLayout.Y_AXIS));
        gameModePanel.setBorder(BorderFactory.createTitledBorder("Game Mode"));
        gameModePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Mode selection buttons with large, clear text
        this.normalModeButton = new JRadioButton("Normal Mode");
        normalModeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        normalModeButton.setSelected(true);
        normalModeButton.setFont(new Font("Arial", Font.BOLD, 16));
        
        this.timeAttackButton = new JRadioButton("⏱️ TIME ATTACK MODE");
        timeAttackButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeAttackButton.setFont(new Font("Arial", Font.BOLD, 16));
        timeAttackButton.setForeground(Color.RED);
        
        // Group radio buttons
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(normalModeButton);
        modeGroup.add(timeAttackButton);
        
        // Add to panel with more spacing
        gameModePanel.add(normalModeButton);
        gameModePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        gameModePanel.add(timeAttackButton);
        gameModePanel.add(Box.createRigidArea(new Dimension(0, 15))); // Extra space before timer options
        
        // EXTREMELY VISIBLE timer panel with direct buttons - but more compact
        JPanel timerOptionsPanel = new JPanel();
        timerOptionsPanel.setLayout(new BorderLayout());
        timerOptionsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.RED, 3), // Slightly thinner border
            "⏱️ SELECT TIME LIMIT ⏱️"));
        timerOptionsPanel.setBackground(new Color(255, 220, 220)); // Brighter background
        timerOptionsPanel.setOpaque(true);
        timerOptionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timerOptionsPanel.setPreferredSize(new Dimension(250, 160)); // Smaller size
        timerOptionsPanel.setMaximumSize(new Dimension(250, 160)); // Limit maximum size
        
        // Extra-obvious heading label
        JLabel timeLabel = new JLabel("⏱️ CHOOSE GAME DURATION: ⏱️");
        timeLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Larger font
        timeLabel.setForeground(Color.RED);
        timeLabel.setHorizontalAlignment(JLabel.CENTER);
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timeLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        // Timer button panel with extra spacing
        this.buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(3, 1, 0, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        buttonPanel.setBackground(new Color(255, 240, 240));
        buttonPanel.setOpaque(true);
        
        // Large, obvious buttons with bright colors
        JButton btn3Min = new JButton("3 MINUTES");
        JButton btn5Min = new JButton("5 MINUTES");
        JButton btn7Min = new JButton("7 MINUTES");
        
        // Format buttons with EXTREMELY distinct colors and larger size
        btn3Min.setBackground(new Color(50, 150, 255));
        btn5Min.setBackground(new Color(230, 230, 255));
        btn7Min.setBackground(new Color(230, 230, 255));
        btn3Min.setForeground(Color.WHITE);
        btn5Min.setForeground(Color.BLACK);
        btn7Min.setForeground(Color.BLACK);
        btn3Min.setFont(new Font("Arial", Font.BOLD, 22)); // Much bigger font
        btn5Min.setFont(new Font("Arial", Font.BOLD, 22));
        btn7Min.setFont(new Font("Arial", Font.BOLD, 22));
        
        // Make buttons appropriate size but not too large
        Dimension buttonSize = new Dimension(170, 35);
        btn3Min.setPreferredSize(buttonSize);
        btn5Min.setPreferredSize(buttonSize);
        btn7Min.setPreferredSize(buttonSize);
        btn3Min.setMinimumSize(buttonSize);
        btn5Min.setMinimumSize(buttonSize);
        btn7Min.setMinimumSize(buttonSize);
        
        // Make buttons look clickable
        btn3Min.setFocusPainted(false);
        btn5Min.setFocusPainted(false);
        btn7Min.setFocusPainted(false);
        btn3Min.setBorderPainted(true);
        btn5Min.setBorderPainted(true);
        btn7Min.setBorderPainted(true);
        
        // Default selected state (store in class-level variable)
        this.selectedTimeLimit = 3;
        btn3Min.setBackground(new Color(50, 150, 255));
        btn3Min.setForeground(Color.WHITE);
        
        // Ensure time buttons are always enabled and visible
        btn3Min.setEnabled(true);
        btn5Min.setEnabled(true);
        btn7Min.setEnabled(true);
        
        // Add button borders to make them more prominent
        btn3Min.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        btn5Min.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        btn7Min.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.BLACK, 2),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Add button action listeners
        btn3Min.addActionListener(e -> {
            selectedTimeLimit = 3;
            // Update button appearances
            btn3Min.setBackground(new Color(100, 150, 255));
            btn3Min.setForeground(Color.WHITE);
            btn5Min.setBackground(new Color(230, 230, 255));
            btn5Min.setForeground(Color.BLACK);
            btn7Min.setBackground(new Color(230, 230, 255));
            btn7Min.setForeground(Color.BLACK);
            System.out.println("Time limit set to 3 minutes");
        });
        
        btn5Min.addActionListener(e -> {
            selectedTimeLimit = 5;
            // Update button appearances
            btn3Min.setBackground(new Color(230, 230, 255));
            btn3Min.setForeground(Color.BLACK);
            btn5Min.setBackground(new Color(100, 150, 255));
            btn5Min.setForeground(Color.WHITE);
            btn7Min.setBackground(new Color(230, 230, 255));
            btn7Min.setForeground(Color.BLACK);
            System.out.println("Time limit set to 5 minutes");
        });
        
        btn7Min.addActionListener(e -> {
            selectedTimeLimit = 7;
            // Update button appearances
            btn3Min.setBackground(new Color(230, 230, 255));
            btn3Min.setForeground(Color.BLACK);
            btn5Min.setBackground(new Color(230, 230, 255));
            btn5Min.setForeground(Color.BLACK);
            btn7Min.setBackground(new Color(100, 150, 255));
            btn7Min.setForeground(Color.WHITE);
            System.out.println("Time limit set to 7 minutes");
        });
        
        // Add buttons to panel
        buttonPanel.add(btn3Min);
        buttonPanel.add(btn5Min);
        buttonPanel.add(btn7Min);
        
        // Assemble timer panel
        timerOptionsPanel.add(timeLabel, BorderLayout.NORTH);
        timerOptionsPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // Add timer options to game mode panel
        gameModePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        gameModePanel.add(timerOptionsPanel);
        
        // Enable/disable timer options based on mode selection - fixed for button UI
        timeAttackButton.addActionListener(e -> {
            System.out.println("Time Attack Mode selected");
            
            // Make timer panel more visible with bright red border and background
            timerOptionsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.RED, 3), "Time Limit (Select One)"));
            timerOptionsPanel.setBackground(new Color(255, 230, 230));
            timerOptionsPanel.setOpaque(true);
            
            // Enable all buttons in the timer panel and highlight them
            for (Component comp : buttonPanel.getComponents()) {
                if (comp instanceof JButton) {
                    comp.setEnabled(true);
                }
            }
            
            // Ensure timer options panel is visible and all UI elements are properly refreshed
            timerOptionsPanel.setVisible(true);
            timerOptionsPanel.invalidate();
            timerOptionsPanel.validate();
            timerOptionsPanel.repaint();
            
            // Force full UI refresh
            gameModePanel.invalidate();
            gameModePanel.validate();
            gameModePanel.repaint();
            settingsPanel.invalidate();
            settingsPanel.validate();
            settingsPanel.repaint();
            this.invalidate();
            this.validate();
            this.repaint();
            
            // Debug info
            System.out.println("Time Attack Mode: Timer panel enabled, selectedTimeLimit=" + selectedTimeLimit);
        });
        
        normalModeButton.addActionListener(e -> {
            System.out.println("Normal Mode selected");
            
            // KEEP the timer panel visible but visually indicate it's not active
            timerOptionsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2), "Time Limit (Disabled in Normal Mode)"));
            timerOptionsPanel.setBackground(new Color(240, 240, 240)); // Light gray
            
            // Don't disable the buttons - just gray them out
            for (Component comp : buttonPanel.getComponents()) {
                if (comp instanceof JButton) {
                    JButton btn = (JButton)comp;
                    btn.setBackground(new Color(220, 220, 220));
                    btn.setForeground(Color.GRAY);
                }
            }
            
            // Keep panel visible - this is critical
            timerOptionsPanel.setVisible(true);
            
            // Ensure UI updates
            timerOptionsPanel.repaint();
            gameModePanel.revalidate();
            gameModePanel.repaint();
            settingsPanel.invalidate();
            settingsPanel.validate();
            settingsPanel.repaint();
            this.invalidate();
            this.validate();
            this.repaint();
            
            System.out.println("Normal Mode: Timer panel visually disabled but still visible");
        });
        
        // Info label
        JLabel timerInfoLabel = new JLabel("<html>In Time Attack Mode, you must solve<br>the puzzle before time runs out!</html>");
        timerInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        timerInfoLabel.setFont(new Font("serif", Font.ITALIC, 12));
        gameModePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        gameModePanel.add(timerInfoLabel);

        // Control button panel
        JPanel controlButtonPanel = new JPanel();
        controlButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(e -> startGame());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> goBack());

        controlButtonPanel.add(backButton);
        controlButtonPanel.add(startButton);

        // Add panels to settings
        settingsPanel.add(difficultyPanel);
        settingsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        settingsPanel.add(gameModePanel);

        // Add panels to frame
        this.add(titlePanel, BorderLayout.NORTH);
        this.add(settingsPanel, BorderLayout.CENTER);
        this.add(controlButtonPanel, BorderLayout.SOUTH);

        // Make frame scrollable to ensure all content is accessible
        JScrollPane scrollPane = new JScrollPane(settingsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Replace main panel with scrollable panel
        this.remove(settingsPanel);
        this.add(scrollPane, BorderLayout.CENTER);

        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        // Pack to ensure proper sizing
        this.pack();

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
        boolean timeAttackMode = timeAttackButton.isSelected();
        
        // Get time limit if time attack mode is enabled
        int timeLimit = 3; // Default to 3 minutes
        if (timeAttackMode) {
            // Use the selectedTimeLimit field that's set by button clicks
            timeLimit = selectedTimeLimit;
            System.out.println("Setting time limit to " + timeLimit + " minutes from button selection");
            
            // Debug output for time attack settings
            System.out.println("Time Attack Mode enabled with " + timeLimit + " minute limit");
        } else {
            System.out.println("Normal Mode selected (no time limit)");
        }

        // Set game level
        gameFrame.getController().setLevel(selectedLevel);

        // Enable or disable timer with selected time limit
        gameFrame.setTimeAttackMode(timeAttackMode, timeLimit);

        // Set the parent frame reference so the "Return to Menu" button works
        gameFrame.setParentFrame(parentFrame);

        // Show game frame
        gameFrame.setVisible(true);
        this.dispose();
    }

    private void goBack() {
        parentFrame.setVisible(true);
        this.dispose();
    }
}
