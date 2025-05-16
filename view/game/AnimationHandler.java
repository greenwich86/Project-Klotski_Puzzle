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
    private EasingType easingType = EasingType.EASE_OUT_CUBIC; // Default easing
    
    // Easing function types for different animation styles
    public enum EasingType {
        LINEAR,
        EASE_IN_OUT_QUAD,
        EASE_OUT_CUBIC,
        EASE_OUT_BOUNCE,
        EASE_OUT_ELASTIC,
        EASE_IN_BACK,
        SPRING
    }
    
    // Fixed point coordinates to prevent drift
    private final int fixedY;
    private final int fixedX;
    
    // Animation parameters
    private static final int FRAME_TIME = 6; // ~166fps for even smoother animation
    private static final int BASE_DURATION = 200; // Base duration in milliseconds
    private static final int MIN_DURATION = 150; // Minimum animation duration
    private static final float DISTANCE_FACTOR = 0.9f; // How much distance affects duration
    
    // Debug and optimization settings
    private static boolean DEBUG_OUTPUT = false; // Enable/disable debug console output
    private static boolean ANTI_JITTER = true;   // Enable jitter prevention at animation end
    
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
        if (DEBUG_OUTPUT) {
            System.out.println("Original delta: (" + deltaX + "," + deltaY + ")");
        }
        
        // Force movement even if delta is incorrectly 0,0
        if (Math.abs(deltaX) <= 1 && Math.abs(deltaY) <= 1) {
            // Determine direction from controller's intended move direction
            // This is a fallback for when the calculated delta is too small
            
            // Get direction from logical to physical movement
            int[] moveDirection = getDirectionFromBoxPositions(startRow, startCol, targetX, targetY);
            
            // Apply forced movement of exactly one grid
            this.targetX = startX + (moveDirection[0] * 70);
            this.targetY = startY + (moveDirection[1] * 70);
            
            if (DEBUG_OUTPUT) {
                System.out.println("FORCED MOVEMENT: Grid unit move in direction " + 
                                 moveDirection[0] + "," + moveDirection[1]);
            }
        } else {
            // Normal case - significant delta was provided
            // Normalize to exact grid size
            if (Math.abs(deltaX) > 0 && Math.abs(deltaY) == 0) {
                // Horizontal movement - standardize to 70 pixels
                int direction = deltaX > 0 ? 1 : -1;
                this.targetX = startX + (direction * 70);
                this.targetY = startY;
                if (DEBUG_OUTPUT) {
                    System.out.println("Normalized to exact 70px horizontal movement");
                }
            } else if (Math.abs(deltaY) > 0 && Math.abs(deltaX) == 0) {
                // Vertical movement - standardize to 70 pixels
                int direction = deltaY > 0 ? 1 : -1;
                this.targetX = startX;
                this.targetY = startY + (direction * 70);
                if (DEBUG_OUTPUT) {
                    System.out.println("Normalized to exact 70px vertical movement");
                }
            } else {
                // Use original values if not a clear horizontal/vertical movement
                this.targetX = targetX;
                this.targetY = targetY;
            }
        }
        
        // Calculate adaptive duration based on movement distance
        if (duration <= 0) {
            // Calculate distance to determine adaptive duration
            int distance = Math.max(
                Math.abs(this.targetX - startX), 
                Math.abs(this.targetY - startY)
            );
            
            // Adaptive duration with a minimum value
            this.duration = Math.max(MIN_DURATION, 
                (int)(BASE_DURATION + (distance * DISTANCE_FACTOR)));
            if (DEBUG_OUTPUT) {
                System.out.println("Adaptive animation duration: " + this.duration + "ms");
            }
        } else {
            this.duration = duration;
        }
        this.onComplete = onComplete;
        
        // Debug positions
        if (DEBUG_OUTPUT) {
            System.out.println("Animation initialized: startX=" + startX + ", startY=" + startY + 
                              ", targetX=" + this.targetX + ", targetY=" + this.targetY);
        }
        
        // Store the fixed positions for maintaining strict linear movement
        this.fixedX = startX;
        this.fixedY = startY;
        
        // Determine movement direction based on larger delta
        int diffX = Math.abs(this.targetX - startX);
        int diffY = Math.abs(this.targetY - startY);
        
        if (diffX > diffY) {
            this.moveDirection = Direction.HORIZONTAL;
            if (DEBUG_OUTPUT) {
                System.out.println("HORIZONTAL MOVEMENT - Y will be fixed at " + fixedY);
            }
        } else {
            this.moveDirection = Direction.VERTICAL;
            if (DEBUG_OUTPUT) {
                System.out.println("VERTICAL MOVEMENT - X will be fixed at " + fixedX);
            }
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
     * Applies easing function to create smoother animation
     * Uses easeInOutQuad for natural acceleration/deceleration
     */
    private float easeInOutQuad(float t) {
        return t < 0.5f ? 2 * t * t : 1 - (float)Math.pow(-2 * t + 2, 2) / 2;
    }
    
    /**
     * Cubic easing for even smoother animations
     * Provides more natural movement than quadratic easing
     */
    private float easeOutCubic(float t) {
        return 1 - (float)Math.pow(1 - t, 3);
    }
    
    /**
     * Bounce ease-out for playful movement with a slight bounce at the end
     */
    private float easeOutBounce(float t) {
        float n1 = 7.5625f;
        float d1 = 2.75f;
        
        if (t < 1 / d1) {
            return n1 * t * t;
        } else if (t < 2 / d1) {
            return n1 * (t -= 1.5f / d1) * t + 0.75f;
        } else if (t < 2.5 / d1) {
            return n1 * (t -= 2.25f / d1) * t + 0.9375f;
        } else {
            return n1 * (t -= 2.625f / d1) * t + 0.984375f;
        }
    }
    
    /**
     * Elastic ease-out for a stretchy, rubber-band like effect
     * Creates a nice stretch and settle animation
     */
    private float easeOutElastic(float t) {
        float c4 = (2 * (float)Math.PI) / 3;
        
        if (t == 0 || t == 1) return t;
        return (float)(Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * c4) + 1);
    }
    
    /**
     * Back ease-in for a slight overshoot effect at the beginning
     * Makes the piece appear to gather momentum
     */
    private float easeInBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1;
        
        return c3 * t * t * t - c1 * t * t;
    }
    
    /**
     * Spring animation for natural physics-based movement
     * Simulates a spring with damping for a natural feel
     */
    private float spring(float t) {
        // Dampened spring formula
        float s = 0.3f; // Higher = more oscillation
        float d = 0.5f; // Higher = faster decay
        
        return 1 - (float)(Math.pow(Math.E, -t/d) * Math.cos(s * t * Math.PI * 2));
    }
    
    /**
     * Sets the easing type for this animation
     * Different easing provides different movement feel
     */
    public void setEasingType(EasingType type) {
        this.easingType = type;
    }
    
    /**
     * Applies easing based on the selected easing type
     */
    private float applyEasing(float t) {
        switch (easingType) {
            case LINEAR:
                return t;
            case EASE_IN_OUT_QUAD:
                return easeInOutQuad(t);
            case EASE_OUT_CUBIC:
                return easeOutCubic(t);
            case EASE_OUT_BOUNCE:
                return easeOutBounce(t);
            case EASE_OUT_ELASTIC:
                return easeOutElastic(t);
            case EASE_IN_BACK:
                return easeInBack(t);
            case SPRING:
                return spring(t);
            default:
                return easeOutCubic(t); // Default to cubic for smooth movement
        }
    }
    
    /**
     * Single animation step with strict directional control
     */
    private void animateStep() {
        // Calculate linear progress
        long currentTimeNano = System.nanoTime();
        float elapsedMillis = TimeUnit.NANOSECONDS.toMillis(currentTimeNano - startTimeNano);
        // Linear progress (no longer need aggressive first-frame movement with easing)
        float linearProgress = Math.min(1.0f, elapsedMillis / duration);
        
        // Apply easing for smoother acceleration/deceleration
        float progress = applyEasing(linearProgress);
        
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
        
        // Apply anti-jitter correction for smoother end of animation
        if (ANTI_JITTER && linearProgress > 0.9f) {
            // Snap to final position when very close to end
            if (moveDirection == Direction.HORIZONTAL) {
                newX = targetX;
            } else {
                newY = targetY;
            }
        }
        
        // Debug output (only if enabled)
        if (DEBUG_OUTPUT) {
            System.out.println("ANIMATION: progress=" + progress + 
                             ", direction=" + moveDirection +
                             ", position=[" + newX + "," + newY + "]");
        }
        
        // Set the new position
        box.setLocation(newX, newY);
        box.repaint();
        
        // Animation complete
        if (progress >= 1.0f || linearProgress >= 1.0f) {
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
     * Enables or disables debug output for all animations
     * 
     * @param enabled true to enable debug messages, false to disable
     */
    public static void setDebugOutput(boolean enabled) {
        DEBUG_OUTPUT = enabled;
    }
    
    /**
     * Sets an easing type for a specific block type
     * This allows specific pieces to have distinctive movement styles
     * 
     * @param blockType The type of block (from MapModel constants)
     * @return The recommended easing type for this block
     */
    public static EasingType getRecommendedEasingForBlockType(int blockType) {
        // Different block types can have different animation styles
        switch (blockType) {
            case 1: // Cao Cao - Important piece gets smooth cubic easing
                return EasingType.EASE_OUT_CUBIC;
            case 2: // Guan Yu - Horizontal piece with bounce
                return EasingType.EASE_OUT_BOUNCE;
            case 3: // General - Vertical piece with elastic motion
                return EasingType.EASE_OUT_ELASTIC;
            case 4: // Soldier - Small pieces with quicker motion
                return EasingType.EASE_IN_OUT_QUAD;
            case 5: // Zhou Yu - Special pieces with special effect
                return EasingType.SPRING;
            default:
                return EasingType.EASE_OUT_CUBIC; // Default smooth motion
        }
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
            if (DEBUG_OUTPUT) {
                System.out.println("Detected LEFT intent from row/col and target");
            }
        } else if (targetX > startX) {
            // Right direction
            direction[0] = 1;
            direction[1] = 0;
            if (DEBUG_OUTPUT) {
                System.out.println("Detected RIGHT intent from row/col and target");
            }
        } 
        // Check if key press intent was up/down
        else if (targetY < startY) {
            // Up direction
            direction[0] = 0;
            direction[1] = -1;
            if (DEBUG_OUTPUT) {
                System.out.println("Detected UP intent from row/col and target");
            }
        } else if (targetY > startY) {
            // Down direction
            direction[0] = 0;
            direction[1] = 1;
            if (DEBUG_OUTPUT) {
                System.out.println("Detected DOWN intent from row/col and target");
            }
        } 
        // Fallback - use controller-provided positions
        else {
            // Default to right direction if cannot determine
            // We just need to make SOME movement
            direction[0] = 1;
            direction[1] = 0;
            if (DEBUG_OUTPUT) {
                System.out.println("FALLBACK direction - defaulting to RIGHT");
            }
        }
        
        return direction;
    }
}
