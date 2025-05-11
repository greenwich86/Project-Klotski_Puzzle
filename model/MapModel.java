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

    public MapModel() {
        // Initialize strict 4x5 Klotski board (4 columns x 5 rows)
        this.matrix = new int[5][4];
        // Adjusted Klotski layout for 4x5
        // Cao Cao (2x2) at top center
        matrix[0][1] = CAO_CAO; matrix[0][2] = CAO_CAO;
        matrix[1][1] = CAO_CAO; matrix[1][2] = CAO_CAO;
        
        // Guan Yu (2x1) horizontal
        matrix[4][1] = GUAN_YU; matrix[4][2] = GUAN_YU;
        
        // Generals (1x2) vertical
        matrix[0][0] = GENERAL; matrix[1][0] = GENERAL;
        matrix[0][3] = GENERAL; matrix[1][3] = GENERAL;
        matrix[3][0] = GENERAL; matrix[4][0] = GENERAL;
        matrix[3][3] = GENERAL; matrix[4][3] = GENERAL;
        
        // Soldiers (1x1)
        matrix[2][0] = SOLDIER;
        matrix[2][3] = SOLDIER;
        matrix[3][1] = SOLDIER;
        matrix[3][2] = SOLDIER;
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
