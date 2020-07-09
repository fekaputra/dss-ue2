package at.ac.tuwien.student.e01526624.backend.domain.excel;

public class AddressRange {
    private int colStart;
    private int colEnd;
    private int rowStart;
    private int rowEnd;

    public AddressRange(int colStart, int colEnd, int rowStart, int rowEnd) {
        this.colStart = colStart;
        this.colEnd = colEnd;
        this.rowStart = rowStart;
        this.rowEnd = rowEnd;
    }

    public int getColStart() {
        return colStart;
    }

    public int getColEnd() {
        return colEnd;
    }

    public int getRowStart() {
        return rowStart;
    }

    public int getRowEnd() {
        return rowEnd;
    }
}
