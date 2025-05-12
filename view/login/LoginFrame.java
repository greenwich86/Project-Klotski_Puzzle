package view.login;

import model.MapModel;
import model.UserManager;
import view.FrameUtil;
import view.game.GameFrame;

import javax.swing.*;
import java.awt.*;
import java.io.File;


public class LoginFrame extends JFrame {
    private JTextField username;
    private JPasswordField password;
    private JButton submitBtn;
    private JButton resetBtn;
    private GameFrame gameFrame;
    private UserManager userManager;

    private boolean validateLogin(String username, String password) {
        boolean isValid = userManager.validateUser(username, password);
        System.out.println("Login validation for " + username + ": " + isValid);
        if (!isValid) {
            System.out.println("Stored users: " + userManager.getUsers());
        }
        return isValid;
    }

    private void showRegistrationDialog() {
        JDialog dialog = new JDialog(this, "Register New User", true);
        dialog.setLayout(null);
        dialog.setSize(350, 250);
        dialog.getContentPane().setBackground(new Color(245, 235, 200));

        // Create styled labels
        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(30, 30, 80, 25);
        userLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        userLabel.setForeground(new Color(101, 67, 33));
        dialog.add(userLabel);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(30, 70, 80, 25);
        passLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        passLabel.setForeground(new Color(101, 67, 33));
        dialog.add(passLabel);

        JLabel confirmLabel = new JLabel("Confirm:");
        confirmLabel.setBounds(30, 110, 80, 25);
        confirmLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        confirmLabel.setForeground(new Color(101, 67, 33));
        dialog.add(confirmLabel);

        // Create styled text fields
        JTextField regUser = new JTextField();
        regUser.setBounds(120, 30, 180, 25);
        styleTextField(regUser);
        dialog.add(regUser);

        JPasswordField regPass = new JPasswordField();
        regPass.setBounds(120, 70, 180, 25);
        styleTextField(regPass);
        dialog.add(regPass);

        JPasswordField confirmPass = new JPasswordField();
        confirmPass.setBounds(120, 110, 180, 25);
        styleTextField(confirmPass);
        dialog.add(confirmPass);

        // Create themed buttons
        JButton confirmBtn = createThemedButton("Confirm", new Point(50, 150), 100, 30);
        dialog.add(confirmBtn);

        JButton registerBtn = createThemedButton("Register", new Point(170, 150), 100, 30);
        registerBtn.setEnabled(false);
        dialog.add(registerBtn);
        confirmBtn.addActionListener(e -> {
            String newUser = regUser.getText();
            String newPass = new String(regPass.getPassword());
            String confirmPassStr = new String(confirmPass.getPassword());
            
            System.out.println("Confirm button clicked");
            System.out.println("Username: " + newUser);
            System.out.println("Password length: " + newPass.length());
            
            if (newUser.isEmpty() || newPass.isEmpty()) {
                System.out.println("Validation failed - empty fields");
                JOptionPane.showMessageDialog(dialog, "Username and password cannot be empty");
                return;
            }
            
            if (!newPass.equals(confirmPassStr)) {
                System.out.println("Validation failed - password mismatch");
                JOptionPane.showMessageDialog(dialog, "Passwords do not match");
                return;
            }
            
            System.out.println("Validation passed - enabling Register button");
            registerBtn.setEnabled(true);
            JOptionPane.showMessageDialog(dialog, "Inputs validated. Click Register to complete.");
        });

        registerBtn.addActionListener(e -> {
            String username = regUser.getText();
            String password = new String(regPass.getPassword());
            System.out.println("Attempting to register user: " + username);
            System.out.println("Password length: " + password.length());
            
            if (userManager.registerUser(username, password)) {
                System.out.println("Registration succeeded for: " + username);
                System.out.println("Current users: " + userManager.getUsers());
                
                // Verify file was created
                File userFile = new File("users.dat");
                if (userFile.exists()) {
                    System.out.println("User file created successfully at: " + userFile.getAbsolutePath());
                    System.out.println("File size: " + userFile.length() + " bytes");
                    JOptionPane.showMessageDialog(dialog, "Registration successful!");
                    dialog.dispose();
                } else {
                    System.err.println("ERROR: User file was not created");
                    JOptionPane.showMessageDialog(dialog, 
                        "Registration failed - could not save user data", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } else {
                System.out.println("Registration failed for: " + username);
                JOptionPane.showMessageDialog(dialog, "Username already exists");
            }
        });

        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
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
        
        return button;
    }

    private void styleTextField(JTextField field) {
        // Base styling
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(139, 69, 19), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        field.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        field.setBackground(new Color(255, 253, 245));
        
        // Focus listener for highlighting
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(210, 105, 30), 2),
                    BorderFactory.createEmptyBorder(4, 4, 4, 4)
                ));
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(139, 69, 19), 1),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)
                ));
            }
        });
        
        // Add placeholder text if empty
        if (field instanceof JTextField && !(field instanceof JPasswordField)) {
            ((JTextField)field).setText("Enter username...");
            field.setForeground(new Color(150, 150, 150));
            field.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent evt) {
                    if (field.getText().equals("Enter username...")) {
                        field.setText("");
                        field.setForeground(Color.BLACK);
                    }
                }
                public void focusLost(java.awt.event.FocusEvent evt) {
                    if (field.getText().isEmpty()) {
                        field.setForeground(new Color(150, 150, 150));
                        field.setText("Enter username...");
                    }
                }
            });
        }
    }

    public LoginFrame(int width, int height) {
        this.userManager = new UserManager();
        this.setTitle("Klotski Puzzle - Login");
        this.setLayout(null);
        // Increased frame size to ensure all components fit
        this.setSize(500, 400);
        this.getContentPane().setBackground(new Color(245, 235, 200));
        
        // Center components with more spacing
        int centerX = 150;
        
        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(centerX, 50, 100, 30);
        userLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        userLabel.setForeground(new Color(101, 67, 33));
        this.add(userLabel);
        
        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(centerX, 120, 100, 30);
        passLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        passLabel.setForeground(new Color(101, 67, 33));
        this.add(passLabel);
        
        username = new JTextField();
        username.setBounds(centerX + 110, 50, 200, 30);
        styleTextField(username);
        this.add(username);
        
        password = new JPasswordField();
        password.setBounds(centerX + 110, 120, 200, 30);
        styleTextField(password);
        this.add(password);

        System.out.println("Creating buttons...");
        
        // Position buttons with better spacing and proportions
        int buttonWidth = 100;
        int buttonHeight = 35;
        int buttonSpacing = 20;
        
        submitBtn = createThemedButton("Login", new Point(centerX, 170), buttonWidth, buttonHeight);
        resetBtn = createThemedButton("Reset", new Point(centerX + buttonWidth + buttonSpacing, 170), buttonWidth, buttonHeight);
        
        JButton guestBtn = createThemedButton("Guest", new Point(centerX, 220), buttonWidth, buttonHeight);
        JButton registerBtn = createThemedButton("Register", new Point(centerX + buttonWidth + buttonSpacing, 220), buttonWidth, buttonHeight);
        
        // Debug logging for button visibility
        System.out.println("\nButton bounds:");
        System.out.println("Login: " + submitBtn.getBounds() + ", visible: " + submitBtn.isVisible());
        System.out.println("Reset: " + resetBtn.getBounds() + ", visible: " + resetBtn.isVisible());
        System.out.println("Guest: " + guestBtn.getBounds() + ", visible: " + guestBtn.isVisible());
        System.out.println("Register: " + registerBtn.getBounds() + ", visible: " + registerBtn.isVisible());
        
        // Explicitly add all buttons to frame and validate
        this.add(submitBtn);
        this.add(resetBtn);
        this.add(guestBtn);
        this.add(registerBtn);
        
        System.out.println("\nFrame components after adding buttons:");
        for (Component comp : this.getContentPane().getComponents()) {
            System.out.println(comp.getClass().getSimpleName() + ": " + comp.getBounds());
        }

        submitBtn.addActionListener(e -> {
            // Validate login credentials
            String user = username.getText();
            if (validateLogin(user, new String(password.getPassword()))) {
                if (this.gameFrame == null) {
                    MapModel mapModel = new MapModel();
                    this.gameFrame = new GameFrame(800, 600, mapModel);
                }
                // Set current user in controller and disable guest mode
                this.gameFrame.getController().setCurrentUser(user);
                this.gameFrame.setGuestMode(false);
                this.gameFrame.setVisible(true);
                this.setVisible(false);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password");
            }
        });

        guestBtn.addActionListener(e -> {
            // Set guest mode and proceed to game
            if (this.gameFrame != null) {
                this.gameFrame.setGuestMode(true);
                this.gameFrame.setVisible(true);
                this.setVisible(false);
            }
        });

        registerBtn.addActionListener(e -> {
            System.out.println("Register button clicked");
            showRegistrationDialog();
            System.out.println("Registration dialog shown");
        });

        resetBtn.addActionListener(e -> {
            username.setText("");
            password.setText("");
        });

        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void setGameFrame(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
    }

    public static void main(String[] args) {
        LoginFrame frame = new LoginFrame(400, 300);
        frame.setVisible(true);
    }
}
