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
    public BoxComponent selectedBox;
    private int horizontalPadding = 150; // Fixed padding for all methods
    private int verticalPadding = 100;   // Fixed padding for all methods
    
    // Special flag for obstacle selection mode - direct approach
    private boolean obstacleSelectionMode = false;

    public GamePanel(MapModel model) {
        boxes = new ArrayList<>();
        this.setVisible(true);
        this.setFocusable(true);
        this.setLayout(null);
        this.requestFocusInWindow(); // Explicitly request focus
        
        // Calculate optimized GRID_SIZE based on model dimensions
        int maxDimension = Math.max(model.getWidth(), model.getHeight());
        
        // Adaptive grid size calculation to ensure entire board is visible
        // Calculate available screen size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int availableWidth = (int)(screenSize.width * 0.8); // Use 80% of screen width
        int availableHeight = (int)(screenSize.height * 0.8); // Use 80% of screen height
        
        // Calculate maximum possible grid size based on screen constraints
        int maxWidthGridSize = (availableWidth - 300) / model.getWidth(); // Account for padding
        int maxHeightGridSize = (availableHeight - 200) / model.getHeight(); // Account for padding
        
        // Choose the smaller of the two to ensure the board fits
        int adaptiveGridSize = Math.min(maxWidthGridSize, maxHeightGridSize);
        
        // Set bounds for grid size
        adaptiveGridSize = Math.max(40, Math.min(70, adaptiveGridSize));
        
        // Use the adaptive grid size
        GRID_SIZE = adaptiveGridSize;
        
        System.out.println("Using adaptive grid size: " + GRID_SIZE + " for board " + 
                         model.getWidth() + "x" + model.getHeight() + 
                         " (max dimension: " + maxDimension + ")");
        
        System.out.println("Standardized GRID_SIZE: " + GRID_SIZE + 
                         " for board " + model.getWidth() + "x" + model.getHeight());
        
        // Calculate board dimensions
        int boardWidth = model.getWidth() * GRID_SIZE;
        int boardHeight = model.getHeight() * GRID_SIZE;
        
        // Space for exit area below board
        int exitSpace = GRID_SIZE + 20;
        
        // Calculate panel size to ensure centered board
        int width = boardWidth + horizontalPadding * 2;
        int height = boardHeight + verticalPadding * 2 + exitSpace;
        
        System.out.println("Panel dimensions: " + width + "x" + height);
        
        // Set sizes and ensure proper layout
        // Set the panel size with a little extra margin to ensure no scrolling is needed
        Dimension panelSize = new Dimension(width + 20, height + 20);
        this.setPreferredSize(panelSize);
        this.setMinimumSize(panelSize);
        this.setSize(panelSize);
        this.model = model;
        this.selectedBox = null;
        
        // Validate layout early to ensure proper dimensions
        this.validate();
        
        // Set background color
        this.setBackground(new Color(240, 240, 255)); // Light blue-gray
        
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
                    case -MapModel.BLOCKED: // Temporarily removed obstacle (lighter color)
                        box = new BoxComponent(new Color(200, 200, 200), i, j); // Light gray color
                        box.setSize(GRID_SIZE, GRID_SIZE);
                        // Allow this to be movable (it's a temporarily removed obstacle)
                        box.setMovable(true);
                        break;
                    case MapModel.MILITARY_CAMP: // Military camp - only soldiers can step on
                        box = new BoxComponent(new Color(139, 69, 19), i, j); // Brown color for military camp
                        box.setSize(GRID_SIZE, GRID_SIZE);
                        box.setMovable(false); // Military camps are immovable
                        // Add Chinese character text display for military camp
                        JLabel campLabel = new JLabel("军营");
                        campLabel.setForeground(Color.WHITE);
                        campLabel.setFont(new Font("SimSun", Font.BOLD, GRID_SIZE / 3));
                        campLabel.setHorizontalAlignment(JLabel.CENTER);
                        campLabel.setBounds(0, 0, GRID_SIZE, GRID_SIZE);
                        box.setLayout(new BorderLayout());
                        box.add(campLabel, BorderLayout.CENTER);
                        break;
                }
                
                if (box != null) {
                // Fixed offsets to ensure consistent positioning across all pieces
                // Calculate precise centered position using fixed values instead of dynamic ones
                int boardWidth = model.getWidth() * GRID_SIZE;
                int boardHeight = model.getHeight() * GRID_SIZE;
                
                // Fixed padding values that won't change with panel resizing
                int xOffset = horizontalPadding;
                int yOffset = verticalPadding;
                
                // Ensure these are consistent with what's used in paintComponent
                if (xOffset < 100) xOffset = 100;
                if (yOffset < 80) yOffset = 80;
                
                // Calculate precise grid-aligned position
                int x = xOffset + j * GRID_SIZE;
                int y = yOffset + i * GRID_SIZE;
                
                // Debug precise positioning
                System.out.println("Box at [" + i + "," + j + "] positioned at exact coordinates: " + 
                                  x + "," + y + " with grid size " + GRID_SIZE);
                
                // Ensure all block types maintain proper grid alignment
                switch (blockType) {
                    case MapModel.GUAN_YU: // 2x1 horizontal
                        if (box.getWidth() != GRID_SIZE * 2) {
                            box.setSize(GRID_SIZE * 2, GRID_SIZE);
                        }
                        break;
                    case MapModel.GENERAL: // 1x2 vertical
                        if (box.getHeight() != GRID_SIZE * 2) {
                            box.setSize(GRID_SIZE, GRID_SIZE * 2);
                        }
                        break;
                    case MapModel.CAO_CAO: // 2x2
                        if (box.getWidth() != GRID_SIZE * 2 || box.getHeight() != GRID_SIZE * 2) {
                            box.setSize(GRID_SIZE * 2, GRID_SIZE * 2);
                        }
                        break;
                    case MapModel.ZHOU_YU: // 3x1 horizontal
                        if (box.getWidth() != GRID_SIZE * 3) {
                            box.setSize(GRID_SIZE * 3, GRID_SIZE);
                        }
                        break;
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
        System.out.println("GamePanel: Mouse click detected at " + point);
        System.out.println("GamePanel: Component at click point: " + (component == null ? "null" : component.getClass().getSimpleName()));
        
        if (component instanceof BoxComponent clickedComponent) {
            // Get the game frame reference (parent container)
            view.game.GameFrame gameFrame = null;
            if (this.getParent() != null && this.getParent().getParent() instanceof view.game.GameFrame) {
                gameFrame = (view.game.GameFrame) this.getParent().getParent();
            }
            
            // Check if we're in obstacle selection mode - new approach
            if (obstacleSelectionMode) {
                System.out.println("GamePanel: In obstacle selection mode, handling click");
                handleObstacleSelection(clickedComponent);
                return; // Exit after handling obstacle selection
            }
            
            // Check if we're in obstacle remover mode via PropPanel
            if (controller != null && gameFrame != null && gameFrame.propPanel != null) {
                boolean obstacleRemoverActive = gameFrame.propPanel.isObstacleRemoverActive();
                if (obstacleRemoverActive) {
                    handleObstacleRemover(clickedComponent, gameFrame);
                    return; // Exit after handling obstacle remover interaction
                }
            }
            
            // Normal piece selection flow
            handleNormalPieceSelection(clickedComponent);
        }
    }
    
    /**
     * Handles obstacle selection in direct selection mode
     * 
     * @param clickedComponent The component that was clicked
     */
    private void handleObstacleSelection(BoxComponent clickedComponent) {
        int row = clickedComponent.getRow();
        int col = clickedComponent.getCol();
        
        // Get piece type directly from model using row and col position
        int pieceType = controller.getModel().getId(row, col);
        
        // Log information for debugging
        System.out.println("GamePanel: Obstacle Selection active. Checking piece at [" + row + "," + col + "]");
        System.out.println("GamePanel: Piece type from model: " + pieceType + 
                         " (BLOCKED=" + MapModel.BLOCKED + ")");
        
        // Check if this is a blocked piece
        if (pieceType == MapModel.BLOCKED) {
            // This is indeed a blocked piece - try to remove it
            System.out.println("GamePanel: Confirmed obstacle at [" + row + "," + col + "] - Removing it");
            
            // Call the controller to handle obstacle removal
            boolean success = controller.useObstacleRemoverProp(row, col);
            System.out.println("GamePanel: Obstacle removal result: " + success);
            
            if (success) {
                // Reset obstacle selection mode
                setObstacleSelectionMode(false);
                
                // Update the board to reflect the removal
                System.out.println("GamePanel: Updating board after obstacle removal");
                resetBoard(controller.getModel().getMatrix());
                
                // Show success message
                JOptionPane.showMessageDialog(this, 
                    "Obstacle successfully removed!",
                    "Obstacle Removed", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                System.out.println("GamePanel: Failed to remove obstacle");
                
                // Reset obstacle selection mode even if failed
                setObstacleSelectionMode(false);
            }
        } else {
            // Not a blocked piece
            System.out.println("GamePanel: Not an obstacle at [" + row + "," + col + "]");
            JOptionPane.showMessageDialog(this,
                "The obstacle remover can only be used on obstacles (gray blocks).",
                "Obstacle Remover",
                JOptionPane.INFORMATION_MESSAGE);
                
            // Reset obstacle selection mode
            setObstacleSelectionMode(false);
        }
    }
    
    /**
     * Handles obstacle remover functionality when a piece is clicked
     * 
     * @param clickedComponent The component that was clicked
     * @param gameFrame The parent game frame
     */
    private void handleObstacleRemover(BoxComponent clickedComponent, view.game.GameFrame gameFrame) {
        int row = clickedComponent.getRow();
        int col = clickedComponent.getCol();
        
        // Get piece type directly from model using row and col position
        int pieceType = controller.getModel().getId(row, col);
        
        // Log information for debugging
        System.out.println("GamePanel: Obstacle Remover active. Checking piece at [" + row + "," + col + "]");
        System.out.println("GamePanel: Piece type from model: " + pieceType + 
                         " (BLOCKED=" + MapModel.BLOCKED + ")");
        System.out.println("GamePanel: Component color: " + clickedComponent.getColor());
        System.out.println("GamePanel: Component is movable: " + clickedComponent.isMovable());
        
        // Check if this is a blocked piece
        if (pieceType == MapModel.BLOCKED) {
            // This is indeed a blocked piece - try to remove it
            System.out.println("GamePanel: Confirmed obstacle at [" + row + "," + col + "] - Removing it");
            
            // Call the controller to handle obstacle removal
            boolean success = controller.useObstacleRemoverProp(row, col);
            System.out.println("GamePanel: Obstacle removal result: " + success);
            
            if (success) {
                // Reset obstacle remover mode
                gameFrame.propPanel.resetObstacleRemoverMode();
                
                // Update the board to reflect the removal
                System.out.println("GamePanel: Updating board after obstacle removal");
                resetBoard(controller.getModel().getMatrix());
                
                // Force UI update
                this.revalidate();
                this.repaint();
                
                // Show success message
                JOptionPane.showMessageDialog(this, 
                    "Obstacle successfully removed!",
                    "Obstacle Removed", 
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                System.out.println("GamePanel: Failed to remove obstacle");
            }
        } else {
            // Not a blocked piece
            System.out.println("GamePanel: Not an obstacle at [" + row + "," + col + "]");
            JOptionPane.showMessageDialog(this,
                "The obstacle remover can only be used on obstacles (gray blocks).",
                "Obstacle Remover",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Handles normal piece selection when not in obstacle remover mode
     * 
     * @param clickedComponent The component that was clicked
     */
    private void handleNormalPieceSelection(BoxComponent clickedComponent) {
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

    /**
     * Checks if any box in the game is currently animating
     * @return true if any box is animating, false otherwise
     */
    private boolean isAnyBoxAnimating() {
        for (BoxComponent box : boxes) {
            if (box.isAnimating()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void doMoveRight() {
        System.out.println("Click VK_RIGHT");
        // Only allow move if no animations are currently running
        if (selectedBox != null && !isAnyBoxAnimating()) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.RIGHT)) {
                afterMove();
            }
        } else if (isAnyBoxAnimating()) {
            System.out.println("Ignoring move - animation in progress");
        }
    }

    @Override
    public void doMoveLeft() {
        System.out.println("Click VK_LEFT");
        // Only allow move if no animations are currently running
        if (selectedBox != null && !isAnyBoxAnimating()) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.LEFT)) {
                afterMove();
            }
        } else if (isAnyBoxAnimating()) {
            System.out.println("Ignoring move - animation in progress");
        }
    }

    @Override
    public void doMoveUp() {
        System.out.println("Click VK_Up");
        // Only allow move if no animations are currently running
        if (selectedBox != null && !isAnyBoxAnimating()) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.UP)) {
                afterMove();
            }
        } else if (isAnyBoxAnimating()) {
            System.out.println("Ignoring move - animation in progress");
        }
    }

    @Override
    public void doMoveDown() {
        System.out.println("Click VK_DOWN");
        // Only allow move if no animations are currently running
        if (selectedBox != null && !isAnyBoxAnimating()) {
            if (controller.doMove(selectedBox.getRow(), selectedBox.getCol(), Direction.DOWN)) {
                afterMove();
            }
        } else if (isAnyBoxAnimating()) {
            System.out.println("Ignoring move - animation in progress");
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
    
    public List<BoxComponent> getBoxes() {
        return boxes;
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
        
        // Use the same fixed offsets for board background as used for pieces
        int boardWidth = model.getWidth() * GRID_SIZE;
        int boardHeight = model.getHeight() * GRID_SIZE;
        
        // Use the same fixed values as initialGame to ensure consistency
        int xOffset = horizontalPadding;
        int yOffset = verticalPadding;
        
        // Ensure minimum consistent margins
        if (xOffset < 100) xOffset = 100;
        if (yOffset < 80) yOffset = 80;
        
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
        
        // Draw exit position centered at the bottom center of the board
        int exitWidth = 2 * GRID_SIZE; // Fixed width of 2 grid cells
        int exitX = xOffset + (boardWidth - exitWidth) / 2; // Center horizontally on the board
        int exitY = yOffset + boardHeight - GRID_SIZE; // Place at the bottom row of the board
        
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
    
    /**
     * Sets the obstacle selection mode
     * 
     * @param active Whether obstacle selection mode is active
     */
    public void setObstacleSelectionMode(boolean active) {
        System.out.println("GamePanel: Setting obstacle selection mode to " + active);
        this.obstacleSelectionMode = active;
        this.setCursor(active ? Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR) : Cursor.getDefaultCursor());
    }
}
