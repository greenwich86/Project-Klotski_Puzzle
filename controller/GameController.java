package controller;

import java.util.Stack;
import javax.swing.JOptionPane;
import model.Direction;
import model.MapModel;
import view.game.BoxComponent;
import view.game.GamePanel;

/**
 * It is a bridge to combine GamePanel(view) and MapMatrix(model) in one game.
 * You can design several methods about the game logic in this class.
 */
public class GameController {
    private final GamePanel view;
    private MapModel model;
    private Stack<int[][]> moveHistory;

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
        if (moveHistory.isEmpty()) {
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
        System.err.println("Checking move from ["+row+"]["+col+"] size "+width+"x"+height+" dir "+direction);
        
        // Check boundaries first
        if (direction == Direction.UP && row == 0) return false;
        if (direction == Direction.DOWN && row + height >= model.getHeight()) return false;
        if (direction == Direction.LEFT && col == 0) return false;
        if (direction == Direction.RIGHT && col + width >= model.getWidth()) return false;
        
        // Check all cells in movement direction
        if (direction == Direction.UP) {
            for (int c = col; c < col + width; c++) {
                if (model.getId(row - 1, c) != 0) return false;
            }
        } 
        else if (direction == Direction.DOWN) {
            for (int c = col; c < col + width; c++) {
                if (model.getId(row + height, c) != 0) return false;
            }
        }
        else if (direction == Direction.LEFT) {
            for (int r = row; r < row + height; r++) {
                if (model.getId(r, col - 1) != 0) return false;
            }
        }
        else if (direction == Direction.RIGHT) {
            for (int r = row; r < row + height; r++) {
                if (model.getId(r, col + width) != 0) return false;
            }
        }
        
        System.out.println("Move valid");
        return true;
    }

    public boolean doMove(int row, int col, Direction direction) {
        System.err.println("Attempting move from ["+row+"]["+col+"] direction "+direction);
        int blockType = model.getId(row, col);
        System.err.println("Block type: "+blockType);
        if (blockType > 0) { // Any block can move
            // Determine block dimensions based on type
            int width = 1;
            int height = 1;
            
            if (blockType == MapModel.CAO_CAO) { // 2x2 block
                width = 2;
                height = 2;
            } else if (blockType == MapModel.GUAN_YU) { // 2x1 block
                width = 2;
            } else if (blockType == MapModel.GENERAL) { // 1x2 block
                height = 2;
            }
            
            System.err.println("Block dimensions: " + width + "x" + height);
            
            if (canMove(row, col, width, height, direction)) {
                // Calculate new top-left position
                int nextRow = row + direction.getRow();
                int nextCol = col + direction.getCol();
                
                // Move the block by clearing old positions and setting new ones
                for (int r = row; r < row + height; r++) {
                    for (int c = col; c < col + width; c++) {
                        model.getMatrix()[r][c] = 0;
                        model.getMatrix()[r + direction.getRow()][c + direction.getCol()] = blockType;
                    }
                }
                
                BoxComponent box = view.getSelectedBox();
                box.setRow(nextRow);
                box.setCol(nextCol);
                box.setLocation(box.getCol() * view.getGRID_SIZE() + 2, box.getRow() * view.getGRID_SIZE() + 2);
                box.repaint();
                
                // Save state after move and update count
                moveHistory.push(model.copyMatrix());
                moveCount++;
                view.updateMoveCount(moveCount);
                
                // Check victory condition (Cao Cao at exit position - bottom row)
                if (blockType == MapModel.CAO_CAO && 
                    nextRow == model.getHeight() - 1 &&  // Bottom row (row 4 in 5-row board)
                    nextCol == 1) {  // Columns 1-2 (2x2 block)
                    showVictory();
                }
                return true;
            }
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

    //todo: add other methods such as loadGame, saveGame...

}
