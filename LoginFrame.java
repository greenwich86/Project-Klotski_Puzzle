package view.login;

import model.MapModel;
import model.UserManager;
import view.game.GameFrame;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.File;

public class LoginFrame extends JFrame {
    private JTextField username;
    private JPasswordField password;
    private JButton submitBtn;
    private JButton resetBtn;
    private GameFrame gameFrame;
    private UserManager userManager;
    private LoginPanel loginPanel;

    // color
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SECONDARY_COLOR = new Color(41, 128, 185);
    private static final Color LIGHT_COLOR = new Color(236, 240, 241);
    private static final Color DARK_COLOR = new Color(52, 73, 94);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);

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
        dialog.setSize(350, 320);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gp = new GradientPaint(
                        0, 0, LIGHT_COLOR,
                        0, getHeight(), LIGHT_COLOR.brighter());
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());

                g2d.setColor(PRIMARY_COLOR);
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 15, 15);
            }
        };
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        dialog.add(contentPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // register
        JLabel titleLabel = new JLabel("Create New Account");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(DARK_COLOR);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(titleLabel, gbc);
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setForeground(DARK_COLOR);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        contentPanel.add(userLabel, gbc);

        JTextField regUser = new JTextField();
        regUser.setFont(new Font("Arial", Font.PLAIN, 14));
        regUser.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        gbc.gridx = 1;
        contentPanel.add(regUser, gbc);

        // password
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passLabel.setForeground(DARK_COLOR);

        gbc.gridx = 0;
        gbc.gridy = 2;
        contentPanel.add(passLabel, gbc);

        JPasswordField regPass = new JPasswordField();
        regPass.setFont(new Font("Arial", Font.PLAIN, 14));
        regPass.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        gbc.gridx = 1;
        contentPanel.add(regPass, gbc);

        // comfirm
        JLabel confirmLabel = new JLabel("Confirm:");
        confirmLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        confirmLabel.setForeground(DARK_COLOR);

        gbc.gridx = 0;
        gbc.gridy = 3;
        contentPanel.add(confirmLabel, gbc);

        JPasswordField confirmPass = new JPasswordField();
        confirmPass.setFont(new Font("Arial", Font.PLAIN, 14));
        confirmPass.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        gbc.gridx = 1;
        contentPanel.add(confirmPass, gbc);

        // botton
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);

        JButton confirmBtn = new JButton("Validate");
        confirmBtn.setFont(new Font("Arial", Font.BOLD, 14));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setBackground(PRIMARY_COLOR);
        confirmBtn.setFocusPainted(false);
        confirmBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        confirmBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        confirmBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                confirmBtn.setBackground(SECONDARY_COLOR);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                confirmBtn.setBackground(PRIMARY_COLOR);
            }
        });

        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("Arial", Font.BOLD, 14));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBackground(Color.GRAY);
        registerBtn.setEnabled(false);
        registerBtn.setFocusPainted(false);
        registerBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        registerBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (registerBtn.isEnabled()) {
                    registerBtn.setBackground(SUCCESS_COLOR.darker());
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (registerBtn.isEnabled()) {
                    registerBtn.setBackground(SUCCESS_COLOR);
                }
            }
        });

        buttonPanel.add(confirmBtn);
        buttonPanel.add(registerBtn);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        contentPanel.add(buttonPanel, gbc);

        confirmBtn.addActionListener(e -> {
            String newUser = regUser.getText();
            String newPass = new String(regPass.getPassword());
            String confirmPassStr = new String(confirmPass.getPassword());

            System.out.println("Confirm button clicked");
            System.out.println("Username: " + newUser);
            System.out.println("Password length: " + newPass.length());

            if (newUser.isEmpty() || newPass.isEmpty()) {
                System.out.println("Validation failed - empty fields");
                JOptionPane.showMessageDialog(dialog, "Username and password cannot be empty",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!newPass.equals(confirmPassStr)) {
                System.out.println("Validation failed - password mismatch");
                JOptionPane.showMessageDialog(dialog, "Passwords do not match",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            System.out.println("Validation passed - enabling Register button");
            registerBtn.setEnabled(true);
            registerBtn.setBackground(SUCCESS_COLOR);
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
                JOptionPane.showMessageDialog(dialog, "Username already exists",
                        "Registration Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    public LoginFrame(int width, int height) {
        this.userManager = new UserManager();
        this.setTitle("Game Login");
        this.setLayout(new GridBagLayout());
        this.setSize(width, height);
        this.setResizable(true);
        this.setUndecorated(true);

        // ---------嵌入背景
        loginPanel = new LoginPanel();
        loginPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        this.add(loginPanel, gbc);

        JButton closeBtn = new JButton("X");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 14));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(ERROR_COLOR);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets = new Insets(10, 0, 0, 10);
        loginPanel.add(closeBtn, gbc);

        closeBtn.addActionListener(e -> System.exit(0));

        JPanel formPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(255, 255, 255, 230));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2d.setColor(PRIMARY_COLOR);
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };

        formPanel.setOpaque(false);
        formPanel.setPreferredSize(new Dimension(350, 400));

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        loginPanel.add(formPanel, gbc);

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 25, 10, 25);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Game Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(DARK_COLOR);
        titleLabel.setHorizontalAlignment(JLabel.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(30, 25, 20, 25);
        formPanel.add(titleLabel, gbc);

        // username：
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        userLabel.setForeground(DARK_COLOR);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 25, 5, 25);
        formPanel.add(userLabel, gbc);

        username = new JTextField();
        username.setFont(new Font("Arial", Font.PLAIN, 16));
        username.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        username.setPreferredSize(new Dimension(200, 35));

        username.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                username.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(SUCCESS_COLOR, 2),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
            @Override
            public void focusLost(FocusEvent e) {
                username.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
        });

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 25, 15, 25);
        formPanel.add(username, gbc);
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        passLabel.setForeground(DARK_COLOR);

        gbc.gridy = 3;
        gbc.insets = new Insets(10, 25, 5, 25);
        formPanel.add(passLabel, gbc);

        password = new JPasswordField();
        password.setFont(new Font("Arial", Font.PLAIN, 16));
        password.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        password.setPreferredSize(new Dimension(200, 35));

        password.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                password.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(SUCCESS_COLOR, 2),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
            @Override
            public void focusLost(FocusEvent e) {
                password.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
                        BorderFactory.createEmptyBorder(8, 12, 8, 12)));
            }
        });

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 25, 25, 25);
        formPanel.add(password, gbc);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        buttonPanel.setOpaque(false);

        submitBtn = new JButton("Login");
        submitBtn.setFont(new Font("Arial", Font.BOLD, 14));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setBackground(PRIMARY_COLOR);
        submitBtn.setFocusPainted(false);
        submitBtn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        submitBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        submitBtn.setPreferredSize(new Dimension(120, 40));

        submitBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                submitBtn.setBackground(SECONDARY_COLOR);
                submitBtn.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR.darker(), 2));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                submitBtn.setBackground(PRIMARY_COLOR);
                submitBtn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            }
        });

        resetBtn = new JButton("Reset");
        resetBtn.setFont(new Font("Arial", Font.BOLD, 14));
        resetBtn.setForeground(DARK_COLOR);
        resetBtn.setBackground(LIGHT_COLOR);
        resetBtn.setFocusPainted(false);
        resetBtn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        resetBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        resetBtn.setPreferredSize(new Dimension(120, 40));

        resetBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                resetBtn.setBackground(LIGHT_COLOR.brighter());
                resetBtn.setBorder(BorderFactory.createLineBorder(DARK_COLOR, 2));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                resetBtn.setBackground(LIGHT_COLOR);
                resetBtn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            }
        });

        JButton guestBtn = new JButton("Guest");
        guestBtn.setFont(new Font("Arial", Font.BOLD, 14));
        guestBtn.setForeground(Color.WHITE);
        guestBtn.setBackground(SUCCESS_COLOR);
        guestBtn.setFocusPainted(false);
        guestBtn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        guestBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        guestBtn.setPreferredSize(new Dimension(120, 40));

        guestBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                guestBtn.setBackground(SUCCESS_COLOR.darker());
                guestBtn.setBorder(BorderFactory.createLineBorder(SUCCESS_COLOR.darker(), 2));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                guestBtn.setBackground(SUCCESS_COLOR);
                guestBtn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            }
        });

        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("Arial", Font.BOLD, 14));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBackground(DARK_COLOR);
        registerBtn.setFocusPainted(false);
        registerBtn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.setPreferredSize(new Dimension(120, 40));

        registerBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerBtn.setBackground(DARK_COLOR.darker());
                registerBtn.setBorder(BorderFactory.createLineBorder(DARK_COLOR.darker(), 2));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerBtn.setBackground(DARK_COLOR);
                registerBtn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
            }
        });

        buttonPanel.add(submitBtn);
        buttonPanel.add(resetBtn);
        buttonPanel.add(guestBtn);
        buttonPanel.add(registerBtn);

        gbc.gridy = 5;
        gbc.insets = new Insets(10, 25, 30, 25);
        formPanel.add(buttonPanel, gbc);

        // 在 LoginFrame 中
        submitBtn.addActionListener(e -> {
            String user = username.getText();
            if (validateLogin(user, new String(password.getPassword()))) {
                if (this.gameFrame == null) {
                    MapModel mapModel = new MapModel();
                    this.gameFrame = new GameFrame(800, 600, mapModel);
                }
                this.gameFrame.getController().setCurrentUser(user);
                this.gameFrame.setGuestMode(false);
                this.gameFrame.setVisible(true);
                this.setVisible(false);
            } else {
                shakeComponent(username);
                shakeComponent(password);
                JOptionPane.showMessageDialog(this, "Invalid username or password",
                        "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        guestBtn.addActionListener(e -> {
            if (this.gameFrame != null) {
                this.gameFrame.setGuestMode(true);
                this.gameFrame.setVisible(true);
                this.setVisible(false);
            }
        });

        registerBtn.addActionListener(e -> {
            showRegistrationDialog();
        });

        resetBtn.addActionListener(e -> {
            username.setText("");
            password.setText("");
        });

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {

                dragOffsetX = evt.getX();
                dragOffsetY = evt.getY();
            }
        });

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                // 计算新位置并移动窗口
                setLocation(evt.getXOnScreen() - dragOffsetX,
                        evt.getYOnScreen() - dragOffsetY);
            }
        });

        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private int dragOffsetX, dragOffsetY;


    private void shakeComponent(JComponent component) {
        Point location = component.getLocation();
        Timer timer = new Timer(50, e -> {
            int x = location.x + (int) (Math.random() * 10 - 5);
            component.setLocation(x, location.y);

            ((Timer) e.getSource()).setInitialDelay(500);
            ((Timer) e.getSource()).setRepeats(false);
        });
        timer.start();
    }

    public void setGameFrame(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame(800, 600);
            frame.setVisible(true);
        });
    }
}