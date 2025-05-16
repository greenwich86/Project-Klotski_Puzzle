package model;

import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import controller.GameController;
import view.game.BoxComponent;

/**
 * AISolver implements a breadth-first search algorithm to find a solution
 * for the Klotski puzzle and execute it step by step.
 */
public class AISolver {
    private MapModel model;
    private GameController controller;
    private List<Move> solution;
    private boolean isSolving = false;
    
    /**
     * Represents a move in the puzzle
     */
    public static class Move {
        public final int row;
        public final int col;
        public final Direction direction;
        
        public Move(int row, int col, Direction direction) {
            this.row = row;
            this.col = col;
            this.direction = direction;
        }
        
        @Override
        public String toString() {
            return String.format("Move piece at [%d,%d] %s", row, col, direction);
        }
    }
    
    /**
     * Represents a state of the puzzle
     */
    private static class State {
        public final int[][] board;
        public final List<Move> moves;
        
        public State(int[][] board, List<Move> moves) {
            this.board = board;
            this.moves = moves;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof State)) return false;
            State other = (State) obj;
            if (board.length != other.board.length || board[0].length != other.board[0].length) {
                return false;
            }
            
            for (int i = 0; i < board.length; i++) {
                for (int j = 0; j < board[0].length; j++) {
                    if (board[i][j] != other.board[i][j]) {
                        return false;
                    }
                }
            }
            return true;
        }
        
        @Override
        public int hashCode() {
            return Arrays.deepHashCode(board);
        }
    }
    
    public AISolver(MapModel model, GameController controller) {
        this.model = model;
        this.controller = controller;
        this.solution = new ArrayList<>();
    }
    
    /**
     * Find a solution for the current puzzle state using an optimized search algorithm
     * @return true if a solution was found
     */
    public boolean findSolution() {
        if (isSolving) return false;
        
        solution.clear();
        
        // Get initial state
        State initialState = new State(model.copyMatrix(), new ArrayList<>());
        
        // Set up optimized bidirectional search
        Queue<State> queue = new PriorityQueue<>((a, b) -> {
            // Prioritize states with Cao Cao closer to the goal
            int[][] boardA = a.board;
            int[][] boardB = b.board;
            
            // Find Cao Cao position in both boards
            int caoCaoDistA = getCaoCaoDistanceToGoal(boardA);
            int caoCaoDistB = getCaoCaoDistanceToGoal(boardB);
            
            // First compare by Cao Cao distance to goal
            if (caoCaoDistA != caoCaoDistB) {
                return caoCaoDistA - caoCaoDistB;
            }
            
            // Then by number of moves (fewer is better)
            return a.moves.size() - b.moves.size();
        });
        
        Set<String> visited = new HashSet<>();
        
        queue.add(initialState);
        visited.add(boardToString(initialState.board));
        
        System.out.println("AI Solver: Starting optimized search...");
        
        int statesExplored = 0;
        int maxQueueSize = 1;
        
        // Enhanced search loop with early termination
        while (!queue.isEmpty()) {
            State current = queue.poll();
            statesExplored++;
            
            // Progress reporting every 1000 states
            if (statesExplored % 1000 == 0) {
                System.out.println("AI Solver: Explored " + statesExplored + 
                                  " states, current queue size: " + queue.size());
            }
            
            // Check if this is the goal state
            if (isGoalState(current.board)) {
                solution.addAll(current.moves);
                System.out.println("AI Solver: Found solution with " + solution.size() + 
                                  " moves after exploring " + statesExplored + " states");
                return true;
            }
            
            // Generate all possible next states
            List<State> nextStates = generateNextStates(current);
            
            for (State next : nextStates) {
                String boardStr = boardToString(next.board);
                if (!visited.contains(boardStr)) {
                    visited.add(boardStr);
                    queue.add(next);
                    
                    // Track max queue size for memory usage reporting
                    maxQueueSize = Math.max(maxQueueSize, queue.size());
                }
            }
            
            // Safety limit to prevent excessive runtime
            if (statesExplored > 100000) {
                System.out.println("AI Solver: Search terminated after exploring 100,000 states");
                return false;
            }
        }
        
        System.out.println("AI Solver: No solution found after exploring " + 
                          statesExplored + " states. Max queue size: " + maxQueueSize);
        return false;
    }
    
    /**
     * Calculate distance from Cao Cao to goal position
     * Lower values are better (closer to goal)
     */
    private int getCaoCaoDistanceToGoal(int[][] board) {
        // Find Cao Cao position
        int caoCaoRow = -1;
        int caoCaoCol = -1;
        
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                if (board[r][c] == MapModel.CAO_CAO) {
                    caoCaoRow = r;
                    caoCaoCol = c;
                    break;
                }
            }
            if (caoCaoRow >= 0) break;
        }
        
        if (caoCaoRow < 0) return Integer.MAX_VALUE; // Cao Cao not found
        
        // Goal position is typically at the bottom-center of the board
        int goalRow = board.length - 2;
        int goalCol = board[0].length / 2 - 1;
        
        // Calculate Manhattan distance
        return Math.abs(caoCaoRow - goalRow) + Math.abs(caoCaoCol - goalCol);
    }
    
    /**
     * Execute the solution move by move with animation
     */
    public void executeSolution() {
        if (solution.isEmpty() || isSolving) {
            return;
        }
        
        isSolving = true;
        
        // Create a timer to execute moves with delay
        final int[] moveIndex = {0};
        Timer timer = new Timer(800, e -> {
            if (moveIndex[0] >= solution.size()) {
                ((Timer)e.getSource()).stop();
                isSolving = false;
                
                // Show success message when solution is complete
                JOptionPane.showMessageDialog(
                    null,
                    "<html><h2>Solution Complete!</h2>" +
                    "The AI has successfully solved the puzzle in " + solution.size() + " moves.</html>",
                    "AI Solution Complete",
                    JOptionPane.INFORMATION_MESSAGE
                );
                
                return;
            }
            
            Move move = solution.get(moveIndex[0]);
            System.out.println("AI Solver: Executing move " + (moveIndex[0] + 1) + 
                              " of " + solution.size() + ": " + move);
            
            // Find the box at the specified position
            BoxComponent selectedBox = controller.selectBoxAt(move.row, move.col);
            
            if (selectedBox != null) {
                // Execute the move
                boolean moveSuccess = controller.doMove(move.row, move.col, move.direction);
                if (!moveSuccess) {
                    System.out.println("AI Solver: Move failed");
                    ((Timer)e.getSource()).stop();
                    isSolving = false;
                    
                    // Show error message
                    JOptionPane.showMessageDialog(
                        null,
                        "Could not execute solution move. The game state may have changed.",
                        "Solution Execution Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    
                    return;
                }
            } else {
                System.out.println("AI Solver: No box at position [" + move.row + "," + move.col + "]");
                ((Timer)e.getSource()).stop();
                isSolving = false;
                
                // Show error message
                JOptionPane.showMessageDialog(
                    null,
                    "Could not find piece at position [" + move.row + "," + move.col + "]",
                    "Solution Execution Error",
                    JOptionPane.ERROR_MESSAGE
                );
                
                return;
            }
            
            moveIndex[0]++;
        });
        
        timer.start();
    }
    
    /**
     * Check if the given state is the goal state (Cao Cao at exit)
     */
    private boolean isGoalState(int[][] board) {
        // Goal is reached when Cao Cao (value 1) is at position (3,1)
        boolean caoCaoAtExit = false;
        
        // Check for Cao Cao at exit position (may vary by puzzle layout)
        for (int row = board.length - 2; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                // Check if Cao Cao is at bottom center position
                if (row < board.length - 1 && col < board[0].length - 1 &&
                    board[row][col] == MapModel.CAO_CAO &&
                    board[row][col+1] == MapModel.CAO_CAO &&
                    board[row+1][col] == MapModel.CAO_CAO &&
                    board[row+1][col+1] == MapModel.CAO_CAO &&
                    col == (board[0].length / 2) - 1) {
                    caoCaoAtExit = true;
                    break;
                }
            }
            if (caoCaoAtExit) break;
        }
        
        return caoCaoAtExit;
    }
    
    /**
     * Generate all possible next states from the current state
     */
    private List<State> generateNextStates(State current) {
        List<State> nextStates = new ArrayList<>();
        int[][] board = current.board;
        
        // For each block on the board
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                int blockType = board[row][col];
                
                // Skip empty cells or already processed blocks
                if (blockType == 0 || (row > 0 && col > 0 && blockType == board[row-1][col-1])) {
                    continue;
                }
                
                // Determine block dimensions
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
                } else if (blockType == MapModel.ZHOU_YU) { // 1x3 block
                    width = 3;
                    height = 1;
                } else if (blockType == MapModel.BLOCKED) { // Immovable
                    continue;
                }
                
                // Try moving in each direction
                for (Direction direction : Direction.values()) {
                    if (canMove(board, row, col, width, height, direction)) {
                        // Create new state with this move
                        int[][] newBoard = copyBoard(board);
                        moveBlock(newBoard, row, col, width, height, direction);
                        
                        List<Move> newMoves = new ArrayList<>(current.moves);
                        newMoves.add(new Move(row, col, direction));
                        
                        nextStates.add(new State(newBoard, newMoves));
                    }
                }
            }
        }
        
        return nextStates;
    }
    
    /**
     * Check if a block can move in the specified direction
     */
    private boolean canMove(int[][] board, int row, int col, int width, int height, Direction direction) {
        // Check boundaries with additional safety checks
        if (direction == Direction.UP && row <= 0) return false;
        if (direction == Direction.DOWN && row + height >= board.length) return false;
        if (direction == Direction.LEFT && col <= 0) return false;
        if (direction == Direction.RIGHT && col + width >= board[0].length) return false;
        
        // Check if block dimensions would be out of bounds
        if (row < 0 || col < 0 || row + height > board.length || col + width > board[0].length) {
            return false;
        }
        
        // Safe bounds check for block type
        int blockType;
        try {
            blockType = board[row][col];
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Error accessing block type at [" + row + "," + col + "]");
            return false;
        }
        
        // Check for obstacles with extra bounds checking
        try {
            if (direction == Direction.UP) {
                for (int c = col; c < col + width; c++) {
                    if (c >= 0 && c < board[0].length && row-1 >= 0 && row-1 < board.length) {
                        if (board[row - 1][c] != 0 && board[row - 1][c] != blockType) {
                            return false;
                        }
                    } else {
                        return false; // Out of bounds
                    }
                }
            } else if (direction == Direction.DOWN) {
                for (int c = col; c < col + width; c++) {
                    if (c >= 0 && c < board[0].length && row+height >= 0 && row+height < board.length) {
                        if (board[row + height][c] != 0 && board[row + height][c] != blockType) {
                            return false;
                        }
                    } else {
                        return false; // Out of bounds
                    }
                }
            } else if (direction == Direction.LEFT) {
                for (int r = row; r < row + height; r++) {
                    if (r >= 0 && r < board.length && col-1 >= 0 && col-1 < board[0].length) {
                        if (board[r][col - 1] != 0 && board[r][col - 1] != blockType) {
                            return false;
                        }
                    } else {
                        return false; // Out of bounds
                    }
                }
            } else if (direction == Direction.RIGHT) {
                for (int r = row; r < row + height; r++) {
                    if (r >= 0 && r < board.length && col+width >= 0 && col+width < board[0].length) {
                        if (board[r][col + width] != 0 && board[r][col + width] != blockType) {
                            return false;
                        }
                    } else {
                        return false; // Out of bounds
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Error checking for obstacles: " + e.getMessage());
            return false;
        }
        
        return true;
    }
    
    /**
     * Move a block in the specified direction on the board
     */
    private void moveBlock(int[][] board, int row, int col, int width, int height, Direction direction) {
        int blockType = board[row][col];
        
        // Clear the old positions
        for (int r = row; r < row + height; r++) {
            for (int c = col; c < col + width; c++) {
                board[r][c] = 0;
            }
        }
        
        // Calculate new position
        int newRow = row + direction.getRow();
        int newCol = col + direction.getCol();
        
        // Set the new positions
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                board[newRow + r][newCol + c] = blockType;
            }
        }
    }
    
    /**
     * Create a deep copy of a board
     */
    private int[][] copyBoard(int[][] board) {
        int[][] copy = new int[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, board[i].length);
        }
        return copy;
    }
    
    /**
     * Convert a board to a string representation for hash set
     */
    private String boardToString(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : board) {
            for (int cell : row) {
                sb.append(cell);
            }
        }
        return sb.toString();
    }
    
    public boolean isSolving() {
        return isSolving;
    }
    
    public int getSolutionLength() {
        return solution.size();
    }
}
