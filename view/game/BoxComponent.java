package view.game;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class BoxComponent extends JComponent {
    private Color color;
    private int row;
    private int col;
    private boolean isSelected;
    private boolean movable;
    private BufferedImage image;

    public BoxComponent(Color color, int row, int col) {
        this(color, row, col, true);
    }

    public BoxComponent(Color color, int row, int col, boolean movable) {
        this.color = color;
        this.row = row;
        this.col = col;
        this.isSelected = false;
        this.movable = movable;
        loadImage();
    }

    private void loadImage() {
        String imageName = getImageNameByColor(color);
        if (imageName == null) return;

        try {
            URL resourceUrl = getClass().getClassLoader().getResource(imageName);
            if (resourceUrl != null) {
                image = ImageIO.read(resourceUrl);
                return;
            }

            resourceUrl = getClass().getResource("/" + imageName);
            if (resourceUrl != null) {
                image = ImageIO.read(resourceUrl);
            }
        } catch (IOException e) {
            System.err.printf("加载图片失败: %s - %s%n", imageName, e.getMessage());
        }
    }

    private String getImageNameByColor(Color color) {
        if (color == null) return null;

        if (color.equals(Color.RED)) return "Cao Cao.jpg";
        if (color.equals(Color.ORANGE)) return "Guan Yu.jpg";
        if (color.equals(Color.BLUE)) return "General.jpg";
        if (color.equals(Color.GREEN)) return "Soldier.jpg";
        if (color.equals(Color.MAGENTA)) return "Zhou Yu.jpg";
        if (color.equals(Color.DARK_GRAY)) return "Blocked.jpg";

        return null;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (image != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(image, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(getFallbackColor(color));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("图片缺失", getWidth()/3, getHeight()/2);
        }

        Border border = isSelected
                ? BorderFactory.createLineBorder(Color.RED, 3)
                : BorderFactory.createLineBorder(Color.DARK_GRAY, 1);
        this.setBorder(border);
    }

    private Color getFallbackColor(Color originalColor) {
        if (originalColor == null) return Color.LIGHT_GRAY;

        return new Color(
                Math.min(255, originalColor.getRed() + 30),
                Math.min(255, originalColor.getGreen() + 30),
                Math.min(255, originalColor.getBlue() + 30)
        );
    }

    // Getters and setters
    public void setSelected(boolean selected) {
        this.isSelected = selected;
        repaint();
    }

    public Color getColor() {
        return color;
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

    public boolean isMovable() {
        return movable;
    }

    public void setMovable(boolean movable) {
        this.movable = movable;
        repaint();
    }
}