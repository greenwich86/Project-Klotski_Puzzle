package view.menu;

import view.FrameUtil;
import view.game.GameFrame;
import view.login.LoginFrame;
import controller.GameController;
import model.MapModel;

import javax.swing.*;
import java.awt.*;

/**
 * This frame provides the main menu options after login:
 * - Start New Game: Opens game settings screen
 * - Load Game: Opens previously saved game
 * - Game Rules: Displays game rules
 */
public class SelectionMenuFrame extends JFrame {
    private GameFrame gameFrame;
    private final String currentUser;

    public SelectionMenuFrame(int width, int height, String username) {
        this.currentUser = username;
        this.setTitle("Klotski Puzzle - Main Menu");
        this.setLayout(new BorderLayout());
        this.setSize(width, height);

        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel("Klotski Puzzle");
        titleLabel.setFont(new Font("serif", Font.BOLD, 30));
        titlePanel.add(titleLabel);

        // Welcome label
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel welcomeLabel = new JLabel("Welcome, " + (username.isEmpty() ? "Guest" : username) + "!");
        welcomeLabel.setFont(new Font("serif", Font.ITALIC, 16));
        welcomePanel.add(welcomeLabel);

        // Combine title and welcome
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.add(titlePanel);
        headerPanel.add(welcomePanel);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Start Game button
        JButton startGameBtn = new JButton("Start New Game");
        startGameBtn.setFont(new Font("serif", Font.PLAIN, 16));
        startGameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        startGameBtn.setMaximumSize(new Dimension(200, 40));
        startGameBtn.addActionListener(e -> {
            openGameSettings();
        });

        // Load Game button
        JButton loadGameBtn = new JButton("Load Game");
        loadGameBtn.setFont(new Font("serif", Font.PLAIN, 16));
        loadGameBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadGameBtn.setMaximumSize(new Dimension(200, 40));
        loadGameBtn.addActionListener(e -> {
            loadGame();
        });

        // Game Rules button
        JButton rulesBtn = new JButton("Game Rules");
        rulesBtn.setFont(new Font("serif", Font.PLAIN, 16));
        rulesBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        rulesBtn.setMaximumSize(new Dimension(200, 40));
        rulesBtn.addActionListener(e -> {
            showGameRules();
        });

        // Logout button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("serif", Font.PLAIN, 16));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(200, 40));
        logoutBtn.addActionListener(e -> {
            logout();
        });

        // Add buttons to panel with spacing
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(startGameBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(loadGameBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(rulesBtn);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        buttonPanel.add(logoutBtn);
        buttonPanel.add(Box.createVerticalGlue());

        // Add panels to frame
        this.add(headerPanel, BorderLayout.NORTH);
        this.add(buttonPanel, BorderLayout.CENTER);

        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void openGameSettings() {
        // Create game frame if it doesn't exist
        if (this.gameFrame == null) {
            MapModel mapModel = new MapModel();
            this.gameFrame = new GameFrame(800, 600, mapModel);
            this.gameFrame.getController().setCurrentUser(currentUser);
            this.gameFrame.setGuestMode(currentUser.isEmpty());
        }

        // Create and show game settings
        GameSettingsFrame settingsFrame = new GameSettingsFrame(400, 350, this.gameFrame, this);
        settingsFrame.setVisible(true);
        this.setVisible(false);
    }

    private void loadGame() {
        // If guest mode, show error
        if (currentUser.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Guest users cannot load games", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create game frame if it doesn't exist
        if (this.gameFrame == null) {
            MapModel mapModel = new MapModel();
            this.gameFrame = new GameFrame(800, 600, mapModel);
            this.gameFrame.getController().setCurrentUser(currentUser);
            this.gameFrame.setGuestMode(false);
        }

        // Try to load the game
        if (this.gameFrame.getController().loadGame()) {
            this.gameFrame.setVisible(true);
            this.setVisible(false);
        }
    }

    private void showGameRules() {
        JOptionPane.showMessageDialog(this,
                "<html><h2>Klotski Puzzle Rules</h2><br>" +
                        "<p>The goal is to move the red block (Cao Cao) to the exit at the bottom of the board.<p>" +
                        "<p>- Select a block with mouse click<br>" +
                        "- Move selected block using arrow keys<br>" +
                        "- Blocks can only move if there is empty space<br>" +
                        "- Complete the puzzle in as few moves as possible</p></html>",
                "Game Rules",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void logout() {
        // Return to login screen
        LoginFrame loginFrame = new LoginFrame(280, 280);
        loginFrame.setVisible(true);

        // Pass game frame reference
        if (this.gameFrame != null) {
            loginFrame.setGameFrame(this.gameFrame);
        } else {
            MapModel mapModel = new MapModel();
            GameFrame gameFrame = new GameFrame(800, 600, mapModel);
            loginFrame.setGameFrame(gameFrame);
        }

        this.dispose();
    }

    public void setGameFrame(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
    }
}