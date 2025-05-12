package controller;

import java.util.Stack;
import javax.swing.JOptionPane;
import model.Direction;
import model.MapModel;
import view.game.BoxComponent;
import view.game.GamePanel;
import java.io.*;
import org.json.*;

/**
 * It is a bridge to combine GamePanel(view) and MapMatrix(model) in one game.
 * You can design several methods about the game logic in this class.
 */
public class GameController {
    private final GamePanel view;
    private MapModel model;
    private Stack<int[][]> moveHistory;
    private String currentUser;

    public GameController(GamePanel view, MapModel model) {
        this.moveHistory = new Stack<>();
        this.view = view;
        this.model = model;
        view.setController(this);
    }

    private int moveCount = 0;
    
    public int getMoveCount() {
        return moveCount;
    }
    
    public void setCurrentUser(String username) {
        this.currentUser = username;
    }
    
    public void restartGame() {
        // Reset to initial board state
        this.model = new MapModel();
        this.moveCount = 0;
        this.moveHistory.clear();
        // Save initial state to allow undo back to start
        moveHistory.push(model.copyMatrix());
        view.resetBoard(model.getMatrix());
        view.updateMoveCount(0);
        view.requestFocusInWindow();
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
        
        // Determine block dimensions based on type
        int width = 1;
        int height = 1;
        
        if (blockType == MapModel.CAO_CAO) { // 2x2 block
            width = 2;
            height = 2;
        } else if (blockType == MapModel.GUAN_YU) { // 2x1 block
            width = 2;
            height = 1;
        } else if (blockType == MapModel.GENERAL) { // 1x2 block
            width = 1;
            height = 2;
        } else { // Single square blocks
            width = 1;
            height = 1;
        }
        
        // System.err.println("Block dimensions: " + width + "x" + height);
        
        // System.err.println("Checking move for block type " + blockType + " with dimensions " + width + "x" + height);
        boolean canMove = canMove(row, col, width, height, direction);
        // System.err.println("Can move result: " + canMove);
        if (canMove) {
            // Calculate new top-left position
            int nextRow = row + direction.getRow();
            int nextCol = col + direction.getCol();
            
            // Move the block by clearing old positions and setting new ones
            for (int r = row; r < row + height; r++) {
                for (int c = col; c < col + width; c++) {
                    model.getMatrix()[r][c] = 0;
                }
            }
            for (int r = row; r < row + height; r++) {
                for (int c = col; c < col + width; c++) {
                    model.getMatrix()[r + direction.getRow()][c + direction.getCol()] = blockType;
                }
            }
            
            BoxComponent box = view.getSelectedBox();
            box.setRow(nextRow);
            box.setCol(nextCol);
            box.setLocation(box.getCol() * view.getGRID_SIZE() + 2, box.getRow() * view.getGRID_SIZE() + 2);
            
            // Special handling for general blocks (1x2)
            if (blockType == MapModel.GENERAL) {
                // Update both positions in the model
                model.getMatrix()[nextRow][nextCol] = blockType;
                model.getMatrix()[nextRow + 1][nextCol] = blockType;
                // Force full panel repaint
                view.resetBoard(model.getMatrix());
                view.repaint();
            } else {
                box.repaint();
            }
            
                // Save state after move and update count
                moveHistory.push(model.copyMatrix());
                moveCount++;
                view.updateMoveCount(moveCount);
                // Force view refresh to ensure proper state
                view.resetBoard(model.getMatrix());
            
            // Check victory condition (Cao Cao at exit position - bottom row)
            if (blockType == MapModel.CAO_CAO && 
                nextRow == model.getHeight() - 1 &&  // Bottom row (row 4 in 5-row board)
                nextCol == 1) {  // Columns 1-2 (2x2 block)
                showVictory();
            }
            return true;
        }
        return false;
    }

    private void showVictory() {
        JOptionPane.showMessageDialog(view, 
            String.format("Congratulations! You won in %d moves!", moveCount),
            "Victory!",
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
