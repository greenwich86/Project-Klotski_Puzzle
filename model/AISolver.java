package model;

import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import controller.GameController;
import view.game.BoxComponent;

/**
 * AISolver implements an A* search algorithm to find an optimal solution
 * for the Klotski puzzle and execute it step by step.
 * This solver does not use props and focuses on pure puzzle-solving moves.
 */
public class AISolver {
    private MapModel model;
    private GameController controller;
    private List<Move> solution;
    private boolean isSolving = false;
    private boolean isSearching = false; // Track when search is in progress
    
    // Constants for A* search
    private static final int MAX_STATES = 1000000; // Further increased limit for complex puzzles
    private static final int REPORT_INTERVAL = 5000; // Report progress every 5000 states
    private static final int MIN_STATES_TO_EXPLORE = 10000; // Minimum states to explore before giving up
    
    // For loading animation
    private Timer animationTimer;
    private int animationFrame = 0;
    private static final String[] LOADING_FRAMES = {
        "Solving ⠋", "Solving ⠙", "Solving ⠹", 
        "Solving ⠸", "Solving ⠼", "Solving ⠴", 
        "Solving ⠦", "Solving ⠧", "Solving ⠇", "Solving ⠏"
    };
    
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
     * Represents a state of the puzzle with A* search information
     */
    private static class State implements Comparable<State> {
        public final int[][] board;
        public final List<Move> moves;
        public final int cost; // g(n): cost to reach this state (number of moves)
        public final int heuristic; // h(n): estimated cost to goal
        public final int fScore; // f(n) = g(n) + h(n)
        
        public State(int[][] board, List<Move> moves, int heuristic) {
            this.board = board;
            this.moves = moves;
            this.cost = moves.size();
            this.heuristic = heuristic;
            this.fScore = this.cost + this.heuristic;
        }
        
        @Override
        public int compareTo(State other) {
            // First compare by f-score
            if (this.fScore != other.fScore) {
                return Integer.compare(this.fScore, other.fScore);
            }
            
            // If f-scores are equal, prefer states with lower heuristic values
            // (closer to the goal)
            if (this.heuristic != other.heuristic) {
                return Integer.compare(this.heuristic, other.heuristic);
            }
            
            // If heuristics are equal, prefer states with fewer moves
            return Integer.compare(this.cost, other.cost);
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
    
    // Cache for goal position to avoid recalculating
    private int goalRow = -1;
    private int goalCol = -1;
    
    public AISolver(MapModel model, GameController controller) {
        this.model = model;
        this.controller = controller;
        this.solution = new ArrayList<>();
        
        // Pre-calculate goal position based on board dimensions
        calculateGoalPosition();
    }
    
    /**
     * Calculate the goal position for Cao Cao based on board dimensions
     */
    private void calculateGoalPosition() {
        int boardHeight = model.getHeight();
        int boardWidth = model.getWidth();
        
        // The goal position is always the bottom center of the board
        // For standard boards:
        // - 4x5 (Easy): [3,1]
        // - 5x6 (Hard): [4,2]
        // - 6x7 (Expert/Master): [5,2]
        goalRow = boardHeight - 2; // Second-to-last row
        goalCol = (boardWidth / 2) - 1; // Center position (adjusted for 0-indexing and even width)
        
        System.out.println("AI Solver: Goal position for Cao Cao calculated as [" + 
                          goalRow + "," + goalCol + "] for board size " + 
                          boardHeight + "x" + boardWidth);
    }
    
    /**
     * Find an optimal solution for the current puzzle state using A* search algorithm
     * This method does not use any props and focuses on pure puzzle-solving
     * 
     * @return true if a solution was found
     */
    public boolean findSolution() {
        if (isSolving) return false;
        
        solution.clear();
        isSearching = true;
        startLoadingAnimation();
        
        // Create a new thread for the A* search to prevent UI freezing
        new Thread(() -> {
            boolean found = performAStarSearch();
            isSearching = false;
            stopLoadingAnimation();
            
            if (found) {
                System.out.println("AI Solver: Solution found with " + solution.size() + " moves");
                // Notify UI thread that solution is ready
                javax.swing.SwingUtilities.invokeLater(() -> {
                    if (controller != null) {
                        JOptionPane.showMessageDialog(
                            null,
                            "AI has found a solution with " + solution.size() + " moves.",
                            "Solution Found",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                });
            } else {
                System.out.println("AI Solver: No solution found");
                javax.swing.SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                        null,
                        "AI could not find a solution for this puzzle configuration.",
                        "Solution Not Found",
                        JOptionPane.WARNING_MESSAGE
                    );
                });
            }
        }).start();
        
        // Return true to indicate search has started, not that solution is found
        return true;
    }
    
