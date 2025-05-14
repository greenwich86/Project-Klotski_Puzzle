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
        g.setColor(color);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Set text color and font - use high contrast colors
        Color textColor = color.getRed() + color.getGreen() + color.getBlue() > 382 ? 
                         Color.BLACK : Color.WHITE;
        g.setColor(textColor);
        
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
        g.setColor(textColor);
        g.drawString(name, x, y);
        
        // Draw debug outlines
        g.setColor(Color.RED);
        g.drawRect(0, 0, getWidth()-1, getHeight()-1);
        g.drawRect(x-2, y-fm.getAscent()-2, 
                  fm.stringWidth(name)+4, fm.getHeight()+4);
        
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

    public boolean isMovable() {
        return movable;
    }

    public void setMovable(boolean movable) {
        this.movable = movable;
        this.repaint();
    }


}
