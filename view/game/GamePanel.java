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
    private int GRID_SIZE;
    private BoxComponent selectedBox;

    public GamePanel(MapModel model) {
        boxes = new ArrayList<>();
        this.setVisible(true);
        this.setFocusable(true);
        this.setLayout(null);
        this.requestFocusInWindow(); // Explicitly request focus
        
        // Calculate optimized GRID_SIZE based on model dimensions
        int maxDimension = Math.max(model.getWidth(), model.getHeight());
        int minDimension = Math.min(model.getWidth(), model.getHeight());
        GRID_SIZE = Math.min(70, Math.max(45, 500 / maxDimension - (maxDimension - minDimension) * 2));
        
        System.out.println("Calculated GRID_SIZE: " + GRID_SIZE + 
                         " for board " + model.getWidth() + "x" + model.getHeight());
        
        // Calculate panel size with dynamic padding
        int padding = Math.max(10, GRID_SIZE / 4);
        int width = model.getWidth() * GRID_SIZE + padding * 2;
        int height = (model.getHeight() + 1) * GRID_SIZE + padding * 2; // +1 for exit
        
        System.out.println("Panel dimensions: " + width + "x" + height);
        
        // Set sizes and ensure proper layout
        this.setPreferredSize(new Dimension(width, height));
        this.setMinimumSize(new Dimension(width, height));
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
        // Debug model dimensions and contents
        // System.out.println("Initializing game with model dimensions: " + 
        //     model.getHeight() + "x" + model.getWidth());
        // System.out.println("Model matrix:");
        // for (int i = 0; i < model.getHeight(); i++) {
        //     for (int j = 0; j < model.getWidth(); j++) {
        //         System.out.print(model.getId(i, j) + " ");
        //     }
        //     System.out.println();
        // }
        
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
                    case MapModel.CAO_CAO: // 2x2
                        if (i < map.length - 1 && j < map[0].length - 1) {
                            box = new BoxComponent(Color.RED, i, j);
                            box.setSize(GRID_SIZE * 2, GRID_SIZE * 2);
                        }
                        break;
                    case MapModel.GUAN_YU: // 2x1 horizontal
                        if (j < map[0].length - 1 && map[i][j] == MapModel.GUAN_YU && map[i][j+1] == MapModel.GUAN_YU) {
                            box = new BoxComponent(Color.ORANGE, i, j);
                            box.setSize(GRID_SIZE * 2, GRID_SIZE);
                            map[i][j+1] = 0; // Mark adjacent cell as processed
                        }
                        break;
                    case MapModel.GENERAL: // 1x2 vertical
                        if (i < map.length - 1 && map[i][j] == MapModel.GENERAL && map[i+1][j] == MapModel.GENERAL) {
                            box = new BoxComponent(Color.BLUE, i, j);
                            box.setSize(GRID_SIZE, GRID_SIZE * 2);
                            map[i+1][j] = 0; // Mark lower cell as processed
                        }
                        break;
                    case MapModel.SOLDIER: // 1x1
                        box = new BoxComponent(Color.GREEN, i, j);
                        box.setSize(GRID_SIZE, GRID_SIZE);
                        break;
                    case MapModel.ZHOU_YU: // 1x3 horizontal
                        if (j < map[0].length - 2) {
                            box = new BoxComponent(Color.MAGENTA, i, j);
                            box.setSize(GRID_SIZE * 3, GRID_SIZE);
                        }
                        break;
                    case MapModel.BLOCKED: // Immovable
                        box = new BoxComponent(Color.DARK_GRAY, i, j);
                        box.setSize(GRID_SIZE, GRID_SIZE);
                        box.setMovable(false);
                        break;
                }
                
                if (box != null) {
                    // Calculate precisely centered position
                    int boardWidth = model.getWidth() * GRID_SIZE;
                    int boardHeight = model.getHeight() * GRID_SIZE;
                    // Calculate position with dynamic padding
                    int panelWidth = this.getWidth();
                    int panelHeight = this.getHeight();
                    int xOffset = (panelWidth - boardWidth) / 2;
                    int yOffset = (panelHeight - boardHeight - GRID_SIZE) / 3; // Adjusted vertical centering
                    
                    // Ensure minimum padding
                    if (xOffset < 10) xOffset = 10;
                    if (yOffset < 10) yOffset = 10;
                    
                    // Calculate precise position accounting for block type
                    int x = xOffset + j * GRID_SIZE;
                    int y = yOffset + i * GRID_SIZE;
                    
                    // Special adjustment for Guan Yu blocks to ensure proper grid alignment
                    if (blockType == MapModel.GUAN_YU) {
                        x = xOffset + j * GRID_SIZE;
                        y = yOffset + i * GRID_SIZE;
                        // Verify the block spans exactly 2 grid cells horizontally
                        if (box.getWidth() != GRID_SIZE * 2) {
                            box.setSize(GRID_SIZE * 2, GRID_SIZE);
                        }
                    }
                    
                    System.out.printf("Block at %d,%d positioned at %d,%d (offset %d,%d)\n",
                        i, j, x, y, xOffset, yOffset);
                    box.setLocation(x, y);
                    boxes.add(box);
                    this.add(box);
                    // Mark all occupied cells as processed
                    for (int r = i; r < i + box.getHeight()/GRID_SIZE; r++) {
                        for (int c = j; c < j + box.getWidth()/GRID_SIZE; c++) {
                            if (r < map.length && c < map[0].length) {
                                map[r][c] = 0;
                            }
                        }
                    }
                }
            }
        }
        this.repaint();
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
        System.out.println("Click VK_RIGHT");
        if (selectedBox != null) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.RIGHT)) {
                afterMove();
            }
        }
    }

    @Override
    public void doMoveLeft() {
        System.out.println("Click VK_LEFT");
        if (selectedBox != null) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.LEFT)) {
                afterMove();
            }
        }
    }

    @Override
    public void doMoveUp() {
        System.out.println("Click VK_Up");
        if (selectedBox != null) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.UP)) {
                afterMove();
            }
        }
    }

    @Override
    public void doMoveDown() {
        System.out.println("Click VK_DOWN");
        if (selectedBox != null) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.DOWN)) {
                afterMove();
            }
        }
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
        this.controller = controller;
        this.requestFocusInWindow(); // Ensure panel has focus for key events
        this.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                System.out.println("GamePanel gained focus");
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                System.out.println("GamePanel lost focus");
            }
        });
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

    private boolean exitHighlighted = false;
    private boolean caoHighlighted = false;

    public void highlightExit(boolean highlight) {
        this.exitHighlighted = highlight;
        this.repaint();
    }

    public void highlightCaoCao(boolean highlight) {
        this.caoHighlighted = highlight;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Unified background drawing with perfect alignment
        Graphics2D g2d = (Graphics2D)g;
        
        // Calculate chessboard dimensions
        int boardWidth = model.getWidth() * GRID_SIZE;
        int boardHeight = model.getHeight() * GRID_SIZE;
        
        // Calculate position accounting for level dimensions
        int xOffset = (this.getWidth() - boardWidth) / 2;
        int yOffset;
        
        // Special handling for level 3 (5x4 grid)
        if (model.getWidth() == 5 && model.getHeight() == 4) {
            // Precise calculation for level 3 (5x4 grid) with higher positioning
            yOffset = (this.getHeight() - boardHeight - GRID_SIZE) / 3;
            // Additional upward adjustment
            yOffset = Math.max(10, yOffset - 8);
        } else {
            yOffset = (this.getHeight() - boardHeight - GRID_SIZE) / 3;
        }
        
        // Draw background with level-specific adjustments
        g2d.setColor(new Color(240, 240, 255)); // Light blue-gray
        
        if (model.getWidth() == 5 && model.getHeight() == 4) {
            // Special handling for level 3 (5x4 grid)
            int topHeight = yOffset - 4;
            int bottomHeight = this.getHeight() - (yOffset + boardHeight + 4);
            
            g2d.fillRect(0, 0, this.getWidth(), topHeight); // Top
            g2d.fillRect(0, topHeight, xOffset - 4, boardHeight + 8); // Left
            g2d.fillRect(xOffset + boardWidth + 4, topHeight, 
                       this.getWidth() - (xOffset + boardWidth + 4), boardHeight + 8); // Right
            g2d.fillRect(0, yOffset + boardHeight + 4, this.getWidth(), bottomHeight); // Bottom
        } else {
            // Standard handling for other levels
            g2d.fillRect(0, 0, this.getWidth(), yOffset - 2); // Top
            g2d.fillRect(0, yOffset - 2, xOffset - 2, boardHeight + 4); // Left
            g2d.fillRect(xOffset + boardWidth + 2, yOffset - 2, 
                       this.getWidth() - (xOffset + boardWidth + 2), boardHeight + 4); // Right
            g2d.fillRect(0, yOffset + boardHeight + 2, this.getWidth(), 
                       this.getHeight() - (yOffset + boardHeight + 2)); // Bottom
        }
        
        // Draw chessboard area (wheat color)
        g2d.setColor(new Color(245, 222, 179));
        g2d.fillRect(xOffset - 2, yOffset - 2, boardWidth + 4, boardHeight + 4);
        
        // Draw inner chessboard (cornsilk)
        g2d.setColor(new Color(255, 248, 220));
        g2d.fillRect(xOffset, yOffset, boardWidth, boardHeight);
        
        // Reuse existing board position variables for exit alignment
        
        // Draw exit position centered below chessboard with tight spacing
        int exitWidth = 2 * GRID_SIZE; // Fixed width of 2 grid cells
        int exitX = (this.getWidth() - exitWidth) / 2; // Center in panel
        int exitY = yOffset + boardHeight + 2; // Below board with 2px margin
        
        // Draw exit background
        if (exitHighlighted) {
            g.setColor(new Color(255, 100, 100)); // Bright red when highlighted
        } else {
            g.setColor(new Color(255, 200, 200)); // Light red normally
        }
        g.fillRect(exitX, exitY, exitWidth, GRID_SIZE);
        
        // Draw exit border
        g.setColor(Color.RED);
        g.drawRect(exitX, exitY, exitWidth, GRID_SIZE);
        
        // Draw "EXIT" text
        g.setColor(Color.BLACK);
        Font exitFont = new Font(Font.SANS_SERIF, Font.BOLD, GRID_SIZE/2);
        g.setFont(exitFont);
        FontMetrics fm = g.getFontMetrics();
        String exitText = "EXIT";
        int textX = exitX + (exitWidth - fm.stringWidth(exitText))/2;
        int textY = exitY + GRID_SIZE/2 + fm.getAscent()/2;
        g.drawString(exitText, textX, textY);
        
        Border border = BorderFactory.createLineBorder(Color.DARK_GRAY, 2);
        this.setBorder(border);
    }

    public void updateMoveCount(int count) {
        this.steps = count;
        if (stepLabel != null) {
            stepLabel.setText(String.format("Step: %d", this.steps));
        }
    }
}
