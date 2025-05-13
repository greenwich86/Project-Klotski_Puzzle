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

    public GamePanel(MapModel model) {
        boxes = new ArrayList<>();
        this.setVisible(true);
        this.setFocusable(true);
        this.setLayout(null);
        this.requestFocusInWindow(); // Explicitly request focus

        // Debug model dimensions and contents before initialization
        System.out.println("GamePanel constructor - Model dimensions: " +
                model.getHeight() + "x" + model.getWidth());
        System.out.println("Model matrix preview:");
        for (int i = 0; i < Math.min(2, model.getHeight()); i++) {
            for (int j = 0; j < Math.min(5, model.getWidth()); j++) {
                System.out.print(model.getId(i, j) + " ");
            }
            System.out.println();
        }

        // Ensure panel size matches model dimensions plus exit space below
        int width = 4 * GRID_SIZE + 4;  // Fixed 4 columns
        int height = 5 * GRID_SIZE + GRID_SIZE + 4; // 5 rows + exit row below
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
                    // Calculate position and ensure it stays within panel bounds
                    int x = Math.min(j * GRID_SIZE + 2, this.getWidth() - box.getWidth());
                    int y = Math.min(i * GRID_SIZE + 2, this.getHeight() - box.getHeight());
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
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        // Draw exit position below chessboard (centered, 1x2)
        int exitX = (this.getWidth()/2) - GRID_SIZE; // Center horizontally
        int exitY = 5 * GRID_SIZE; // Below last row
        if (exitHighlighted) {
            g.setColor(new Color(255, 100, 100)); // Bright red when highlighted
        } else {
            g.setColor(new Color(255, 200, 200)); // Light red normally
        }
        g.fillRect(exitX, exitY, 2 * GRID_SIZE, GRID_SIZE);
        g.setColor(Color.RED);
        g.drawRect(exitX, exitY, 2 * GRID_SIZE, GRID_SIZE);

        // Highlight CaoCao block if needed
        if (caoHighlighted) {
            for (BoxComponent box : boxes) {
                if (box.getColor() == Color.RED) { // CaoCao is red
                    g.setColor(new Color(255, 255, 0, 150)); // Yellow highlight
                    g.fillRect(box.getX(), box.getY(), box.getWidth(), box.getHeight());
                }
            }
        }

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