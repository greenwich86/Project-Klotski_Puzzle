package view.game;

import controller.GameController;
import model.Direction;
import model.MapModel;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * It is the subclass of ListenerPanel, so that it should implement those four methods: do move left, up, down ,right.
 * The class contains a grids, which is the corresponding GUI view of the matrix variable in MapMatrix.
 */
public class GamePanel extends ListenerPanel {
    private List<BoxComponent> boxes;
    private MapModel model;
    private GameController controller;
    private JLabel stepLabel;
    private int steps;
    private final int GRID_SIZE = 50;
    private BoxComponent selectedBox;

    private String colorToString(Color color) {
        if (color.equals(Color.RED)) return "CaoCao (2x2)";
        if (color.equals(Color.ORANGE)) return "GuanYu (2x1)"; 
        if (color.equals(Color.BLUE)) return "General (1x2)";
        if (color.equals(Color.GREEN)) return "Soldier (1x1)";
        return "Unknown";
    }

    public GamePanel(MapModel model) {
        boxes = new ArrayList<>();
        this.setVisible(true);
        this.setFocusable(true);
        this.setLayout(null);
        this.requestFocusInWindow(); // Explicitly request focus
        
        
        // Ensure panel size matches model dimensions plus space for undo button
        int width = 4 * GRID_SIZE + 4;  // Fixed 4 columns
        int height = 5 * GRID_SIZE + 4; // Fixed 5 rows
        this.setSize(width, height);
        this.model = model;
        this.selectedBox = null;
        
        try {
            initialGame();
        } catch (Exception e) {
            System.err.println("Error during initialGame():");
            e.printStackTrace();
            throw e;
        }
    }

