package view.game;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AnimationHandler {
    private BoxComponent box;
    private int startX, startY;
    private int targetX, targetY;
    private int duration;
    private Timer timer;
    private long startTime;
    private Runnable onComplete;

    public AnimationHandler(BoxComponent box, int targetX, int targetY, int duration, Runnable onComplete) {
        this.box = box;
        this.startX = box.getX();
        this.startY = box.getY();
        this.targetX = targetX;
        this.targetY = targetY;
        this.duration = duration;
        this.onComplete = onComplete;
        
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
        
        // Use easing function for smoother animation
        float easedProgress = easeOutQuad(progress);
        
        int currentX = (int)(startX + (targetX - startX) * easedProgress);
        int currentY = (int)(startY + (targetY - startY) * easedProgress);
        
        box.setLocation(currentX, currentY);
        box.repaint();
        
        if (progress >= 1.0f) {
            timer.stop();
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    private float easeOutQuad(float t) {
        return t * (2 - t);
    }

    public void start() {
        this.startTime = System.currentTimeMillis();
        timer.start();
    }

    public void stop() {
        timer.stop();
    }
}
