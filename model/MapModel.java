package model;

/**
 * This class represents the Klotski game board with different block types:
 * 0 = Empty
 * 1 = Cao Cao (2x2)
 * 2 = Guan Yu (2x1) 
 * 3 = General (1x2)
 * 4 = Soldier (1x1)
 * 5 = Zhou Yu (1x3)
 * 9 = Blocked (immovable)
 * 10 = Military Camp (soldiers cannot step on)
 */
public class MapModel {
    public static final int CAO_CAO = 1;
    public static final int GUAN_YU = 2;
    public static final int GENERAL = 3;
    public static final int SOLDIER = 4;
    public static final int ZHOU_YU = 5; // 1x3 horizontal block
    public static final int BLOCKED = 9; // Immovable obstacle
    public static final int MILITARY_CAMP = 10; // Military camp - soldiers cannot step on
    
    // Difficulty level names
    public static final String[] LEVEL_NAMES = {
        "Easy", "Hard", "Expert", "Master"
    };
    
    // Props availability per level
    public static final boolean[] LEVEL_PROPS_ALLOWED = {
        false, // Easy - no props
        true,  // Hard - props allowed
        true,  // Expert - props allowed
        false  // Master - no props
    };
    
    // Time attack enforced settings
    public static final boolean[] LEVEL_TIME_ATTACK_ENFORCED = {
        false, // Easy - optional time attack
        false, // Hard - optional time attack
        false, // Expert - optional time attack
        true   // Master - enforced 5 min time attack
    };
    
    // Default time for enforced time attack levels (in minutes)
    public static final int DEFAULT_MASTER_TIME_LIMIT = 5;
    
    int[][] matrix;
    public static final int[][][] LEVELS = {
        // Level 0 - Easy (4x5) Classic configuration
        {
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
            {SOLDIER, 0, 0, SOLDIER},
            {GENERAL, SOLDIER, SOLDIER, GENERAL},
            {GENERAL, GUAN_YU, GUAN_YU, GENERAL}
        },
        // Level 1 - Hard (5x6) with Cao Cao at top middle
        {
            {0, BLOCKED, CAO_CAO, CAO_CAO, BLOCKED},
            {0, BLOCKED, CAO_CAO, CAO_CAO, BLOCKED},
            {SOLDIER, SOLDIER, 0, 0, SOLDIER},
            {GENERAL, GENERAL, SOLDIER, SOLDIER, GENERAL},
            {0, GENERAL, GUAN_YU, GUAN_YU, GENERAL},
            {0, SOLDIER, 0, BLOCKED, SOLDIER}
        },
        // Level 2 - Expert (6x7) with more blocks
        {
            {0, 0, CAO_CAO, CAO_CAO, 0, 0},
            {0, 0, CAO_CAO, CAO_CAO, 0, 0},
            {SOLDIER, BLOCKED, 0, 0, BLOCKED, SOLDIER},
            {GENERAL, SOLDIER, SOLDIER, SOLDIER, SOLDIER, GENERAL},
            {GENERAL, GUAN_YU, GUAN_YU, GUAN_YU, GUAN_YU, GENERAL},
            {SOLDIER, 0, 0, BLOCKED, 0, SOLDIER},
            {0, 0, ZHOU_YU, ZHOU_YU, ZHOU_YU, 0}
        },
        // Level 3 - Master (6x7) with Military Camp obstacles
        {
            {0, MILITARY_CAMP, CAO_CAO, CAO_CAO, MILITARY_CAMP, 0},
            {0, 0, CAO_CAO, CAO_CAO, 0, 0},
            {SOLDIER, BLOCKED, MILITARY_CAMP, MILITARY_CAMP, BLOCKED, SOLDIER},
            {GENERAL, SOLDIER, SOLDIER, SOLDIER, SOLDIER, GENERAL},
            {GENERAL, GUAN_YU, GUAN_YU, GUAN_YU, GUAN_YU, GENERAL},
            {SOLDIER, MILITARY_CAMP, 0, BLOCKED, MILITARY_CAMP, SOLDIER},
            {0, 0, ZHOU_YU, ZHOU_YU, ZHOU_YU, 0}
        }
    };

    public MapModel() {
        this(0); // Default to first level
    }

    public MapModel(int level) {
        if (level < 0 || level >= LEVELS.length) {
            level = 0;
        }
        int rows = LEVELS[level].length;
        int cols = LEVELS[level][0].length;
        this.matrix = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(LEVELS[level][i], 0, this.matrix[i], 0, cols);
        }
    }

    public MapModel(int[][] matrix) {
        this.matrix = matrix;
    }

    public int getWidth() {
        return this.matrix[0].length;
    }

    public int getHeight() {
        return this.matrix.length;
    }

    public int getId(int row, int col) {
        return matrix[row][col];
    }

    public int[][] getMatrix() {
        return matrix;
    }

    public boolean checkInWidthSize(int col) {
        return col >= 0 && col < matrix[0].length;
    }

    public boolean checkInHeightSize(int row) {
        return row >= 0 && row < matrix.length;
    }

    public int[][] copyMatrix() {
        int[][] copy = new int[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, copy[i], 0, matrix[i].length);
        }
        return copy;
    }
}
