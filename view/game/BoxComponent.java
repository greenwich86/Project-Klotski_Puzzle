package view.game;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BoxComponent extends JComponent {
    private static final Map<Color, String> IMAGE_MAP = new HashMap<>();
    static {
        IMAGE_MAP.put(Color.RED, "caocao.png");    // 曹操
        IMAGE_MAP.put(Color.ORANGE, "guanyu.png"); // 关羽
        IMAGE_MAP.put(Color.BLUE, "general.png");  // 将军
        IMAGE_MAP.put(Color.GREEN, "soldier.png"); // 士兵
    }
    
    private Color color;
    private int row;
    private int col;
    private boolean isSelected;
    private Image image;
    private Point targetPosition;
    private Point currentPosition;
    private boolean isAnimating;
    private long animationStartTime;

    public BoxComponent(Color color, int row, int col) {
        this.color = color;
        this.row = row;
        this.col = col;
        isSelected = false;
        isAnimating = false;
        targetPosition = new Point();
        currentPosition = new Point();
        
        // Load corresponding image
        String imagePath = "resources/" + IMAGE_MAP.get(color);
        ImageIcon icon = new ImageIcon(imagePath);
        this.image = icon.getImage();
    }

    public void animateTo(Point target, int durationMs) {
        System.out.println("Starting animation from: " + getLocation() + " to: " + target);
        
        this.targetPosition = target;
        this.currentPosition = getLocation();
        this.isAnimating = true;
        this.animationStartTime = System.currentTimeMillis();
        
        // Start animation timer
        Timer animationTimer = new Timer(16, e -> {
            long elapsed = System.currentTimeMillis() - animationStartTime;
            if (elapsed >= durationMs) {
                isAnimating = false;
                setLocation(targetPosition);
                ((Timer)e.getSource()).stop();
                showMoveCompletionEffect();
                return;
            }
            
            // Linear interpolation
            float progress = (float)elapsed / durationMs;
            int x = (int)(currentPosition.x + (targetPosition.x - currentPosition.x) * progress);
            int y = (int)(currentPosition.y + (targetPosition.y - currentPosition.y) * progress);
            
            setLocation(x, y);
            repaint();
        });
        animationTimer.start();
    }

    private void showMoveCompletionEffect() {
        // Create a pulsing effect when move completes
        Timer effectTimer = new Timer(50, e -> {
            long elapsed = System.currentTimeMillis() - animationStartTime;
            if (elapsed > 500) {
                ((Timer)e.getSource()).stop();
                return;
            }
            
            // Pulsing glow effect
            float intensity = 0.5f + 0.5f * (float)Math.sin(elapsed / 100.0 * Math.PI);
            repaint();
        });
        effectTimer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        
        // Draw wooden texture background with gradient
        GradientPaint woodGradient = new GradientPaint(
            0, 0, new Color(210, 180, 140),
            getWidth(), getHeight(), new Color(180, 140, 90));
        g2d.setPaint(woodGradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Add wood grain texture
        g2d.setColor(new Color(160, 110, 60, 30));
        for (int i = 0; i < getWidth(); i += 3) {
            g2d.drawLine(i, 0, i, getHeight());
        }
        
        // Draw the character image with natural drop shadow
        if (image != null) {
            // Calculate centered position maintaining aspect ratio
            double scale = Math.min(
                (double)getWidth() / image.getWidth(null),
                (double)getHeight() / image.getHeight(null));
            int imgWidth = (int)(image.getWidth(null) * scale);
            int imgHeight = (int)(image.getHeight(null) * scale);
            int imgX = (getWidth() - imgWidth) / 2;
            int imgY = (getHeight() - imgHeight) / 2;
            
            // Draw soft shadow with blur effect
            g2d.setColor(new Color(0, 0, 0, 60));
            for (int i = 0; i < 5; i++) {
                int offset = i + 1;
                g2d.fillRoundRect(
                    imgX + offset, 
                    imgY + offset, 
                    imgWidth, 
                    imgHeight, 
                    10, 10);
            }
            
            // Draw image with proper scaling
            g.drawImage(image, imgX, imgY, imgWidth, imgHeight, this);
        }
        
        // Draw character name at bottom with subtle background
        String name = "";
        if (color.equals(Color.RED)) {
            name = "\u66F9\u64CD"; // 曹操 (Cao Cao)
        } else if (color.equals(Color.ORANGE)) {
            name = "\u5173\u7FBD"; // 关羽 (Guan Yu)
        } else if (color.equals(Color.BLUE)) {
            name = "\u5C06\u519B"; // 将军 (General)
        } else if (color.equals(Color.GREEN)) {
            name = "\u58EB\u5175"; // 士兵 (Soldier)
        }
        
        if (!name.isEmpty()) {
            // Set up text rendering
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                               RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Calculate font size
            int fontSize = Math.min(getWidth(), getHeight()) / 5;
            Font font = new Font("Microsoft YaHei", Font.BOLD, fontSize);
            g2d.setFont(font);
            
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(name);
            int textHeight = fm.getHeight();
            int padding = 4;
            
            // Draw text background
            int bgX = (getWidth() - textWidth) / 2 - padding;
            int bgY = getHeight() - textHeight - padding;
            int bgWidth = textWidth + padding * 2;
            int bgHeight = textHeight + padding;
            
            g2d.setColor(new Color(0, 0, 0, 120));
            g2d.fillRoundRect(bgX, bgY, bgWidth, bgHeight, 5, 5);
            
            // Draw text
            int textX = (getWidth() - textWidth) / 2;
            int textY = getHeight() - padding - fm.getDescent();
            
            g2d.setColor(Color.WHITE);
            g2d.drawString(name, textX, textY);
        }
        
        // Draw move completion glow if recently animated
        long sinceAnimation = System.currentTimeMillis() - animationStartTime;
        if (sinceAnimation < 500) {
            float intensity = 0.5f + 0.5f * (float)Math.sin(sinceAnimation / 100.0 * Math.PI);
            GradientPaint glow = new GradientPaint(
                0, 0, new Color(255, 255, 255, (int)(80 * intensity)),
                getWidth(), getHeight(), new Color(255, 255, 255, (int)(120 * intensity)));
            g2d.setPaint(glow);
            g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        }

        // Draw enhanced selection highlight
        if (isSelected) {
            // Inner glow
            GradientPaint glow = new GradientPaint(
                0, 0, new Color(255, 255, 150, 80),
                getWidth(), getHeight(), new Color(255, 255, 0, 120));
            g2d.setPaint(glow);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Pulsing border
            g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(new Color(255, 215, 0));
            g2d.drawRoundRect(2, 2, getWidth()-5, getHeight()-5, 10, 10);
            
            // Outer glow
            g2d.setStroke(new BasicStroke(1));
            g2d.setColor(new Color(255, 255, 0, 50));
            g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
        }
        
        // Draw enhanced border with depth
        g2d.setStroke(new BasicStroke(2));
        
        // Dark outer edge
        g2d.setColor(new Color(101, 67, 33));
        g2d.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 5, 5);
        
        // Light inner edge
        g2d.setColor(new Color(244, 164, 96));
        g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 5, 5);
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
        this.repaint();
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public Color getColor() {
        return color;
    }

    private String colorToString(Color color) {
        if (color.equals(Color.RED)) return "CaoCao (2x2)";
        if (color.equals(Color.ORANGE)) return "GuanYu (2x1)"; 
        if (color.equals(Color.BLUE)) return "General (1x2)";
        if (color.equals(Color.GREEN)) return "Soldier (1x1)";
        return "Unknown";
    }
}
