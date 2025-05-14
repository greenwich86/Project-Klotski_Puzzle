package controller;

import java.util.Stack;
import javax.swing.JOptionPane;
import model.Direction;
import model.MapModel;
import view.game.BoxComponent;
import view.game.GamePanel;
import view.game.AnimationHandler;
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
        // System.err.println("Attempting move from ["+row+"]["+col+"] direction "+direction);
        int blockType = model.getId(row, col);
        // System.err.println("Block type: "+blockType);

        // Determine block dimensions based on type (must be final for use in Runnable)
        final int width;
        final int height;

        if (blockType == MapModel.CAO_CAO) { // 2x2 block
            width = 2;
            height = 2;
        } else if (blockType == MapModel.GUAN_YU) { // 2x1 block
            width = 2;
            height = 1;
        } else if (blockType == MapModel.GENERAL) { // 1x2 block
            width = 1;
            height = 2;
        } else if (blockType == MapModel.ZHOU_YU) { // 1x3 block
            width = 3;
            height = 1;
        } else if (blockType == MapModel.BLOCKED) { // Immovable
            return false;
        } else { // Single square blocks
            width = 1;
            height = 1;
        }

        // System.err.println("Block dimensions: " + width + "x" + height);

        // System.err.println("Checking move for block type " + blockType + " with dimensions " + width + "x" + height);
        boolean canMove = canMove(row, col, width, height, direction);
        // System.err.println("Can move result: " + canMove);
        if (canMove) {
            // Get the block component and update its position immediately
            BoxComponent box = view.getSelectedBox();
            if (box == null) {
                System.err.println("No block selected for movement");
                return false;
            }
            box.setAnimating(true);
            
            // Calculate target position in model coordinates
            int nextRow = row + direction.getRow();
            int nextCol = col + direction.getCol();
            
            System.out.printf("RIGHT movement validation - current: [%d,%d] target: [%d,%d] block size: %dx%d\n",
                col, row, nextCol, nextRow, width, height);
            
            // Validate new position stays within board bounds
            if (nextRow < 0 || nextCol < 0 || 
                nextRow + height > model.getHeight() || 
                nextCol + width > model.getWidth()) {
                return false;
            }
            
            // Calculate target position in absolute coordinates
            int targetX = nextCol * view.getGRID_SIZE();
            int targetY = nextRow * view.getGRID_SIZE();
            
            // Verify position is within game board bounds
            if (targetX < 0 || targetY < 0 || 
                targetX >= view.getGameBoardWidth() - box.getWidth() ||
                targetY >= view.getGameBoardHeight() - box.getHeight()) {
                System.err.println("Invalid target position: " + targetX + "," + targetY);
                return false;
            }
            
            // Verify grid size is correct
            System.out.printf("Grid size: %d, Block size: %dx%d\n",
                view.getGRID_SIZE(), box.getWidth(), box.getHeight());
            
            // Debug output to verify coordinates
            System.out.printf("Moving block from [%d,%d] to [%d,%d] (pixels: %d,%d)\n",
                col, row, nextCol, nextRow, targetX, targetY);
            
            // Update model first before animation starts
            for (int r = row; r < row + height; r++) {
                for (int c = col; c < col + width; c++) {
                    model.getMatrix()[r][c] = 0;
                }
            }
            for (int r = nextRow; r < nextRow + height; r++) {
                for (int c = nextCol; c < nextCol + width; c++) {
                    model.getMatrix()[r][c] = blockType;
                }
            }
            
            // Update box component state immediately
            box.setRow(nextRow);
            box.setCol(nextCol);
            
            System.out.printf("Creating animation - targetX: %d, targetY: %d, direction: %s\n",
                targetX, targetY, direction);
            
            // Create animation with direction-aware movement
            new AnimationHandler(box, targetX, targetY, direction, new Runnable() {
                @Override
                public void run() {
                    // Update model with new positions
                    for (int r = nextRow; r < nextRow + height; r++) {
                        for (int c = nextCol; c < nextCol + width; c++) {
                            model.getMatrix()[r][c] = blockType;
                        }
                    }
                    
                    // Update box component state
                    box.setRow(nextRow);
                    box.setCol(nextCol);
                    box.setAnimating(false);
                    
                    // Save game state
                    moveHistory.push(model.copyMatrix());
                    moveCount++;
                    view.updateMoveCount(moveCount);
                    
                    // Refresh view
                    view.resetBoard(model.getMatrix());
                    view.repaint();
                    
                    // Check victory condition
                    if (blockType == MapModel.CAO_CAO && nextRow == 3 && nextCol == 1) {
                        boolean victory = true;
                        for (int r = nextRow; r < nextRow + 2; r++) {
                            for (int c = nextCol; c < nextCol + 2; c++) {
                                if (model.getId(r, c) != MapModel.CAO_CAO) {
                                    victory = false;
                                    break;
                                }
                            }
                            if (!victory) break;
                        }
                        
                        if (victory) {
                            showVictory();
                        }
                    }
                }
            }).start();
            
            return true;
        }
        return false;
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