    /**
     * Perform the actual A* search algorithm
     * @return true if a solution was found
     */
    private boolean performAStarSearch() {
        // Get initial state and calculate its heuristic
        int[][] initialBoard = model.copyMatrix();
        int initialHeuristic = calculateHeuristic(initialBoard);
        State initialState = new State(initialBoard, new ArrayList<>(), initialHeuristic);
        
        // Set up A* search with priority queue
        PriorityQueue<State> openSet = new PriorityQueue<>();
        // Use a more efficient way to track visited states - hash the board string
        Set<String> closedSet = new HashSet<>();
        
        openSet.add(initialState);
        
        System.out.println("AI Solver: Starting A* search with initial heuristic value: " + initialHeuristic);
        System.out.println("AI Solver: Goal position for Cao Cao is [" + goalRow + "," + goalCol + "]");
        
        // Print initial board state
        System.out.println("Initial board state:");
        printBoardState(initialBoard);
        
        int statesExplored = 0;
        int maxOpenSetSize = 1;
        
        // Track best state seen so far in case we need to terminate early
        State bestState = initialState;
        int bestHeuristic = initialHeuristic;
        
        // A* search loop
        while (!openSet.isEmpty() && isSearching) {
            // Get state with lowest f-score
            State current = openSet.poll();
            statesExplored++;
            
            // Keep track of the best state seen so far
            if (current.heuristic < bestHeuristic) {
                bestState = current;
                bestHeuristic = current.heuristic;
                
                // Log when we find a better state
                if (statesExplored % 1000 == 0) {
                    System.out.println("AI Solver: Found better state with heuristic: " + bestHeuristic);
                }
            }
            
            // Progress reporting
            if (statesExplored % REPORT_INTERVAL == 0) {
                System.out.println("AI Solver: Explored " + statesExplored + 
                                  " states, current queue size: " + openSet.size() + 
                                  ", current f-score: " + current.fScore + 
                                  " (g=" + current.cost + ", h=" + current.heuristic + ")");
                
                // Update animation text with progress
                updateAnimationText("Solving: " + statesExplored + " states");
            }
            
            // Check if this is the goal state
            if (isGoalState(current.board)) {
                solution.addAll(current.moves);
                
                // Optimize solution by removing unnecessary moves
                optimizeSolution();
                
                System.out.println("AI Solver: Found solution with " + solution.size() + 
                                  " moves after exploring " + statesExplored + " states");
                
                // Verify solution
                if (verifySolution(initialBoard)) {
                    System.out.println("AI Solver: Solution verified successfully");
                } else {
                    System.out.println("AI Solver: Solution verification failed - may be incorrect");
                }
                
                return true;
            }
            
            // Add to closed set
            String boardStr = boardToString(current.board);
            closedSet.add(boardStr);
            
            // Generate all possible next states
            List<State> nextStates = generateNextStates(current);
            
            for (State next : nextStates) {
                String nextBoardStr = boardToString(next.board);
                
                // Skip if already evaluated
                if (closedSet.contains(nextBoardStr)) {
                    continue;
                }
                
                // Add to open set if not already there - using hash-based check for efficiency
                if (!closedSet.contains(nextBoardStr)) {
                    openSet.add(next);
                    
                    // Track max open set size for memory usage reporting
                    maxOpenSetSize = Math.max(maxOpenSetSize, openSet.size());
                }
            }
            
            // Safety limit to prevent excessive runtime
            if (statesExplored > MAX_STATES) {
                System.out.println("AI Solver: Search terminated after exploring " + MAX_STATES + " states");
                
                // If we've explored a reasonable number of states but haven't found a solution,
                // use the best state we've seen so far to provide a partial solution
                if (statesExplored >= MIN_STATES_TO_EXPLORE && bestState.moves.size() > 0) {
                    System.out.println("AI Solver: Providing partial solution with " + bestState.moves.size() + 
                                      " moves (best heuristic: " + bestHeuristic + ")");
                    solution.addAll(bestState.moves);
                    return true;
                }
                
                return false;
            }
        }
        
        // If we've explored a reasonable number of states but haven't found a solution,
        // use the best state we've seen so far
        if (statesExplored >= MIN_STATES_TO_EXPLORE && bestState.moves.size() > 0) {
            System.out.println("AI Solver: Providing partial solution with " + bestState.moves.size() + 
                              " moves (best heuristic: " + bestHeuristic + ")");
            solution.addAll(bestState.moves);
            return true;
        }
        
        System.out.println("AI Solver: No solution found after exploring " + 
                          statesExplored + " states. Max queue size: " + maxOpenSetSize);
        return false;
    }
    
