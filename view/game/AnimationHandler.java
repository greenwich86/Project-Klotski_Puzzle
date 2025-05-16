package view.game;

import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

/**
 * Simple animation handler for straight line movements.
 * Fixed to prevent any vertical or horizontal drift during animations.
 */
public class AnimationHandler {
    // Direction of movement 
    public enum Direction {
        HORIZONTAL,
        VERTICAL
    }
    
    private BoxComponent box;
    private int startX, startY;
    private int targetX, targetY;
    private int duration;
    private Timer timer;
    private long startTimeNano;
    private Runnable onComplete;
    private int blockType;
    private Direction moveDirection;
    
    // Fixed point coordinates to prevent drift
    private final int fixedY;
    private final int fixedX;
    
    private static final int FRAME_TIME = 16; // ~60fps
    
    /**
     * Creates a new simple animation handler for straight line movement
     */
    public AnimationHandler(BoxComponent box, int targetX, int targetY, int duration, Runnable onComplete) {
        this.box = box;
        this.startX = box.getX();
        this.startY = box.getY();
        
        // Get row and column for logical position calculation
        int startRow = box.getRow();
        int startCol = box.getCol();
        
        // Calculate the intended direction based on controller movement intent
        int deltaX = targetX - startX;
        int deltaY = targetY - startY;
        System.out.println("Original delta: (" + deltaX + "," + deltaY + ")");
        
        // Force movement even if delta is incorrectly 0,0
        if (Math.abs(deltaX) <= 1 && Math.abs(deltaY) <= 1) {
            // Determine direction from controller's intended move direction
            // This is a fallback for when the calculated delta is too small
            
            // Get direction from logical to physical movement
            int[] moveDirection = getDirectionFromBoxPositions(startRow, startCol, targetX, targetY);
            
            // Apply forced movement of exactly one grid
            this.targetX = startX + (moveDirection[0] * 70);
            this.targetY = startY + (moveDirection[1] * 70);
            
            System.out.println("FORCED MOVEMENT: Grid unit move in direction " + 
                             moveDirection[0] + "," + moveDirection[1]);
        } else {
            // Normal case - significant delta was provided
            // Normalize to exact grid size
            if (Math.abs(deltaX) > 0 && Math.abs(deltaY) == 0) {
                // Horizontal movement - standardize to 70 pixels
                int direction = deltaX > 0 ? 1 : -1;
                this.targetX = startX + (direction * 70);
                this.targetY = startY;
                System.out.println("Normalized to exact 70px horizontal movement");
            } else if (Math.abs(deltaY) > 0 && Math.abs(deltaX) == 0) {
                // Vertical movement - standardize to 70 pixels
                int direction = deltaY > 0 ? 1 : -1;
                this.targetX = startX;
                this.targetY = startY + (direction * 70);
                System.out.println("Normalized to exact 70px vertical movement");
            } else {
                // Use original values if not a clear horizontal/vertical movement
                this.targetX = targetX;
                this.targetY = targetY;
            }
        }
        
        this.duration = duration;
        this.onComplete = onComplete;
        
        // Debug positions
        System.out.println("Animation initialized: startX=" + startX + ", startY=" + startY + 
                          ", targetX=" + this.targetX + ", targetY=" + this.targetY);
        
        // Store the fixed positions for maintaining strict linear movement
        this.fixedX = startX;
        this.fixedY = startY;
        
        // Determine movement direction based on larger delta
        int diffX = Math.abs(this.targetX - startX);
        int diffY = Math.abs(this.targetY - startY);
        
        if (diffX > diffY) {
            this.moveDirection = Direction.HORIZONTAL;
            System.out.println("HORIZONTAL MOVEMENT - Y will be fixed at " + fixedY);
        } else {
            this.moveDirection = Direction.VERTICAL;
            System.out.println("VERTICAL MOVEMENT - X will be fixed at " + fixedX);
        }
        
        // Set up timer with high frame rate for smoother animation
        this.timer = new Timer(FRAME_TIME, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animateStep();
            }
        });
        this.timer.setCoalesce(true); // Ensure events are combined for better performance
    }
    
    /**
     * Sets the block type for specialized handling
     */
    public void setBlockType(int blockType) {
        this.blockType = blockType;
    }

    /**
     * Single animation step with strict directional control
     */
    private void animateStep() {
        // Calculate linear progress
        long currentTimeNano = System.nanoTime();
        float elapsedMillis = TimeUnit.NANOSECONDS.toMillis(currentTimeNano - startTimeNano);
        // More aggressive first-frame movement to ensure immediate visual feedback
        float progress = Math.min(1.0f, Math.max(0.35f, elapsedMillis / duration));
        
        // Calculate current position with STRICT LINEAR MOVEMENT
        int newX, newY;
        
        if (moveDirection == Direction.HORIZONTAL) {
            // Only X changes, Y is absolutely fixed at starting position
            newX = startX + (int)((targetX - startX) * progress);
            newY = fixedY; // Y NEVER changes in horizontal movement
        } else {
            // Only Y changes, X is absolutely fixed at starting position
            newX = fixedX; // X NEVER changes in vertical movement
            newY = startY + (int)((targetY - startY) * progress);
        }
        
        // Debug output
        System.out.println("ANIMATION: progress=" + progress + 
                         ", direction=" + moveDirection +
                         ", position=[" + newX + "," + newY + "]");
        
        // Set the new position
        box.setLocation(newX, newY);
        box.repaint();
        
        // Animation complete
        if (progress >= 1.0f) {
            timer.stop();
            
            // Final position must respect directional constraints
            if (moveDirection == Direction.HORIZONTAL) {
                box.setLocation(targetX, fixedY);
            } else {
                box.setLocation(fixedX, targetY);
            }
            
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }
    
    /**
     * Starts the animation
     */
    public void start() {
        this.startTimeNano = System.nanoTime();
        timer.start();
    }
    
    /**
     * Stops the animation immediately
     */
    public void stop() {
        timer.stop();
    }
    
    /**
     * Determines movement direction based on row and column positions
     * 
     * @param startRow Starting row of the box
     * @param startCol Starting column of the box
     * @param targetX Target X position (physical pixels)
     * @param targetY Target Y position (physical pixels)
     * @return An array with [horizontalDirection, verticalDirection]
     */
    private int[] getDirectionFromBoxPositions(int startRow, int startCol, int targetX, int targetY) {
        int[] direction = new int[2];
        
        // Use GamePanel's row/col coordinates to determine logical direction
        // Check if key press intent was left/right
        if (targetX < startX) {
            // Left direction
            direction[0] = -1;
            direction[1] = 0;
            System.out.println("Detected LEFT intent from row/col and target");
        } else if (targetX > startX) {
            // Right direction
            direction[0] = 1;
            direction[1] = 0;
            System.out.println("Detected RIGHT intent from row/col and target");
        } 
        // Check if key press intent was up/down
        else if (targetY < startY) {
            // Up direction
            direction[0] = 0;
            direction[1] = -1;
            System.out.println("Detected UP intent from row/col and target");
        } else if (targetY > startY) {
            // Down direction
            direction[0] = 0;
            direction[1] = 1;
            System.out.println("Detected DOWN intent from row/col and target");
        } 
        // Fallback - use controller-provided positions
        else {
            // Default to right direction if cannot determine
            // We just need to make SOME movement
            direction[0] = 1;
            direction[1] = 0;
            System.out.println("FALLBACK direction - defaulting to RIGHT");
        }
        
        return direction;
    }
}
