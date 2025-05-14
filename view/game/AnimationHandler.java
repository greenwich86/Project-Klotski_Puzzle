package view.game;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import model.Direction;

public class AnimationHandler {
    private final BoxComponent box;
    private final int targetX;
    private final int targetY;
    private final Direction direction;
    private final Timer timer;
    private final Runnable onComplete;
    private final int gridSize;
    private int currentStep = 0;
    private final int totalSteps = 8; // More steps for smoother animation

    public AnimationHandler(BoxComponent box, int targetX, int targetY, 
                          Direction direction, Runnable onComplete) {
        this.box = box;
        this.targetX = targetX;
        this.targetY = targetY;
        this.direction = direction;
        this.onComplete = onComplete;
        this.gridSize = ((GamePanel)box.getParent()).getGRID_SIZE();

        this.timer = new Timer(20, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animateStep();
            }
        });
    }

    private void animateStep() {
        currentStep++;
        
        // Calculate progress (0.0 to 1.0)
        float progress = Math.min(1.0f, (float)currentStep / totalSteps);
        
        // Apply easing function for smoother movement
        progress = easeOutQuad(progress);
        
        // Calculate new position based on direction with strict bounds
        int newX = box.getX();
        int newY = box.getY();
        
        switch(direction) {
            case UP:
            case DOWN:
                // Vertical movement - only change Y
                newY = (int)(box.getY() + (targetY - box.getY()) * progress);
                newX = box.getX(); // Keep X constant
                break;
            case LEFT:
            case RIGHT:
                // Horizontal movement - only change X 
                newX = (int)(box.getX() + (targetX - box.getX()) * progress);
                newY = box.getY(); // Keep Y constant
                break;
        }
        
        // Apply strict bounds
        newX = Math.max(0, Math.min(targetX, newX));
        newY = Math.max(0, Math.min(targetY, newY));
        
        // Debug log positions
        System.out.printf("Animating %s: step %d/%d - from (%d,%d) to (%d,%d) now at (%d,%d)\n",
            direction, currentStep, totalSteps, 
            box.getX(), box.getY(), targetX, targetY, newX, newY);
        
        // Set new position with bounds checking
        if (newX >= 0 && newY >= 0) {
            box.setLocation(newX, newY);
            box.repaint();
            box.getParent().repaint();
        }
        
        // Check if animation complete
        if (currentStep >= totalSteps) {
            timer.stop();
            // Snap to final position to ensure precision
            box.setLocation(targetX, targetY);
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    private float easeOutQuad(float t) {
        return t * (2 - t);
    }

    public void start() {
        box.setAnimating(true);
        timer.start();
    }

    public void stop() {
        timer.stop();
        box.setAnimating(false);
    }
}
