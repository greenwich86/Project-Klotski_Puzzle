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
        dialog.setSize(300, 200);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(20, 20, 80, 25);
        dialog.add(userLabel);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(20, 60, 80, 25);
        dialog.add(passLabel);

        JLabel confirmLabel = new JLabel("Confirm:");
        confirmLabel.setBounds(20, 100, 80, 25);
        dialog.add(confirmLabel);

        JTextField regUser = new JTextField();
        regUser.setBounds(110, 20, 150, 25);
        dialog.add(regUser);

        JPasswordField regPass = new JPasswordField();
        regPass.setBounds(110, 60, 150, 25);
        dialog.add(regPass);

        JPasswordField confirmPass = new JPasswordField();
        confirmPass.setBounds(110, 100, 150, 25);
        dialog.add(confirmPass);

        dialog.setSize(300, 300); // Increased height for new buttons

        JButton confirmBtn = new JButton("Confirm");
        confirmBtn.setBounds(50, 140, 90, 30);
        dialog.add(confirmBtn);

        JButton registerBtn = new JButton("Register");
        registerBtn.setBounds(160, 140, 90, 30);
        registerBtn.setEnabled(false);
        dialog.add(registerBtn); // Add this line to ensure button is in dialog
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


    public LoginFrame(int width, int height) {
        this.userManager = new UserManager();
        this.setTitle("Login Frame");
        this.setLayout(null);
        this.setSize(width, height);
        JLabel userLabel = FrameUtil.createJLabel(this, new Point(50, 20), 70, 40, "username:");
        JLabel passLabel = FrameUtil.createJLabel(this, new Point(50, 80), 70, 40, "password:");
        username = FrameUtil.createJTextField(this, new Point(120, 20), 120, 40);
        password = new JPasswordField();
        password.setBounds(120, 80, 120, 40);
        this.add(password);

        submitBtn = FrameUtil.createButton(this, "Login", new Point(40, 140), 100, 40);
        resetBtn = FrameUtil.createButton(this, "Reset", new Point(160, 140), 100, 40);
        JButton guestBtn = FrameUtil.createButton(this, "Play as Guest", new Point(40, 190), 220, 40);
        JButton registerBtn = FrameUtil.createButton(this, "Register", new Point(40, 240), 220, 40);

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
                
                // Create and show the selection menu instead of the game directly
                view.menu.SelectionMenuFrame menuFrame = new view.menu.SelectionMenuFrame(600, 400, user);
                menuFrame.setGameFrame(this.gameFrame);
                
                // Set the parent frame reference in the GameFrame for the "Return to Menu" button
                this.gameFrame.setParentFrame(menuFrame);
                
                menuFrame.setVisible(true);
                this.setVisible(false);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password");
            }
        });

        guestBtn.addActionListener(e -> {
            // Set guest mode and create menu
            if (this.gameFrame != null) {
                this.gameFrame.setGuestMode(true);
                
                // Create and show the selection menu instead of the game directly
                view.menu.SelectionMenuFrame menuFrame = new view.menu.SelectionMenuFrame(600, 400, "");
                menuFrame.setGameFrame(this.gameFrame);
                
                // Set the parent frame reference in the GameFrame for the "Return to Menu" button
                this.gameFrame.setParentFrame(menuFrame);
                
                menuFrame.setVisible(true);
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