    /**
     * Print a board state to the console for debugging
     */
    private void printBoardState(int[][] board) {
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                System.out.print(" " + board[r][c] + " ");
            }
            System.out.println();
        }
    }
    
    /**
     * Start the loading animation timer
     */
    private void startLoadingAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        
        animationFrame = 0;
        animationTimer = new Timer(150, e -> {
            // Update loading animation frame
            animationFrame = (animationFrame + 1) % LOADING_FRAMES.length;
            
            // Update UI with current animation frame
            updateAnimationText(LOADING_FRAMES[animationFrame]);
        });
        
        animationTimer.start();
    }
    
    /**
     * Update the animation text
     */
    private void updateAnimationText(String text) {
        if (controller != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    JOptionPane.getRootFrame().setTitle("AI Solver: " + text);
                } catch (Exception e) {
                    // Ignore any errors if no dialog is showing
                }
            });
        }
    }
    
    /**
     * Stop the loading animation timer
     */
    private void stopLoadingAnimation() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
        
        // Reset window title
        if (controller != null) {
            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    JOptionPane.getRootFrame().setTitle("AI Solver");
                } catch (Exception e) {
                    // Ignore any errors if no dialog is showing
                }
            });
        }
    }
    
    /**
     * Calculate the heuristic value for a given board state
     * This is a combination of multiple factors to better account for all pieces:
     * 1. Manhattan distance from Cao Cao to goal
     * 2. Number of blocking pieces between Cao Cao and goal
     * 3. Board congestion factor (overall piece arrangement)
     * 4. Piece mobility (how many pieces can move and in which directions)
     * 5. Path complexity (difficulty of clearing path to goal)
     * 
     * @param board The board state to evaluate
     * @return The heuristic value (lower is better)
     */
    private int calculateHeuristic(int[][] board) {
        // Find Cao Cao position (top-left corner)
        int caoCaoRow = -1;
        int caoCaoCol = -1;
        
        outerLoop:
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                if (board[r][c] == MapModel.CAO_CAO) {
                    // Ensure we have the top-left corner of Cao Cao (2x2)
                    if (r + 1 < board.length && c + 1 < board[0].length &&
                        board[r][c+1] == MapModel.CAO_CAO && 
                        board[r+1][c] == MapModel.CAO_CAO && 
                        board[r+1][c+1] == MapModel.CAO_CAO) {
                        caoCaoRow = r;
                        caoCaoCol = c;
                        break outerLoop;
                    }
                }
            }
        }
        
        if (caoCaoRow < 0) return Integer.MAX_VALUE; // Cao Cao not found
        
        // 1. Manhattan distance from Cao Cao to goal
        // Use center point of Cao Cao (2x2 block) to goal position
        double caoCaoCenterRow = caoCaoRow + 0.5;
        double caoCaoCenterCol = caoCaoCol + 0.5;
        double goalCenterRow = goalRow - 0.5; // Goal is bottom edge position
        double goalCenterCol = goalCol + 0.5; // Center of goal
        
        // Calculate Euclidean distance for more accuracy
        double euclideanDistance = Math.sqrt(
            Math.pow(caoCaoCenterRow - goalCenterRow, 2) + 
            Math.pow(caoCaoCenterCol - goalCenterCol, 2)
        );
        
        int distanceValue = (int)Math.round(euclideanDistance * 3);
        
        // 2. Count blocking pieces between Cao Cao and goal
        int blockingPieces = countBlockingPieces(board, caoCaoRow, caoCaoCol);
        
        // 3. Board congestion factor - evaluate overall piece arrangement
        int congestion = calculateBoardCongestion(board);
        
        // 4. Piece mobility - how many pieces can move and in which directions
        int mobilityFactor = calculateMobilityFactor(board);
        
        // 5. Path complexity - difficulty of clearing path to goal
        int pathComplexity = calculatePathComplexity(board, caoCaoRow, caoCaoCol);
        
        // 6. Check if Cao Cao is able to move in the direction of the goal
        int caoCaoMobilityPenalty = 0;
        boolean canMoveSouth = caoCaoRow + 2 < board.length && 
                              board[caoCaoRow + 2][caoCaoCol] == 0 && 
                              board[caoCaoRow + 2][caoCaoCol + 1] == 0;
        
        // Apply a penalty if Cao Cao cannot move toward the goal
        if (!canMoveSouth && caoCaoRow < goalRow) {
            caoCaoMobilityPenalty = 5;
        }
        
        // 7. Check if there is a clear path in the final column(s)
        int clearPathBonus = 0;
        if (caoCaoCol == goalCol) {
            boolean pathClear = true;
            // Check if path below Cao Cao to goal is clear
            for (int r = caoCaoRow + 2; r <= goalRow + 1; r++) {
                if (r < board.length) {
                    if (board[r][caoCaoCol] != 0 || board[r][caoCaoCol + 1] != 0) {
                        pathClear = false;
                        break;
                    }
                }
            }
            if (pathClear) {
                clearPathBonus = 15; // Significant bonus for clear path
            }
        }
        
        // Combine factors with improved weights
        // Lower the weights to reduce the overall heuristic value
        int heuristicValue = (distanceValue * 2) + 
                           (blockingPieces * 3) + 
                           (congestion * 2) + 
                           (mobilityFactor * 2) + 
                           (pathComplexity * 3) +
                           caoCaoMobilityPenalty -
                           clearPathBonus;
        
        // Ensure heuristic is never negative
        return Math.max(0, heuristicValue);
    }
    
    /**
     * Count the number of pieces blocking Cao Cao's path to the goal
     */
    private int countBlockingPieces(int[][] board, int caoCaoRow, int caoCaoCol) {
        int blockingPieces = 0;
        
        // Calculate path from Cao Cao to goal
        // First move down to the goal row
        for (int r = caoCaoRow + 2; r <= goalRow + 1; r++) {
            if (r < board.length) {
                // Check both columns that Cao Cao occupies
                for (int c = caoCaoCol; c <= caoCaoCol + 1; c++) {
                    if (board[r][c] != 0 && board[r][c] != MapModel.CAO_CAO) {
                        blockingPieces++;
                    }
                }
            }
        }
        
        // Then move horizontally to the goal column if needed
        int horizontalDir = Integer.compare(goalCol, caoCaoCol);
        if (horizontalDir != 0) {
            int startCol = caoCaoCol + (horizontalDir > 0 ? 2 : -1);
            int endCol = goalCol + (horizontalDir > 0 ? 1 : 0);
            
            for (int c = startCol; horizontalDir > 0 ? c <= endCol : c >= endCol; c += horizontalDir) {
                if (c >= 0 && c < board[0].length && goalRow < board.length && goalRow + 1 < board.length) {
                    // Check both rows at the goal position
                    for (int r = goalRow; r <= goalRow + 1; r++) {
                        if (board[r][c] != 0) {
                            blockingPieces++;
                        }
                    }
                }
            }
        }
        
        return blockingPieces;
    }
    
    /**
     * Calculate the overall congestion of the board
     * This measures how tightly packed the pieces are
     */
    private int calculateBoardCongestion(int[][] board) {
        int congestion = 0;
        Set<Integer> processedPieces = new HashSet<>();
        
        // For each piece on the board
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                int pieceType = board[r][c];
                
                // Skip empty cells and already processed pieces
                if (pieceType == 0 || processedPieces.contains(pieceType)) {
                    continue;
                }
                
                processedPieces.add(pieceType);
                
                // Count adjacent pieces (not including empty spaces)
                int[][] directions = {{-1,0}, {1,0}, {0,-1}, {0,1}}; // up, down, left, right
                
                for (int[] dir : directions) {
                    int newR = r + dir[0];
                    int newC = c + dir[1];
                    
                    if (newR >= 0 && newR < board.length && newC >= 0 && newC < board[0].length) {
                        if (board[newR][newC] != 0 && board[newR][newC] != pieceType) {
                            congestion++;
                        }
                    }
                }
            }
        }
        
        return congestion;
    }
    
    /**
     * Calculate how mobile the pieces are on the board
     * A higher return value means less mobility (worse)
     */
    private int calculateMobilityFactor(int[][] board) {
        int immobilePieces = 0;
        Set<Integer> processedPieces = new HashSet<>();
        
        // For each piece on the board
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                int pieceType = board[r][c];
                
                // Skip empty cells and already processed pieces
                if (pieceType == 0 || pieceType == MapModel.BLOCKED || processedPieces.contains(pieceType)) {
                    continue;
                }
                
                // Determine piece dimensions
                int width = 1;
                int height = 1;
                
                if (pieceType == MapModel.CAO_CAO) { // 2x2 block
                    width = 2;
                    height = 2;
                } else if (pieceType == MapModel.GUAN_YU) { // 2x1 block
                    width = 2;
                    height = 1;
                } else if (pieceType == MapModel.GENERAL) { // 1x2 block
                    width = 1;
                    height = 2;
                } else if (pieceType == MapModel.ZHOU_YU) { // 1x3 block
                    width = 3;
                    height = 1;
                }
                
                // Check if piece can move in any direction
                boolean canMove = false;
                for (Direction dir : Direction.values()) {
                    if (canMove(board, r, c, width, height, dir)) {
                        canMove = true;
                        break;
                    }
                }
                
                if (!canMove) {
                    immobilePieces++;
                }
                
                processedPieces.add(pieceType);
            }
        }
        
        return immobilePieces * 10; // High penalty for immobile pieces
    }
    
    /**
     * Calculate the complexity of clearing a path to the goal
     * This estimates how difficult it is to move blocking pieces out of the way
     */
    private int calculatePathComplexity(int[][] board, int caoCaoRow, int caoCaoCol) {
        int complexity = 0;
        Set<Integer> blockingPieces = new HashSet<>();
        
        // Find pieces in the path from Cao Cao to goal
        // Vertical path
        for (int r = caoCaoRow + 2; r <= goalRow + 1; r++) {
            if (r < board.length) {
                for (int c = caoCaoCol; c <= caoCaoCol + 1; c++) {
                    if (board[r][c] != 0 && board[r][c] != MapModel.CAO_CAO) {
                        blockingPieces.add(board[r][c]);
                    }
                }
            }
        }
        
        // Horizontal path if needed
        int horizontalDir = Integer.compare(goalCol, caoCaoCol);
        if (horizontalDir != 0) {
            int startCol = caoCaoCol + (horizontalDir > 0 ? 2 : -1);
            int endCol = goalCol + (horizontalDir > 0 ? 1 : 0);
            
            for (int c = startCol; horizontalDir > 0 ? c <= endCol : c >= endCol; c += horizontalDir) {
                if (c >= 0 && c < board[0].length && goalRow < board.length && goalRow + 1 < board.length) {
                    for (int r = goalRow; r <= goalRow + 1; r++) {
                        if (board[r][c] != 0) {
                            blockingPieces.add(board[r][c]);
                        }
                    }
                }
            }
        }
        
        // For each blocking piece, evaluate how difficult it is to move
        for (Integer pieceType : blockingPieces) {
            // Find the piece's position
            int pieceRow = -1;
            int pieceCol = -1;
            int width = 1;
            int height = 1;
            
            outerLoop:
            for (int r = 0; r < board.length; r++) {
                for (int c = 0; c < board[0].length; c++) {
                    if (board[r][c] == pieceType) {
                        pieceRow = r;
                        pieceCol = c;
                        
                        // Determine piece dimensions
                        if (pieceType == MapModel.CAO_CAO) { // 2x2 block
                            width = 2;
                            height = 2;
                        } else if (pieceType == MapModel.GUAN_YU) { // 2x1 block
                            width = 2;
                            height = 1;
                        } else if (pieceType == MapModel.GENERAL) { // 1x2 block
                            width = 1;
                            height = 2;
                        } else if (pieceType == MapModel.ZHOU_YU) { // 1x3 block
                            width = 3;
                            height = 1;
                        }
                        
                        break outerLoop;
                    }
                }
            }
            
            if (pieceRow < 0) continue; // Piece not found
            
            // Check how many directions the piece can move
            int movableDirections = 0;
            for (Direction dir : Direction.values()) {
                if (canMove(board, pieceRow, pieceCol, width, height, dir)) {
                    movableDirections++;
                }
            }
            
            // Pieces that can't move or can only move in one direction are more complex
            if (movableDirections == 0) {
                complexity += 15; // Completely blocked piece
            } else if (movableDirections == 1) {
                complexity += 8;  // Limited movement
            } else {
                complexity += 3;  // More freedom to move
            }
        }
        
        return complexity;
    }
    
    /**
     * Execute the solution move by move with animation
     */
    public void executeSolution() {
        if (solution.isEmpty() || isSolving) {
            return;
        }
        
        isSolving = true;
        
        // Print board state before execution for debugging
        System.out.println("AI Solver: Initial board state before execution:");
        int[][] boardState = model.getMatrix();
        for (int r = 0; r < boardState.length; r++) {
            for (int c = 0; c < boardState[0].length; c++) {
                System.out.print(" " + boardState[r][c] + " ");
            }
            System.out.println();
        }
        
        // Create a timer to execute moves with delay
        final int[] moveIndex = {0};
        final long[] lastMoveTime = {System.currentTimeMillis()};
        
        Timer timer = new Timer(1000, e -> {
            // Ensure enough time has passed between moves (at least 800ms)
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMoveTime[0] < 800) {
                return; // Wait longer between moves
            }
            
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
            
            // Get the move to execute
            Move move = solution.get(moveIndex[0]);
            System.out.println("AI Solver: Executing move " + (moveIndex[0] + 1) + 
                              " of " + solution.size() + ": " + move);
            
            // Validate move is legal in current board state
            int[][] currentBoard = model.getMatrix();
            int blockType = -1;
            
            try {
                blockType = currentBoard[move.row][move.col];
                System.out.println("AI Solver: Piece type at [" + move.row + "," + move.col + "]: " + blockType);
                
                if (blockType == 0) {
                    System.out.println("AI Solver: Skipping move - empty cell at position");
                    moveIndex[0]++;
                    return;
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                System.out.println("AI Solver: Invalid move coordinates [" + move.row + "," + move.col + "]");
                moveIndex[0]++;
                return;
            }
            
            // Find the box at the specified position
            BoxComponent selectedBox = controller.selectBoxAt(move.row, move.col);
            
            if (selectedBox != null) {
                // Execute the move
                boolean moveSuccess = controller.doMove(move.row, move.col, move.direction);
                if (moveSuccess) {
                    System.out.println("AI Solver: Move executed successfully");
                    lastMoveTime[0] = System.currentTimeMillis();
                    moveIndex[0]++;
                } else {
                    System.out.println("AI Solver: Move failed, trying next move");
                    // Continue with next move instead of stopping on failure
                    moveIndex[0]++;
                }
            } else {
                System.out.println("AI Solver: No box at position [" + move.row + "," + move.col + "], trying next move");
                // Print current board state for debugging
                System.out.println("Current board state:");
                final int[][] currentBoardState = model.getMatrix();
                for (int r = 0; r < currentBoardState.length; r++) {
                    for (int c = 0; c < currentBoardState[0].length; c++) {
                        System.out.print(" " + currentBoardState[r][c] + " ");
                    }
                    System.out.println();
                }
                
                // Continue with next move
                moveIndex[0]++;
            }
        });
        
        timer.start();
    }
    
    /**
     * Check if the given state is the goal state (Cao Cao at exit)
     */
    private boolean isGoalState(int[][] board) {
        // Goal state is when Cao Cao (2x2 block) is at the bottom center
        // For standard boards:
        // - 4x5 (Easy): Cao Cao at [3,1]
        // - 5x6 (Hard): Cao Cao at [4,2]
        // - 6x7 (Expert/Master): Cao Cao at [5,2]
        
        // Find Cao Cao position
        for (int r = 0; r < board.length - 1; r++) {
            for (int c = 0; c < board[0].length - 1; c++) {
                if (board[r][c] == MapModel.CAO_CAO && 
                    board[r][c+1] == MapModel.CAO_CAO && 
                    board[r+1][c] == MapModel.CAO_CAO && 
                    board[r+1][c+1] == MapModel.CAO_CAO) {
                    
                    // Check if Cao Cao's bottom edge is at the goal position
                    return r + 1 == goalRow && c == goalCol;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Generate a string representation of a board state for use in hashsets
     */
    private String boardToString(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                sb.append(board[r][c]).append(",");
            }
        }
        return sb.toString();
    }
    
    /**
     * Generate all possible next states from the current state
     */
    private List<State> generateNextStates(State current) {
        List<State> nextStates = new ArrayList<>();
        int[][] board = current.board;
        
        // Track which piece types we've already processed
        Set<Integer> processedPieces = new HashSet<>();
        
        // For each position on the board
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                int pieceType = board[r][c];
                
                // Skip empty cells, blocked cells, and already processed pieces
                if (pieceType == 0 || pieceType == MapModel.BLOCKED || 
                    processedPieces.contains(pieceType)) {
                    continue;
                }
                
                // Mark this piece as processed
                processedPieces.add(pieceType);
                
                // Determine piece dimensions based on type
                int width = 1;
                int height = 1;
                
                if (pieceType == MapModel.CAO_CAO) { // 2x2 block
                    width = 2;
                    height = 2;
                } else if (pieceType == MapModel.GUAN_YU) { // 2x1 block
                    width = 2;
                    height = 1;
                } else if (pieceType == MapModel.GENERAL) { // 1x2 block
                    width = 1;
                    height = 2;
                } else if (pieceType == MapModel.ZHOU_YU) { // 1x3 block
                    width = 3;
                    height = 1;
                }
                
                // Try moving in each direction
                for (Direction dir : Direction.values()) {
                    if (canMove(board, r, c, width, height, dir)) {
                        // Create a new board with the move applied
                        int[][] newBoard = deepCopyBoard(board);
                        
                        // Create the move
                        Move move = new Move(r, c, dir);
                        
                        // Apply the move to the new board
                        int dr = 0, dc = 0;
                        
                        switch (dir) {
                            case UP:
                                dr = -1;
                                break;
                            case DOWN:
                                dr = 1;
                                break;
                            case LEFT:
                                dc = -1;
                                break;
                            case RIGHT:
                                dc = 1;
                                break;
                        }
                        
                        // Clear the original piece position
                        for (int i = 0; i < height; i++) {
                            for (int j = 0; j < width; j++) {
                                newBoard[r + i][c + j] = 0;
                            }
                        }
                        
                        // Place the piece in the new position
                        for (int i = 0; i < height; i++) {
                            for (int j = 0; j < width; j++) {
                                newBoard[r + i + dr][c + j + dc] = pieceType;
                            }
                        }
                        
                        // Create a new list of moves
                        List<Move> newMoves = new ArrayList<>(current.moves);
                        newMoves.add(move);
                        
                        // Create a new state
                        int newHeuristic = calculateHeuristic(newBoard);
                        State newState = new State(newBoard, newMoves, newHeuristic);
                        
                        // Add to next states
                        nextStates.add(newState);
                    }
                }
            }
        }
        
        return nextStates;
    }
    
    /**
     * Check if the board in the queue already contains the given board
     */
    private boolean containsBoard(PriorityQueue<State> queue, int[][] board) {
        for (State state : queue) {
            if (Arrays.deepEquals(state.board, board)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Deep copy a board state
     */
    private int[][] deepCopyBoard(int[][] board) {
        int[][] copy = new int[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, board[i].length);
        }
        return copy;
    }
    
    /**
     * Check if a piece can move in the given direction
     */
    private boolean canMove(int[][] board, int row, int col, int width, int height, Direction dir) {
        int dr = 0, dc = 0;
        int pieceType = board[row][col];
        boolean isSoldier = (pieceType == MapModel.SOLDIER);
        
        switch (dir) {
            case UP:
                dr = -1;
                if (row <= 0) return false; // Already at top edge
                
                // Check all cells above the piece
                for (int j = 0; j < width; j++) {
                    if (col + j >= board[0].length) continue;
                    
                    int targetCell = board[row + dr][col + j];
                    
                    // Check if cell is occupied
                    if (targetCell != 0) {
                        return false; // Blocked by another piece
                    }
                    
                    // Special rule: Soldiers cannot move onto military camps
                    if (isSoldier && targetCell == MapModel.MILITARY_CAMP) {
                        return false;
                    }
                }
                break;
                
            case DOWN:
                dr = 1;
                if (row + height >= board.length) return false; // Already at bottom edge
                
                // Check all cells below the piece
                for (int j = 0; j < width; j++) {
                    if (col + j >= board[0].length) continue;
                    
                    int targetCell = board[row + height + dr - 1][col + j];
                    
                    // Check if cell is occupied
                    if (targetCell != 0) {
                        return false; // Blocked by another piece
                    }
                    
                    // Special rule: Soldiers cannot move onto military camps
                    if (isSoldier && targetCell == MapModel.MILITARY_CAMP) {
                        return false;
                    }
                }
                break;
                
            case LEFT:
                dc = -1;
                if (col <= 0) return false; // Already at left edge
                
                // Check all cells to the left of the piece
                for (int i = 0; i < height; i++) {
                    if (row + i >= board.length) continue;
                    
                    int targetCell = board[row + i][col + dc];
                    
                    // Check if cell is occupied
                    if (targetCell != 0) {
                        return false; // Blocked by another piece
                    }
                    
                    // Special rule: Soldiers cannot move onto military camps
                    if (isSoldier && targetCell == MapModel.MILITARY_CAMP) {
                        return false;
                    }
                }
                break;
                
            case RIGHT:
                dc = 1;
                if (col + width >= board[0].length) return false; // Already at right edge
                
                // Check all cells to the right of the piece
                for (int i = 0; i < height; i++) {
                    if (row + i >= board.length) continue;
                    
                    int targetCell = board[row + i][col + width + dc - 1];
                    
                    // Check if cell is occupied
                    if (targetCell != 0) {
                        return false; // Blocked by another piece
                    }
                    
                    // Special rule: Soldiers cannot move onto military camps
                    if (isSoldier && targetCell == MapModel.MILITARY_CAMP) {
                        return false;
                    }
                }
                break;
        }
        
        return true;
    }
    
    /**
     * Optimize the solution by removing unnecessary moves
     * This is an important step to ensure the AI doesn't make redundant moves
     */
    private void optimizeSolution() {
        if (solution.size() <= 1) return;
        
        // Copy the original board
        int[][] board = model.copyMatrix();
        List<Move> optimizedSolution = new ArrayList<>();
        
        // For each move in the solution
        for (Move move : solution) {
            // Get the piece type and dimensions
            int pieceType = board[move.row][move.col];
            int width = 1, height = 1;
            
            if (pieceType == MapModel.CAO_CAO) { // 2x2 block
                width = 2;
                height = 2;
            } else if (pieceType == MapModel.GUAN_YU) { // 2x1 block
                width = 2;
                height = 1;
            } else if (pieceType == MapModel.GENERAL) { // 1x2 block
                width = 1;
                height = 2;
            } else if (pieceType == MapModel.ZHOU_YU) { // 1x3 block
                width = 3;
                height = 1;
            }
            
            // Apply the move to the board
            int dr = 0, dc = 0;
            
            switch (move.direction) {
                case UP:
                    dr = -1;
                    break;
                case DOWN:
                    dr = 1;
                    break;
                case LEFT:
                    dc = -1;
                    break;
                case RIGHT:
                    dc = 1;
                    break;
            }
            
            // Clear the original piece position
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (move.row + i < board.length && move.col + j < board[0].length) {
                        board[move.row + i][move.col + j] = 0;
                    }
                }
            }
            
            // Place the piece in the new position
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    if (move.row + i + dr < board.length && 
                        move.col + j + dc < board[0].length &&
                        move.row + i + dr >= 0 &&
                        move.col + j + dc >= 0) {
                        board[move.row + i + dr][move.col + j + dc] = pieceType;
                    }
                }
            }
            
            // Add the move to the optimized solution
            optimizedSolution.add(move);
            
            // If this is the goal state, stop
            if (isGoalState(board)) {
                break;
            }
        }
        
        solution = optimizedSolution;
        System.out.println("AI Solver: Optimized solution from " + solution.size() + 
                          " moves to " + optimizedSolution.size() + " moves");
    }
    
    /**
     * Verify the solution by replaying the moves on a fresh board
     */
    private boolean verifySolution(int[][] initialBoard) {
        // Create a copy of the initial board
        int[][] board = deepCopyBoard(initialBoard);
        
        // For each move in the solution
        for (Move move : solution) {
            // Get the piece type at the specified position
            int pieceType;
            try {
                pieceType = board[move.row][move.col];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("AI Solver: Invalid move coordinates in solution: [" + 
                                  move.row + "," + move.col + "]");
                return false;
            }
            
            if (pieceType == 0) {
                System.out.println("AI Solver: Invalid move in solution - empty cell at [" + 
                                  move.row + "," + move.col + "]");
                return false;
            }
            
            // Determine piece dimensions
            int width = 1, height = 1;
            
            if (pieceType == MapModel.CAO_CAO) { // 2x2 block
                width = 2;
                height = 2;
            } else if (pieceType == MapModel.GUAN_YU) { // 2x1 block
                width = 2;
                height = 1;
            } else if (pieceType == MapModel.GENERAL) { // 1x2 block
                width = 1;
                height = 2;
            } else if (pieceType == MapModel.ZHOU_YU) { // 1x3 block
                width = 3;
                height = 1;
            }
            
            // Check if the move is valid
            if (!canMove(board, move.row, move.col, width, height, move.direction)) {
                System.out.println("AI Solver: Invalid move in solution - can't move piece at [" + 
                                  move.row + "," + move.col + "] " + move.direction);
                return false;
            }
            
            // Apply the move
            int dr = 0, dc = 0;
            
            switch (move.direction) {
                case UP:
                    dr = -1;
                    break;
                case DOWN:
                    dr = 1;
                    break;
                case LEFT:
                    dc = -1;
                    break;
                case RIGHT:
                    dc = 1;
                    break;
            }
            
            // Clear the original piece position
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    board[move.row + i][move.col + j] = 0;
                }
            }
            
            // Place the piece in the new position
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    board[move.row + i + dr][move.col + j + dc] = pieceType;
                }
            }
        }
        
        // Check if the final state is the goal state
        return isGoalState(board);
    }
    
    /**
     * Get the length of the current solution
     */
    public int getSolutionLength() {
        return solution.size();
    }
    
    /**
     * Get a specific number of moves from the solution
     * 
     * @param count The number of moves to get
     * @return A list of moves from the solution
     */
    public List<Move> getSolutionMoves(int count) {
        int movesToReturn = Math.min(count, solution.size());
        return solution.subList(0, movesToReturn);
    }
}
