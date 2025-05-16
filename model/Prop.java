package model;

/**
 * Represents a prop that can be used in the game
 * Props are special items that provide game assistance
 * Different prop types are available based on difficulty level
 */
public class Prop {
    
    public enum PropType {
        HINT("Hint", "Shows the next best move"),
        TIME_BONUS("Time Bonus", "Adds 30 seconds to the timer"),
        OBSTACLE_REMOVER("Obstacle Remover", "Temporarily removes an obstacle");
        
        private final String name;
        private final String description;
        
        PropType(String name, String description) {
            this.name = name;
            this.description = description;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    private PropType type;
    private int count;
    
    public Prop(PropType type, int count) {
        this.type = type;
        this.count = count;
    }
    
    public PropType getType() {
        return type;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public boolean isAvailable() {
        return count > 0;
    }
    
    public boolean use() {
        if (count > 0) {
            count--;
            return true;
        }
        return false;
    }
    
    public void add(int amount) {
        count += amount;
    }
    
    @Override
    public String toString() {
        return type.getName() + " (" + count + ")";
    }
}
