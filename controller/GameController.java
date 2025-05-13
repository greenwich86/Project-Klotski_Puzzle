package controller;

import java.util.Stack;
import javax.swing.JOptionPane;
import model.Direction;
import model.MapModel;
import view.game.BoxComponent;
import view.game.GamePanel;
import java.awt.Color;
import java.io.*;
import org.json.*;

public class GameController {
    private final GamePanel view;
    private MapModel model;
    private Stack<int[][]> moveHistory;
    private String currentUser;
    private int currentLevel = 0;
    private boolean debugMode = false;
    private int moveCount = 0;

    public GameController(GamePanel view, MapModel model) {
        this.moveHistory = new Stack<>();
        this.view = view;
        this.model = model;
        view.setController(this);
    }

    public void toggleDebugMode() {
        debugMode = !debugMode;
        view.resetBoard(model.getMatrix());
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public void setCurrentUser(String username) {
        this.currentUser = username;
    }

    public void restartGame() {
        restartGame(currentLevel);
    }

    public void restartGame(int level) {
        this.currentLevel = level;
        this.model = new MapModel(level);
        view.resetBoard(model.getMatrix());
        this.moveCount = 0;
        this.moveHistory.clear();
        moveHistory.push(model.copyMatrix());
        view.resetBoard(model.getMatrix());
        view.updateMoveCount(0);
        view.requestFocusInWindow();
    }

    public void setLevel(int level) {
        if (level >= 0 && level < MapModel.LEVELS.length) {
            this.currentLevel = level;
            restartGame(level);
        }
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int level) {
        if (level >= 0 && level < MapModel.LEVELS.length) {
            this.currentLevel = level;
        }
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

    public boolean doMove(int row, int col, Direction direction) {
        int blockType = model.getId(row, col);
        int width = 1;
        int height = 1;
        
        if (blockType == MapModel.CAO_CAO) {
            width = 2;
            height = 2;
        } else if (blockType == MapModel.GUAN_YU) {
            width = 2;
            height = 1;
        } else if (blockType == MapModel.GENERAL) {
            width = 1;
            height = 2;
        } else if (blockType == MapModel.ZHOU_YU) {
            width = 3;
            height = 1;
        } else if (blockType == MapModel.BLOCKED) {
            return false;
        }

        if (canMove(row, col, width, height, direction)) {
            int nextRow = row + direction.getRow();
            int nextCol = col + direction.getCol();
            
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
            
            if (blockType == MapModel.GENERAL) {
                model.getMatrix()[nextRow][nextCol] = blockType;
                model.getMatrix()[nextRow + 1][nextCol] = blockType;
                view.resetBoard(model.getMatrix());
                view.repaint();
            } else {
                box.repaint();
            }
            
            moveHistory.push(model.copyMatrix());
            moveCount++;
            view.updateMoveCount(moveCount);
            view.resetBoard(model.getMatrix());
            
            if (blockType == MapModel.CAO_CAO && nextRow == 3 && nextCol == 1) {
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
                    showVictory();
                    return true;
                }
            }
            return true;
        }
        return false;
    }

    private boolean canMove(int row, int col, int width, int height, Direction direction) {
        if (direction == Direction.UP && row == 0) return false;
        if (direction == Direction.DOWN && row + height >= model.getHeight()) return false;
        if (direction == Direction.LEFT && col == 0) return false;
        if (direction == Direction.RIGHT && col + width >= model.getWidth()) return false;
        
        if (direction == Direction.UP) {
            for (int c = col; c < col + width; c++) {
                if (model.getId(row - 1, c) > 0) return false;
            }
        } else if (direction == Direction.DOWN) {
            for (int c = col; c < col + width; c++) {
                if (model.getId(row + height, c) > 0) return false;
            }
        } else if (direction == Direction.LEFT) {
            for (int r = row; r < row + height; r++) {
                if (model.getId(r, col - 1) > 0) return false;
            }
        } else if (direction == Direction.RIGHT) {
            for (int r = row; r < row + height; r++) {
                if (model.getId(r, col + width) > 0) return false;
            }
        }
        return true;
    }

    private void showVictory() {
        for (int i = 0; i < 5; i++) {
            view.highlightExit(true);
            view.highlightCaoCao(true);
            try { Thread.sleep(200); } catch (InterruptedException e) {}
            view.setBackground(Color.YELLOW);
            view.repaint();
            try { Thread.sleep(200); } catch (InterruptedException e) {}
            view.highlightExit(false);
            view.highlightCaoCao(false);
            view.setBackground(Color.LIGHT_GRAY);
            view.repaint();
            try { Thread.sleep(200); } catch (InterruptedException e) {}
        }
        
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
                writer.write(saveData.toString(2));
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

            if (!saveData.getString("username").equals(currentUser)) {
                throw new Exception("Save file does not belong to current user");
            }

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
