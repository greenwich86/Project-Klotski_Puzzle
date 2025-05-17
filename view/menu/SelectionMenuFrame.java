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
                "<html><h2 style='color:#990000;text-align:center'>Klotski Puzzle Rules</h2>" +
                        "<hr style='border:1px solid #cccccc'>" +
                        
                        "<h3 style='color:#333399'>Game Objective</h3>" +
                        "<p>Move the red block (Cao Cao) to the exit at the bottom center of the board. " +
                        "Clear the path by sliding other blocks out of the way.</p>" +
                        
                        "<h3 style='color:#333399'>Basic Controls</h3>" +
                        "<ul>" +
                        "<li><b>Select Block:</b> Click on any block with the mouse</li>" +
                        "<li><b>Move Block:</b> Use arrow keys to move the selected block</li>" +
                        "<li><b>Undo:</b> Click the Undo button to reverse your last move</li>" +
                        "<li><b>Restart:</b> Click the Restart button to reset the puzzle</li>" +
                        "</ul>" +
                        
                        "<h3 style='color:#333399'>Piece Types</h3>" +
                        "<ul>" +
                        "<li><b style='color:red'>Cao Cao (曹操):</b> Red 2×2 block - must be moved to the exit</li>" +
                        "<li><b style='color:#FF8C00'>Guan Yu (关羽):</b> Orange 2×1 horizontal block</li>" +
                        "<li><b style='color:blue'>General (将军):</b> Blue 1×2 vertical block</li>" +
                        "<li><b style='color:green'>Soldier (士兵):</b> Green 1×1 block</li>" +
                        "<li><b style='color:#FF00FF'>Zhou Yu (周瑜):</b> Magenta 1×3 horizontal block</li>" +
                        "<li><b style='color:#696969'>Obstacle (障碍):</b> Gray immovable block</li>" +
                        "<li><b>Military Camp:</b> Special area that soldiers cannot step on (Master difficulty only)</li>" +
                        "</ul>" +
                        
                        "<h3 style='color:#333399'>Difficulty Levels</h3>" +
                        "<ul>" +
                        "<li><b>Easy:</b> Standard 4×5 board with classic layout. No props available.</li>" +
                        "<li><b>Hard:</b> 5×6 board with Cao Cao at top middle and obstacles. Props allowed.</li>" +
                        "<li><b>Expert:</b> 6×7 board with more complex layout and obstacles. Props allowed.</li>" +
                        "<li><b>Master:</b> 6×7 board with military camps that soldiers cannot step on. No props, enforced 5-minute time limit.</li>" +
                        "</ul>" +
                        
                        "<h3 style='color:#333399'>Game Modes</h3>" +
                        "<ul>" +
                        "<li><b>Normal Mode:</b> Solve the puzzle with no time constraints.</li>" +
                        "<li><b>Time Attack Mode:</b> Solve the puzzle before time runs out (3, 5, or 7 minutes).</li>" +
                        "</ul>" +
                        
                        "<h3 style='color:#333399'>Props System</h3>" +
                        "<p>Props are special items available in Hard and Expert difficulty levels:</p>" +
                        "<ul>" +
                        "<li><b>Hint:</b> Highlights a suggested move to help you progress.</li>" +
                        "<li><b>Time Bonus:</b> Adds extra time in Time Attack Mode.</li>" +
                        "<li><b>Obstacle Remover:</b> Temporarily removes an obstacle block.</li>" +
                        "</ul>" +
                        
                        "<h3 style='color:#333399'>AI Solver</h3>" +
                        "<p>The AI Solver can automatically solve the puzzle for you, but it doesn't use props - it finds a pure solution based on moves only.</p>" +
                        
                        "<hr style='border:1px solid #cccccc'>" +
                        "<p style='text-align:center;font-style:italic'>Complete the puzzle in as few moves as possible to master the game!</p>" +
                        "</html>",
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
