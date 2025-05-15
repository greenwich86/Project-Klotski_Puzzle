package view.game;

import controller.GameController;
import model.MapModel;
import view.FrameUtil;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {

    private GameController controller;
    private JButton restartBtn;
    private JButton loadBtn;
    private boolean guestMode = false;

    private JLabel stepLabel;
    private GamePanel gamePanel;

    public GameFrame(int width, int height, MapModel mapModel) {
        this.setTitle("2025 CS109 Project Demo");
        this.setLayout(new BorderLayout());
        this.setSize(width, height);
        this.setMinimumSize(new Dimension(800, 600));

        // 颜色
        this.getContentPane().setBackground(new Color(240, 240, 240));

        try {
            gamePanel = new GamePanel(mapModel);
            JScrollPane scrollPane = new JScrollPane(gamePanel);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            this.add(scrollPane, BorderLayout.CENTER);

            this.controller = new GameController(gamePanel, mapModel);
            this.controller.restartGame();
            gamePanel.requestFocusInWindow();
        } catch (Exception e) {
            System.err.println("Error creating GamePanel:");
            e.printStackTrace();
            throw e;
        }

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        controlPanel.setBackground(new Color(240, 240, 240));

        // 计数器
        this.stepLabel = new JLabel("Start");
        stepLabel.setFont(new Font("serif", Font.ITALIC, 22));
        stepLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlPanel.add(stepLabel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        gamePanel.setStepLabel(stepLabel);

        // 关卡
        JPanel levelPanel = new JPanel();
        levelPanel.setLayout(new BoxLayout(levelPanel, BoxLayout.Y_AXIS));
        levelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        levelPanel.setBackground(new Color(240, 240, 240));
        for (int i = 0; i < 3; i++) {
            final int level = i;
            JButton levelBtn = new JButton("Level " + (i + 1));
            levelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            levelBtn.setFont(new Font("Arial", Font.BOLD, 14));
            levelBtn.setBackground(new Color(52, 152, 219));
            levelBtn.setForeground(Color.WHITE);
            levelBtn.setFocusPainted(false);
            levelBtn.setBorder(BorderFactory.createRaisedBevelBorder());
            levelBtn.addActionListener(e -> {
                controller.setLevel(level);
                gamePanel.requestFocusInWindow();
            });
            levelPanel.add(levelBtn);
            levelPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        controlPanel.add(levelPanel);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 操作
        this.restartBtn = new JButton("Restart");
        restartBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        restartBtn.setFont(new Font("Arial", Font.BOLD, 14));
        restartBtn.setBackground(new Color(52, 152, 219));
        restartBtn.setForeground(Color.WHITE);
        restartBtn.setFocusPainted(false);
        restartBtn.setBorder(BorderFactory.createRaisedBevelBorder());
        restartBtn.addActionListener(e -> {
            controller.restartGame();
            gamePanel.requestFocusInWindow();
        });

        JButton undoBtn = new JButton("Undo");
        undoBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        undoBtn.setFont(new Font("Arial", Font.BOLD, 14));
        undoBtn.setBackground(new Color(52, 152, 219));
        undoBtn.setForeground(Color.WHITE);
        undoBtn.setFocusPainted(false);
        undoBtn.setBorder(BorderFactory.createRaisedBevelBorder());
        undoBtn.addActionListener(e -> {
            controller.undoMove();
            gamePanel.requestFocusInWindow();
        });

        this.loadBtn = new JButton("Load");
        loadBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadBtn.setFont(new Font("Arial", Font.BOLD, 14));
        loadBtn.setBackground(new Color(52, 152, 219));
        loadBtn.setForeground(Color.WHITE);
        loadBtn.setFocusPainted(false);
        loadBtn.setBorder(BorderFactory.createRaisedBevelBorder());
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
        saveBtn.setFont(new Font("Arial", Font.BOLD, 14));
        saveBtn.setBackground(new Color(52, 152, 219));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setBorder(BorderFactory.createRaisedBevelBorder());
        saveBtn.addActionListener(e -> {
            if (guestMode) {
                JOptionPane.showMessageDialog(this, "Guest users cannot save games");
            } else {
                controller.saveGame();
                gamePanel.requestFocusInWindow();
            }
        });

        controlPanel.add(restartBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(undoBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        controlPanel.add(loadBtn);
        controlPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        controlPanel.add(saveBtn);

        this.add(controlPanel, BorderLayout.EAST);
        this.setLocationRelativeTo(null);

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent e) {
                gamePanel.requestFocusInWindow();
            }
        });

        this.requestFocusInWindow();
        gamePanel.requestFocusInWindow();
    }

    public void setGuestMode(boolean guestMode) {
        this.guestMode = guestMode;
        if (guestMode) {
            this.setTitle("2025 CS109 Project Demo (Guest Mode)");
            this.loadBtn.setEnabled(false);
            // 游客模式禁用保存
            for (Component c : this.getContentPane().getComponents()) {
                if (c instanceof JButton && ((JButton) c).getText().equals("Save")) {
                    ((JButton) c).setEnabled(false);
                }
            }
        } else {
            this.setTitle("2025 CS109 Project Demo");
            this.loadBtn.setEnabled(true);
            // 非游客保存
            for (Component c : this.getContentPane().getComponents()) {
                if (c instanceof JButton && ((JButton) c).getText().equals("Save")) {
                    ((JButton) c).setEnabled(true);
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
}