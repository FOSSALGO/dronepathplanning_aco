package drone.dronepathplanning_aco;

/**
 * Immutable integer grid coordinate (row, column).
 */
public record Point(int row, int col) {

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Point(int row1, int col1))) return false;
        return row == row1 && col == col1;
    }

}
