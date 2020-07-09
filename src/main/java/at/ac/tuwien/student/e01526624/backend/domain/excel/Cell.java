package at.ac.tuwien.student.e01526624.backend.domain.excel;

public abstract class Cell {
    private int row; // We use the 0 based index here, not the excel representation
    private int column; // here too the 0 based index is used
    private String worksheet;
    private String stringValue;
    private double numericValue;
    private boolean booleanValue;
    private ValueType valueType;
    private boolean fulfillsConstraint;

    public boolean isFulfillsConstraint() {
        return fulfillsConstraint;
    }

    public void setFulfillsConstraint(boolean fulfillsConstraint) {
        this.fulfillsConstraint = fulfillsConstraint;
    }

    public String getCellLocation() {
        return this.worksheet + "!" + this.column + ":" + this.row;
    }

    private boolean isInRow(int row) {
        return row == this.row;
    }

    private boolean isInColumn(int column) {
        return numericColumnIndexToTextualColumnIndex(column).equals(this.column);
    }

    private boolean isInWorksheet(String worksheet) {
        return this.worksheet.equals(worksheet);
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    //    public void setColumn(int column) {
//        this.column = numericColumnIndexToTextualColumnIndex(column);
//    }



    public String getWorksheet() {
        return worksheet;
    }

    public void setWorksheet(String worksheet) {
        this.worksheet = worksheet;
    }

    public String getStringValue() throws IncorrectValueTypeException {
        if (this.valueType != ValueType.STRING) {
            throw new IncorrectValueTypeException();
        }
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
        this.valueType = ValueType.STRING;
    }

    public double getNumericValue() throws IncorrectValueTypeException {
        if (this.valueType != ValueType.NUMERIC) {
            throw new IncorrectValueTypeException();
        }
        return numericValue;
    }

    public void setNumericValue(double numericValue) {
        this.numericValue = numericValue;
        this.valueType = ValueType.NUMERIC;
    }

    public boolean getBooleanValue() throws IncorrectValueTypeException {
        if (this.valueType != ValueType.BOOLEAN) {
            throw new IncorrectValueTypeException();
        }
        return booleanValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
        this.valueType = ValueType.BOOLEAN;
    }

    public ValueType getValueType() {
        return this.valueType;
    }

    public static String numericColumnIndexToTextualColumnIndex(int index) {
        String[] indexChars = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };
        int currentFactor = 1;
        String textualIndex = indexChars[index % 26];
        while (index >= Math.pow(26, currentFactor)) {
            textualIndex = indexChars[((index / (int)(Math.pow(26, currentFactor)))-1) % 26] + textualIndex;
            currentFactor++;
        }
        return textualIndex;
    }
}
