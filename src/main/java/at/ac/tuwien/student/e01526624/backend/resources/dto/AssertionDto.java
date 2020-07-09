package at.ac.tuwien.student.e01526624.backend.resources.dto;

import at.ac.tuwien.student.e01526624.backend.domain.assertion.Assertion;
import at.ac.tuwien.student.e01526624.backend.domain.assertion.AssertionTypes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssertionDto {
    private int rowStart;
    private int rowEnd;
    private int columnStart;
    private int columnEnd;
    private String worksheet;
    private String workbookHash;
    private String strVal;
    private double numVal;
    private String operator;

    public AssertionDto(int rowStart, int rowEnd, int columnStart, int columnEnd, String worksheet, String workbookHash, String strVal, double numVal, String operator) {
        this.rowStart = rowStart;
        this.rowEnd = rowEnd;
        this.columnStart = columnStart;
        this.columnEnd = columnEnd;
        this.worksheet = worksheet;
        this.workbookHash = workbookHash;
        this.strVal = strVal;
        this.numVal = numVal;
        this.operator = operator;
    }

    public AssertionDto() {
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

    public String getWorksheet() {
        return worksheet;
    }

    public void setWorksheet(String worksheet) {
        this.worksheet = worksheet;
    }

    public String getWorkbookHash() {
        return workbookHash;
    }

    public void setWorkbookHash(String workbookHash) {
        this.workbookHash = workbookHash;
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

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public static Assertion toDomain(AssertionDto dto) {
        Assertion a = new Assertion();
        a.setRowStart(dto.getRowStart());
        a.setRowEnd(dto.getRowEnd());
        a.setColumnStart(dto.getColumnStart());
        a.setColumnEnd(dto.getColumnEnd());
        a.setWorksheet(dto.getWorksheet());
        a.setWorkbookHash(dto.getWorkbookHash());
        switch (dto.getOperator()) {
            case "eq":
                a.setNumVal(dto.getNumVal());
                a.setType(AssertionTypes.EQ);
                break;
            case "gt":
                a.setNumVal(dto.getNumVal());
                a.setType(AssertionTypes.GT);
                break;
            case "gte":
                a.setNumVal(dto.getNumVal());
                a.setType(AssertionTypes.GTE);
                break;
            case "lt":
                a.setNumVal(dto.getNumVal());
                a.setType(AssertionTypes.LT);
                break;
            case "lte":
                a.setNumVal(dto.getNumVal());
                a.setType(AssertionTypes.LTE);
                break;
            case "str_eq":
                a.setStrVal(dto.getStrVal());
                a.setType(AssertionTypes.STR_EQ);
                break;
        }
        return a;
    }

    private static boolean isDoubleParseable(String s) {
        String rgx = "^-?\\d+(?:\\.\\d+)?$";
        Pattern p = Pattern.compile(rgx);
        Matcher m = p.matcher(s);
        return m.find();
    }
}
