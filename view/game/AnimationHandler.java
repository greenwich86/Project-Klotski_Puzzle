package view.game;

import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

/**
 * Handles smooth animations for block movements with various easing functions.
 * Optimized for performance and different block types.
 */
public class AnimationHandler {
    private BoxComponent box;
    private int startX, startY;
    private int targetX, targetY;
    private int duration;
    private Timer timer;
    private long startTimeNano;  // Using nanoseconds for more precise timing
    private Runnable onComplete;
    private int blockType;       // Store the block type for specialized handling
    private Direction moveDirection; // Direction of movement
    
    // Easing function types
    public enum EasingType {
        LINEAR,
        EASE_IN_QUAD,
        EASE_OUT_QUAD,
        EASE_IN_OUT_QUAD,
        EASE_OUT_BACK,
        EASE_OUT_BOUNCE
    }
    
    // Direction of movement (for specialized animation handling)
    public enum Direction {
        HORIZONTAL,
        VERTICAL
    }
    
    private EasingType easingType = EasingType.EASE_OUT_QUAD;
    
    // Reusable Swing Timer
    private static Timer sharedTimer;
    private static final int FRAME_TIME = 16; // ~60fps
    
    /**
     * Creates a new animation handler with the default easing (EASE_OUT_QUAD).
     */
    public AnimationHandler(BoxComponent box, int targetX, int targetY, int duration, Runnable onComplete) {
        this(box, targetX, targetY, duration, onComplete, EasingType.EASE_OUT_QUAD);
    }
    
    /**
     * Creates a new animation handler with a specific easing function.
     */
    public AnimationHandler(BoxComponent box, int targetX, int targetY, int duration, Runnable onComplete, EasingType easingType) {
        this.box = box;
        this.startX = box.getX();
        this.startY = box.getY();
        this.targetX = targetX;
        this.targetY = targetY;
        this.duration = duration;
        this.onComplete = onComplete;
        this.easingType = easingType;
        
                // Detect movement direction for specialized handling
                this.moveDirection = (Math.abs(targetX - startX) > Math.abs(targetY - startY)) ? 
                                      Direction.HORIZONTAL : Direction.VERTICAL;
                
                // Debug print movement direction
                System.out.println("Animation direction: " + this.moveDirection + 
                                  " (dx=" + (targetX - startX) + ", dy=" + (targetY - startY) + ")");
        
        // Set up timer with higher frame rate for smoother animation
        this.timer = new Timer(FRAME_TIME, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animateStep();
            }
        });
        
        // Make timer not coalesce events for smoother animation
        this.timer.setCoalesce(false);
    }
    
    /**
     * Sets the block type for specialized handling
     */
    public void setBlockType(int blockType) {
        this.blockType = blockType;
        System.out.println("Setting block type to: " + blockType + " in AnimationHandler (General = 3)");
    }

    /**
     * Single animation step
     */
    private void animateStep() {
        // Use nanoTime for more precise timing
        long currentTimeNano = System.nanoTime();
        float elapsedMillis = TimeUnit.NANOSECONDS.toMillis(currentTimeNano - startTimeNano);
        float progress = Math.min(1.0f, elapsedMillis / duration);
        
        // Apply selected easing function
        float easedProgress = applyEasing(progress);
        
        // Calculate new position
        int currentX = (int)(startX + (targetX - startX) * easedProgress);
        int currentY = (int)(startY + (targetY - startY) * easedProgress);
        
        // Debug output for General piece
        if (blockType == 3) {  // 3 is the General piece ID
            System.out.println("Animating General piece: progress=" + progress + 
                              " direction=" + moveDirection +
                              " current=[" + currentX + "," + currentY + "]" +
                              " target=[" + targetX + "," + targetY + "]");
            
            // Add different animation effects based on direction
            if (moveDirection == Direction.VERTICAL) {
                // For vertical movement, add slight horizontal wobble
                int wobbleAmount = (int)(Math.sin(progress * Math.PI * 3) * 3);
                currentX += wobbleAmount;
            } else {
                // For horizontal movement of vertical piece, add bounce effect
                float bounceFactor = (float)Math.abs(Math.sin(progress * Math.PI));
                currentY += (int)(bounceFactor * 4 * (1.0 - progress));
            }
        }
        
        // Set the new position
        box.setLocation(currentX, currentY);
        
        // Optimization: Only repaint the affected area
        box.repaint();
        
        // Animation complete
        if (progress >= 1.0f) {
            timer.stop();
            // Ensure final position is exact
            box.setLocation(targetX, targetY);
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }
    
    /**
     * Applies the selected easing function to the linear progress value.
     */
    private float applyEasing(float t) {
        switch (easingType) {
            case LINEAR: 
                return t;
            case EASE_IN_QUAD: 
                return easeInQuad(t);
            case EASE_OUT_QUAD: 
                return easeOutQuad(t);
            case EASE_IN_OUT_QUAD: 
                return easeInOutQuad(t);
            case EASE_OUT_BACK: 
                return easeOutBack(t);
            case EASE_OUT_BOUNCE: 
                return easeOutBounce(t);
            default: 
                return easeOutQuad(t); // Default fallback
        }
    }
    
    // Linear easing (no easing)
    private float linear(float t) {
        return t;
    }
    
    // Quadratic easing in
    private float easeInQuad(float t) {
        return t * t;
    }
    
    // Quadratic easing out
    private float easeOutQuad(float t) {
        return t * (2 - t);
    }
    
    // Quadratic easing in/out
    private float easeInOutQuad(float t) {
        return t < 0.5f ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }
    
    // Back easing out - slight overshoot
    private float easeOutBack(float t) {
        float s = 1.70158f;
        return (t = t - 1) * t * ((s + 1) * t + s) + 1;
    }
    
    // Bounce easing out
    private float easeOutBounce(float t) {
        if (t < (1/2.75f)) {
            return 7.5625f * t * t;
        } else if (t < (2/2.75f)) {
            return 7.5625f * (t -= (1.5f/2.75f)) * t + 0.75f;
        } else if (t < (2.5/2.75)) {
            return 7.5625f * (t -= (2.25f/2.75f)) * t + 0.9375f;
        } else {
            return 7.5625f * (t -= (2.625f/2.75f)) * t + 0.984375f;
        }
    }

    /**
     * Starts the animation.
     */
    public void start() {
        this.startTimeNano = System.nanoTime();
        timer.start();
    }

    /**
     * Stops the animation immediately without calling the onComplete handler.
     */
    public void stop() {
        timer.stop();
    }
    
    /**
     * Sets the easing type for this animation.
     */
    public void setEasingType(EasingType type) {
        this.easingType = type;
    }
    
    /**
     * Returns a random easing type for variety in animations.
     */
    public static EasingType getRandomEasing() {
        EasingType[] types = EasingType.values();
        return types[(int)(Math.random() * types.length)];
    }
}