    /*
                        {1, 2, 2, 1, 1},
                        {3, 4, 4, 2, 2},
                        {3, 4, 4, 1, 0},
                        {1, 2, 2, 1, 0},
                        {1, 1, 1, 1, 1}
     */
    public void initialGame() {
        this.steps = 0;
        
        // Validate model dimensions first (4 columns x 5 rows)
        if (model.getWidth() != 4 || model.getHeight() != 5) {
            throw new IllegalArgumentException("MapModel must be 4 columns x 5 rows");
        }
        
        // Initialize game board from full model
        int[][] map = new int[model.getHeight()][model.getWidth()];
        for (int i = 0; i < model.getHeight(); i++) {
            for (int j = 0; j < model.getWidth(); j++) {
                map[i][j] = model.getId(i, j);
            }
        }
        // Create components for all blocks
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] == 0) continue; // Skip empty cells
                
                BoxComponent box = null;
                int blockType = map[i][j];
                
                switch(blockType) {
                    case 1: // Cao Cao (2x2)
                        if (i < map.length - 1 && j < map[0].length - 1) {
                            box = new BoxComponent(Color.RED, i, j);
                            box.setSize(GRID_SIZE * 2, GRID_SIZE * 2);
                        }
                        break;
                    case 2: // Guan Yu (2x1)
                        if (j < map[0].length - 1) {
                            box = new BoxComponent(Color.ORANGE, i, j);
                            box.setSize(GRID_SIZE * 2, GRID_SIZE);
                        }
                        break;
                    case 3: // General (1x2 vertical)
                        if (i < map.length - 1 && map[i][j] == MapModel.GENERAL && map[i+1][j] == MapModel.GENERAL) {
                            box = new BoxComponent(Color.BLUE, i, j);
                            box.setSize(GRID_SIZE, GRID_SIZE * 2);
                            map[i+1][j] = 0; // Mark lower cell as processed
                        }
                        break;
                    case 4: // Soldier (1x1)
                        box = new BoxComponent(Color.GREEN, i, j);
                        box.setSize(GRID_SIZE, GRID_SIZE);
                        break;
                }
                
                if (box != null) {
                    // Simple grid-aligned positioning
                    int x = j * GRID_SIZE;
                    int y = i * GRID_SIZE;
                    
                    
                    box.setLocation(x, y);
                    boxes.add(box);
                    this.add(box);
                    
                    // Mark occupied cells
                    int rowsOccupied = box.getHeight()/GRID_SIZE;
                    int colsOccupied = box.getWidth()/GRID_SIZE;
                    for (int r = 0; r < rowsOccupied; r++) {
                        for (int c = 0; c < colsOccupied; c++) {
                            if (i+r < map.length && j+c < map[0].length) {
                                map[i+r][j+c] = 0;
                            }
                        }
                    }
                }
            }
        }
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        
        // Draw parchment background with texture
        GradientPaint bgGradient = new GradientPaint(
            0, 0, new Color(245, 235, 200),
            getWidth(), getHeight(), new Color(220, 200, 170));
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Add subtle paper texture
        g2d.setColor(new Color(0, 0, 0, 10));
        for (int i = 0; i < getWidth(); i += 4) {
            g2d.drawLine(i, 0, i, getHeight());
        }
        
        // Draw grid lines aligned with block positions
        g2d.setColor(new Color(139, 69, 19, 50));
        g2d.setStroke(new BasicStroke(1));
        for (int i = 0; i <= 5; i++) { // 5 rows
            g2d.drawLine(0, i * GRID_SIZE, getWidth(), i * GRID_SIZE);
        }
        for (int i = 0; i <= 4; i++) { // 4 columns
            g2d.drawLine(i * GRID_SIZE, 0, i * GRID_SIZE, getHeight());
        }
        
        // Enhanced border
        Border border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(101, 67, 33), 2),
            BorderFactory.createMatteBorder(4, 4, 4, 4, new Color(244, 164, 96))
        );
        this.setBorder(border);
    }

    @Override
    public void doMouseClick(Point point) {
        Component component = this.getComponentAt(point);
        if (component instanceof BoxComponent clickedComponent) {
            if (selectedBox == null) {
                selectedBox = clickedComponent;
                selectedBox.setSelected(true);
            } else if (selectedBox != clickedComponent) {
                selectedBox.setSelected(false);
                clickedComponent.setSelected(true);
                selectedBox = clickedComponent;
            } else {
                clickedComponent.setSelected(false);
                selectedBox = null;
            }
        }
    }

    @Override
    public void doMoveRight() {
        if (selectedBox != null) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.RIGHT)) {
                animatePiece(selectedBox, Direction.RIGHT);
                afterMove();
            }
        }
    }

    @Override
    public void doMoveLeft() {
        if (selectedBox != null) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.LEFT)) {
                animatePiece(selectedBox, Direction.LEFT);
                afterMove();
            }
        }
    }

    @Override
    public void doMoveUp() {
        if (selectedBox != null) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.UP)) {
                animatePiece(selectedBox, Direction.UP);
                afterMove();
            }
        }
    }

    @Override
    public void doMoveDown() {
        if (selectedBox != null) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.DOWN)) {
                animatePiece(selectedBox, Direction.DOWN);
                afterMove();
            }
        }
    }

    private void animatePiece(BoxComponent box, Direction direction) {
        // Calculate new model position
        int newRow = box.getRow();
        int newCol = box.getCol();
        
        switch(direction) {
            case RIGHT: newCol++; break;
            case LEFT: newCol--; break;
            case UP: newRow--; break;
            case DOWN: newRow++; break;
        }
        
        // Update model position first
        box.setRow(newRow);
        box.setCol(newCol);
        
        // Calculate target pixel position
        int x = newCol * GRID_SIZE;
        int y = newRow * GRID_SIZE;
        
        // Bring to front during movement
        box.getParent().setComponentZOrder(box, 0);
        
        // Animate smoothly to new position (300ms duration)
        box.animateTo(new Point(x, y), 300);
        
    }

    public void afterMove() {
        // Step count is now handled by GameController
        if (stepLabel != null) {
            stepLabel.setText(String.format("Step: %d", controller.getMoveCount()));
        }
    }

    public void setStepLabel(JLabel stepLabel) {
        this.stepLabel = stepLabel;
    }


    public void setController(GameController controller) {
        System.err.println("Setting controller: " + controller);
        this.controller = controller;
        this.requestFocusInWindow(); // Ensure panel has focus for key events
    }

    public BoxComponent getSelectedBox() {
        return selectedBox;
    }

    public int getGRID_SIZE() {
        return GRID_SIZE;
    }

    public void resetBoard(int[][] newMatrix) {
        // Clear existing boxes
        for (BoxComponent box : boxes) {
            this.remove(box);
        }
        boxes.clear();
        
        // Update model reference
        this.model = new MapModel(newMatrix);
        
        // Reinitialize game with new board
        initialGame();
    }

    public void updateMoveCount(int count) {
        this.steps = count;
        if (stepLabel != null) {
            stepLabel.setText(String.format("Step: %d", this.steps));
        }
    }
}
