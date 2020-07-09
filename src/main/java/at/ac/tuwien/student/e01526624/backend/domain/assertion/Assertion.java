package at.ac.tuwien.student.e01526624.backend.domain.assertion;

public class Assertion {
    private String workbookHash;
    private String worksheet;
    private int rowStart;
    private int rowEnd;
    private int columnStart;
    private int columnEnd;
    private AssertionTypes type;
    private String strVal;
    private double numVal;

    public Assertion(String workbookHash, String worksheet, int rowStart, int rowEnd, int columnStart, int columnEnd, AssertionTypes type, String strVal, double numVal) {
        this.workbookHash = workbookHash;
        this.worksheet = worksheet;
        this.rowStart = rowStart;
        this.rowEnd = rowEnd;
        this.columnStart = columnStart;
        this.columnEnd = columnEnd;
        this.type = type;
        this.strVal = strVal;
        this.numVal = numVal;
    }

    public Assertion() {
    }

    public String getWorkbookHash() {
        return workbookHash;
    }

    public void setWorkbookHash(String workbookHash) {
        this.workbookHash = workbookHash;
    }

    public String getWorksheet() {
        return worksheet;
    }

    public void setWorksheet(String worksheet) {
        this.worksheet = worksheet;
    }

    public int getRowStart() {
        return rowStart;
    }

    public void setRowStart(int rowStart) {
        this.rowStart = rowStart;
    }

    public int getRowEnd() {
        return rowEnd;
    }

    public void setRowEnd(int rowEnd) {
        this.rowEnd = rowEnd;
    }

    public int getColumnStart() {
        return columnStart;
    }

    public void setColumnStart(int columnStart) {
        this.columnStart = columnStart;
    }

    public int getColumnEnd() {
        return columnEnd;
    }

    public void setColumnEnd(int columnEnd) {
        this.columnEnd = columnEnd;
    }

    public AssertionTypes getType() {
        return type;
    }

    public void setType(AssertionTypes type) {
        this.type = type;
    }

    public String getStrVal() {
        return strVal;
    }

    public void setStrVal(String strVal) {
        this.strVal = strVal;
    }

    public double getNumVal() {
        return numVal;
    }

    public void setNumVal(double numVal) {
        this.numVal = numVal;
    }
}
