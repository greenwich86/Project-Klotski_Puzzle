package model;

/**
 * This class represents the Klotski game board with different block types:
 * 0 = Empty
 * 1 = Cao Cao (2x2)
 * 2 = Guan Yu (2x1) 
 * 3 = General (1x2)
 * 4 = Soldier (1x1)
 */
public class MapModel {
    public static final int CAO_CAO = 1;
    public static final int GUAN_YU = 2;
    public static final int GENERAL = 3;
    public static final int SOLDIER = 4;
    
    int[][] matrix;
    public static final int[][][] LEVELS = {
        // Level 1 - Original configuration
        {
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
            {SOLDIER, 0, 0, SOLDIER},
            {GENERAL, SOLDIER, SOLDIER, GENERAL},
            {GENERAL, GUAN_YU, GUAN_YU, GENERAL}
        },
        // Level 2 - More challenging configuration
        {
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
            {SOLDIER, 0, 0, SOLDIER},
            {GENERAL, SOLDIER, SOLDIER, GENERAL},
            {GENERAL, GUAN_YU, GUAN_YU, GENERAL}
        },
        // Level 3 - Different block arrangement
        {
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
            {GENERAL, CAO_CAO, CAO_CAO, GENERAL},
            {SOLDIER, 0, 0, SOLDIER},
            {GENERAL, SOLDIER, SOLDIER, GENERAL},
            {GENERAL, GUAN_YU, GUAN_YU, GENERAL}
        }
    };

    public MapModel() {
        this(0); // Default to first level
    }

    public MapModel(int level) {
        if (level < 0 || level >= LEVELS.length) {
            level = 0;
        }
        this.matrix = new int[5][4];
        for (int i = 0; i < 5; i++) {
            System.arraycopy(LEVELS[level][i], 0, this.matrix[i], 0, 4);
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
