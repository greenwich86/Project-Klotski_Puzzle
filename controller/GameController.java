package controller;

import java.util.Stack;
import javax.swing.JOptionPane;
import model.Direction;
import model.MapModel;
import view.game.AnimationHandler;
import view.game.BoxComponent;
import view.game.GamePanel;
import java.awt.Color;
import java.io.*;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * It is a bridge to combine GamePanel(view) and MapMatrix(model) in one game.
 * You can design several methods about the game logic in this class.
 */
public class GameController {
    private final GamePanel view;
    private MapModel model;
    private Stack<int[][]> moveHistory;
    private String currentUser;
    private int currentLevel;

    public GameController(GamePanel view, MapModel model) {
        this.moveHistory = new Stack<>();
        this.view = view;
        this.model = model;
        this.currentLevel = 0; // Default to first level
        view.setController(this);
    }

    private int moveCount = 0;

    public int getMoveCount() {
        return moveCount;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public void setLevel(int level) {
        // Reset game with specified level
        this.currentLevel = level;
        this.model = new MapModel(level);
        this.moveCount = 0;
        this.moveHistory.clear();
        moveHistory.push(model.copyMatrix());
        view.resetBoard(model.getMatrix());
        view.updateMoveCount(0);
        view.requestFocusInWindow();
    }

    public void restartGame() {
        restartGame(currentLevel);
    }

    public void restartGame(int level) {
        // Reset to specified level's initial board state
        this.currentLevel = level;
        this.model = new MapModel(level);
        this.moveCount = 0;
        this.moveHistory.clear();
        // Save initial state to allow undo back to start
        moveHistory.push(model.copyMatrix());
        view.resetBoard(model.getMatrix());
        view.updateMoveCount(0);
        view.requestFocusInWindow();
    }


    public int getLevelCount() {
        return MapModel.LEVELS.length;
    }

    public boolean undoMove() {
        if (moveHistory.isEmpty() || moveCount <= 0) {
            return false;
        }

        int[][] previousState = moveHistory.pop();
        this.model = new MapModel(previousState);
        this.moveCount--;
        view.resetBoard(previousState);
        view.updateMoveCount(moveCount);
        return true;
    }

    private boolean canMove(int row, int col, int width, int height, Direction direction) {
        // System.err.println("Checking move from ["+row+"]["+col+"] size "+width+"x"+height+" dir "+direction);

        // Check boundaries first
        if (direction == Direction.UP && row == 0) return false;
        if (direction == Direction.DOWN && row + height >= model.getHeight()) return false;
        if (direction == Direction.LEFT && col == 0) return false;
        if (direction == Direction.RIGHT && col + width >= model.getWidth()) return false;

        // Check all cells in movement direction
        if (direction == Direction.UP) {
            for (int c = col; c < col + width; c++) {
                if (model.getId(row - 1, c) > 0) return false;
            }
        }
        else if (direction == Direction.DOWN) {
            for (int c = col; c < col + width; c++) {
                if (model.getId(row + height, c) > 0) return false;
            }
        }
        else if (direction == Direction.LEFT) {
            for (int r = row; r < row + height; r++) {
                if (model.getId(r, col - 1) > 0) return false;
            }
        }
        else if (direction == Direction.RIGHT) {
            for (int r = row; r < row + height; r++) {
                if (model.getId(r, col + width) > 0) return false;
            }
        }

        // System.out.println("Move valid");
        return true;
    }

    public boolean doMove(int row, int col, Direction direction) {
        // Get the block type at the current position
        int blockType = model.getId(row, col);
        
        // Determine block dimensions based on type
        int width = 1;
        int height = 1;
        final boolean isGeneral = blockType == MapModel.GENERAL; // Make final for lambda usage

        if (blockType == MapModel.CAO_CAO) { // 2x2 block
            width = 2;
            height = 2;
        } else if (blockType == MapModel.GUAN_YU) { // 2x1 block
            width = 2;
            height = 1;
        } else if (blockType == MapModel.GENERAL) { // 1x2 block
            width = 1;
            height = 2;
            // isGeneral is already set above
        } else if (blockType == MapModel.ZHOU_YU) { // 1x3 block
            width = 3;
            height = 1;
        } else if (blockType == MapModel.BLOCKED) { // Immovable
            return false;
        }

        boolean canMove = canMove(row, col, width, height, direction);
        
        if (canMove) {
            // Calculate new top-left position
            final int nextRow = row + direction.getRow();
            final int nextCol = col + direction.getCol();
            
            // Save current matrix state before modifying
            final int[][] originalMatrix = model.copyMatrix();
            
            // Get the selected box component
            final BoxComponent box = view.getSelectedBox();
            
            // Store original position for animations
            final int originalX = box.getX();
            final int originalY = box.getY();
            
            // Update the component's logical position
            box.setRow(nextRow);
            box.setCol(nextCol);
            
            // Clear the old positions in the model
            clearOldPositions(row, col, width, height);
            
            // Set the new positions in the model
            setNewPositions(nextRow, nextCol, width, height, blockType);
            
            // Calculate animation target
            int xOffset = (view.getWidth() - model.getWidth() * view.getGRID_SIZE()) / 2;
            int yOffset = (view.getHeight() - model.getHeight() * view.getGRID_SIZE() - view.getGRID_SIZE()) / 3;
            if (xOffset < 10) xOffset = 10;
            if (yOffset < 10) yOffset = 10;
            
            // Calculate precise target position within the grid
            int targetX = xOffset + nextCol * view.getGRID_SIZE();
            int targetY = yOffset + nextRow * view.getGRID_SIZE();
            
            // Safety check bounds
            if (targetX < 0) targetX = 0;
            if (targetY < 0) targetY = 0;
            if (targetX > view.getWidth() - box.getWidth()) {
                targetX = view.getWidth() - box.getWidth();
            }
            if (targetY > view.getHeight() - box.getHeight()) {
                targetY = view.getHeight() - box.getHeight();
            }
            
            // Mark as animating
            box.setAnimating(true);
            
            // Choose animation duration and easing
            int animationDuration = 150;
            
            // Select easing based on block type and direction
            AnimationHandler.EasingType easing;
            if (isGeneral) {
                // Special handling for General piece (1x2 vertical)
                if (direction == Direction.LEFT || direction == Direction.RIGHT) {
                    // More pronounced effect for horizontal movement of vertical pieces
                    easing = AnimationHandler.EasingType.EASE_OUT_BACK;
                    // Slightly longer animation for horizontal movement
                    animationDuration = 180;
                } else {
                    // Standard easing for vertical movement
                    easing = AnimationHandler.EasingType.EASE_OUT_QUAD;
                }
            } else if (blockType == MapModel.CAO_CAO) {
                // Smoother movement for the main character
                easing = AnimationHandler.EasingType.EASE_OUT_QUAD;
            } else {
                // Default easing for other pieces
                easing = AnimationHandler.EasingType.EASE_OUT_QUAD;
            }
            
            // Test if we're moving a General piece in left/right direction
            final boolean isHorizontalMoveOfVerticalPiece = isGeneral && 
                                                      (direction == Direction.LEFT || 
                                                       direction == Direction.RIGHT);
    
            // Debug print
            System.out.println("Animating " + 
                              (isGeneral ? "General" : "other") + 
                              " piece " + blockType + 
                              " dir=" + direction + 
                              " from [" + row + "," + col + "] to [" + nextRow + "," + nextCol + "]" +
                              " isHorizontal=" + isHorizontalMoveOfVerticalPiece);
                
            // Create animation handler and configure it
            final AnimationHandler animation = new AnimationHandler(
                box, 
                targetX, 
                targetY, 
                animationDuration, 
                () -> {
                    // Animation complete callback
                    box.setAnimating(false);
                    
                    // Special handling for General pieces, but in a unified way
                    if (isGeneral) {
                        // The General is a vertical piece (1 wide, 2 tall)
                        // Update both cells in the matrix to ensure consistency
                        model.getMatrix()[nextRow][nextCol] = blockType;
                        
                        // Set second cell (below first cell)
                        if (nextRow + 1 < model.getHeight()) {
                            model.getMatrix()[nextRow + 1][nextCol] = blockType;
                        }
                        
                        // Ensure repaint happens regardless
                        box.repaint();
                    } else {
                        // For other pieces, just repaint
                        box.repaint();
                    }
                    
                    // Save game state - shared logic for all pieces
                    moveHistory.push(model.copyMatrix());
                    moveCount++;
                    view.updateMoveCount(moveCount);
                    
                    // Check for victory
                    checkVictoryCondition(blockType, nextRow, nextCol);
                },
                easing
            );
            
            // Set block type and start the animation
            animation.setBlockType(blockType);
            animation.start();
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Clear positions for the block being moved
     */
    private void clearOldPositions(int row, int col, int width, int height) {
        for (int r = row; r < row + height; r++) {
            for (int c = col; c < col + width; c++) {
                if (r < model.getHeight() && c < model.getWidth()) {
                    model.getMatrix()[r][c] = 0;
                }
            }
        }
    }
    
    /**
     * Set new positions for the block after movement
     */
    private void setNewPositions(int nextRow, int nextCol, int width, int height, int blockType) {
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (nextRow + r < model.getHeight() && nextCol + c < model.getWidth()) {
                    model.getMatrix()[nextRow + r][nextCol + c] = blockType;
                }
            }
        }
    }
    
    private void checkVictoryCondition(int blockType, int nextRow, int nextCol) {
        // Debug output for movement
        System.out.printf("Moving block %d to [%d][%d] (model size %dx%d)\n",
                blockType, nextRow, nextCol, model.getWidth(), model.getHeight());

        // Check victory condition (Cao Cao covering exit position)
        // Debug victory condition check
        System.out.printf("Checking victory for block %d at [%d][%d] (model size %dx%d)\n",
                blockType, nextRow, nextCol, model.getWidth(), model.getHeight());

        // Check victory condition when CaoCao moves to exit position
        if (blockType == MapModel.CAO_CAO) {
            // Verify all 4 positions of CaoCao block (2x2)
            boolean validPosition = true;
            for (int r = nextRow; r < nextRow + 2; r++) {
                for (int c = nextCol; c < nextCol + 2; c++) {
                    if (r >= model.getHeight() || c >= model.getWidth() ||
                            model.getId(r, c) != MapModel.CAO_CAO) {
                        validPosition = false;
                        System.out.printf("  [%d][%d]: %s (expected CAO_CAO)\n",
                                r, c,
                                r >= model.getHeight() || c >= model.getWidth() ?
                                        "OUT_OF_BOUNDS" : model.getId(r, c));
                    } else {
                        System.out.printf("  [%d][%d]: OK\n", r, c);
                    }
                }
            }

            // Victory occurs when CaoCao covers exit position (rows 3-4, columns 1-2)
            boolean coversExitPosition = (nextRow == 3 && nextCol == 1);

            // Victory condition - CaoCao must cover exit position (row 3, col 1)
            if (blockType == MapModel.CAO_CAO && nextRow == 3 && nextCol == 1) {
                // Additional check that all 4 CaoCao positions are valid
                boolean allPositionsValid = true;
                for (int r = nextRow; r < nextRow + 2; r++) {
                    for (int c = nextCol; c < nextCol + 2; c++) {
                        if (model.getId(r, c) != MapModel.CAO_CAO) {
                            allPositionsValid = false;
                            break;
                        }
                    }
                    if (!allPositionsValid) break;
                }

                if (allPositionsValid) {
                    System.out.println("***** VICTORY! CaoCao at exit position with DOWN press *****");
                    System.out.println("CaoCao covers:");
                    System.out.println("  [3][1] - [3][2]");
                    System.out.println("  [4][1] - [4][2]");

                    showVictory();
                }
            }

            for (int r = 0; r < model.getHeight(); r++) {
                for (int c = 0; c < model.getWidth(); c++) {
                    System.out.printf("%2d ", model.getId(r, c));
                }
                System.out.println();
            }
        }
    }

    private void showVictory() {
        // More prominent victory feedback
        for (int i = 0; i < 5; i++) {
            // Flash exit and highlight CaoCao
            view.highlightExit(true);
            view.highlightCaoCao(true);
            try { Thread.sleep(200); } catch (InterruptedException e) {}

            // Flash entire panel background
            view.setBackground(Color.YELLOW);
            view.repaint();
            try { Thread.sleep(200); } catch (InterruptedException e) {}

            view.highlightExit(false);
            view.highlightCaoCao(false);
            view.setBackground(Color.LIGHT_GRAY);
            view.repaint();
            try { Thread.sleep(200); } catch (InterruptedException e) {}
        }

        // More prominent victory message
        JOptionPane.showMessageDialog(view,
                String.format("<html><h1>VICTORY!</h1><br>You won in %d moves!</html>", moveCount),
                "Klotski Puzzle Solved!",
                JOptionPane.INFORMATION_MESSAGE);

        restartGame();
    }

    public void saveGame() {
        if (currentUser == null || currentUser.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Cannot save in guest mode", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            File savesDir = new File("saves");
            if (!savesDir.exists()) {
                savesDir.mkdir();
            }

            JSONObject saveData = new JSONObject();
            saveData.put("username", currentUser);
            saveData.put("moveCount", moveCount);
            saveData.put("boardState", model.getMatrix());

            File saveFile = new File("saves/" + currentUser + ".json");
            try (FileWriter writer = new FileWriter(saveFile)) {
                writer.write(saveData.toString(2)); // Pretty print with 2-space indent
                JOptionPane.showMessageDialog(view, "Game saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Failed to save game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public boolean loadGame() {
        if (currentUser == null || currentUser.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Cannot load in guest mode", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        File saveFile = new File("saves/" + currentUser + ".json");
        if (!saveFile.exists()) {
            JOptionPane.showMessageDialog(view, "No saved game found", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            StringBuilder json = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
            }

            JSONObject saveData = new JSONObject(json.toString());
            if (!saveData.has("username") || !saveData.has("moveCount") || !saveData.has("boardState")) {
                throw new Exception("Invalid save file format");
            }

            // Verify the save belongs to current user
            if (!saveData.getString("username").equals(currentUser)) {
                throw new Exception("Save file does not belong to current user");
            }

            // Restore game state
            moveCount = saveData.getInt("moveCount");
            JSONArray boardArray = saveData.getJSONArray("boardState");
            int[][] loadedMatrix = new int[boardArray.length()][];
            for (int i = 0; i < boardArray.length(); i++) {
                JSONArray row = boardArray.getJSONArray(i);
                loadedMatrix[i] = new int[row.length()];
                for (int j = 0; j < row.length(); j++) {
                    loadedMatrix[i][j] = row.getInt(j);
                }
            }

            this.model = new MapModel(loadedMatrix);
            this.moveHistory.clear();
            this.moveHistory.push(model.copyMatrix());
            view.resetBoard(loadedMatrix);
            view.updateMoveCount(moveCount);
            view.requestFocusInWindow();

            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Failed to load game: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }
}
