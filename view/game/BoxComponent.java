package view.game;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.stream.Collectors;

public class BoxComponent extends JComponent {
    private Color color;
    private int row;
    private int col;
    private boolean isSelected;
    private boolean movable;
    private boolean isAnimating;
    private float scale = 1.0f;
    private Color shadowColor;

    public BoxComponent(Color color, int row, int col) {
        this(color, row, col, true);
    }

    public BoxComponent(Color color, int row, int col, boolean movable) {
        this.color = color;
        this.row = row;
        this.col = col;
        this.isSelected = false;
        this.movable = movable;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g.create();
        
        // Debug output for position verification
//        System.out.printf("Painting BoxComponent at (%d,%d) size %dx%d (row=%d,col=%d)\n",
//            getX(), getY(), getWidth(), getHeight(), row, col);
            
        // Apply scaling transformation
        if (scale != 1.0f) {
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            g2d.translate(centerX, centerY);
            g2d.scale(scale, scale);
            g2d.translate(-centerX, -centerY);
        }

        // Draw more pronounced shadow if set
        if (shadowColor != null) {
            g2d.setColor(shadowColor);
            g2d.fillRoundRect(5, 8, getWidth(), getHeight(), 10, 10); // Larger, softer shadow
        }

        // Draw main component
        g2d.setColor(color);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Set text color and font - use high contrast colors
        Color textColor = color.getRed() + color.getGreen() + color.getBlue() > 382 ? 
                         Color.BLACK : Color.WHITE;
        g2d.setColor(textColor);
        
        // Calculate dynamic font size based on component dimensions
        int fontSize = Math.min(getWidth(), getHeight()) / 3;
        fontSize = Math.max(8, Math.min(24, fontSize)); // Clamp between 8-24
        
        try {
            Font font = new Font("Microsoft YaHei", Font.BOLD, fontSize);
            g.setFont(font);
        } catch (Exception e) {
            Font font = new Font(Font.SANS_SERIF, Font.BOLD, fontSize);
            g.setFont(font);
        }
        
        // Determine Chinese name based on color (using Unicode escapes)
        String name = "";
        if (color.equals(Color.RED)) {
            name = "\u66F9\u64CD"; // 曹操 (Cao Cao)
        } else if (color.equals(Color.ORANGE)) {
            name = "\u5173\u7FBD"; // 关羽 (Guan Yu)
        } else if (color.equals(Color.BLUE)) {
            name = "\u5C06\u519B"; // 将军 (General)
        } else if (color.equals(Color.GREEN)) {
            name = "\u58EB\u5175"; // 士兵 (Soldier)
        } else if (color.equals(Color.MAGENTA)) {
            name = "\u5468\u745E"; // 周瑜 (Zhou Yu)
        } else if (color.equals(Color.DARK_GRAY)) {
            name = "\u969C\u788D"; // 障碍 (Obstacle)
        }
        
        // Draw text with positioning
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(name)) / 2;
        int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
        
        // Draw text background for visibility
        g.setColor(new Color(0,0,0,100));
        g.fillRect(x-2, y-fm.getAscent()-2, 
                  fm.stringWidth(name)+4, fm.getHeight()+4);
        
        // Draw the text
        g2d.setColor(textColor);
        g2d.drawString(name, x, y);
        
        // Draw debug outlines
        g2d.setColor(Color.RED);
        g2d.drawRect(0, 0, getWidth()-1, getHeight()-1);
        g2d.drawRect(x-2, y-fm.getAscent()-2, 
                  fm.stringWidth(name)+4, fm.getHeight()+4);
        
        g2d.dispose();
        
        // Set selection border
        Border border;
        if(isSelected){
            border = BorderFactory.createLineBorder(Color.red,3);
        }else {
            border = BorderFactory.createLineBorder(Color.DARK_GRAY, 1);
        }
        this.setBorder(border);
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        this.repaint();
    }

    public Color getColor() {
        return this.color;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public void setAnimating(boolean animating) {
        this.isAnimating = animating;
    }

    public void setScale(float scale) {
        this.scale = scale;
        this.repaint();
    }

    public void setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
        this.repaint();
    }

    public boolean isMovable() {
        return movable;
    }

    public void setMovable(boolean movable) {
        this.movable = movable;
        this.repaint();
    }
}
