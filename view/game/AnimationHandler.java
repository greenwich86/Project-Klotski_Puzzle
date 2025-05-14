package view.game;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Handles smooth animations for block movements with various easing functions.
 */
public class AnimationHandler {
    private BoxComponent box;
    private int startX, startY;
    private int targetX, targetY;
    private int duration;
    private Timer timer;
    private long startTime;
    private Runnable onComplete;
    
    // Easing function types
    public enum EasingType {
        LINEAR,
        EASE_IN_QUAD,
        EASE_OUT_QUAD,
        EASE_IN_OUT_QUAD,
        EASE_OUT_BACK,
        EASE_OUT_BOUNCE
    }
    
    private EasingType easingType = EasingType.EASE_OUT_QUAD;

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
        
        // Use higher frame rate for smoother animation (60 FPS)
        this.timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animateStep();
            }
        });
    }

    private void animateStep() {
        long currentTime = System.currentTimeMillis();
        float progress = Math.min(1.0f, (float)(currentTime - startTime) / duration);
        
        // Apply selected easing function
        float easedProgress = applyEasing(progress);
        
        int currentX = (int)(startX + (targetX - startX) * easedProgress);
        int currentY = (int)(startY + (targetY - startY) * easedProgress);
        
        box.setLocation(currentX, currentY);
        box.repaint();
        
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
        this.startTime = System.currentTimeMillis();
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
