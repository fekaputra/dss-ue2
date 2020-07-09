package at.ac.tuwien.student.e01526624.backend.domain;


import at.ac.tuwien.student.e01526624.backend.domain.excel.AddressRange;
import at.ac.tuwien.student.e01526624.backend.domain.excel.CalculatedCell;
import at.ac.tuwien.student.e01526624.backend.domain.excel.Cell;
import at.ac.tuwien.student.e01526624.backend.domain.excel.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ExcelWorkbook {
    private Map<String, Cell> cells = new HashMap<>();
    private String filename;
    private String sha1;

    public void addCell(Cell cell) {
        cells.put(cell.getCellLocation(), cell);
    }

    public Collection<Cell> getCells() {
        return cells.values();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public void linkCellDependencies() {
        this.cells.values().forEach(cell -> {
            if (cell instanceof CalculatedCell) {
                linkCell((CalculatedCell) cell);
            }
        });
    }

    private void linkCell(CalculatedCell cell) {
        String formula = cell.getFormula();
        Collection<String> dependentAddressRanges = getDependentAddressRanges(formula);
        dependentAddressRanges.forEach(range -> addDependentCells(range, cell));
    }

    private void addDependentCells(String addressRange, CalculatedCell baseCell) {
        String worksheetName = getWorksheetNameFromRangeIfExists(addressRange);
        if (worksheetName == null) {
            worksheetName = baseCell.getWorksheet();
        }
        AddressRange range = getAddressRangeFromString(addressRange);
        for (int i = range.getColStart(); i <= range.getColEnd(); i++) {
            for (int j = range.getRowStart(); j <= range.getRowEnd(); j++) {
                Cell depCell = this.cells.get(worksheetName + "!" + i + ":" + j);
                if (depCell != null) {
                    baseCell.addDependentCell(depCell);
                }
            }
        }
    }

    private Collection<String> getDependentAddressRanges(String formula) {
        Collection<String> resultList = new ArrayList<>();
        String pattern = "((?:(?:'.+'!)|(?:\\w+!))?[A-Z]+[0-9]+(?::[A-Z]+[0-9]+)?)";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(formula);
        while (m.find()) {
            resultList.add(m.group(0));
        }
        return resultList;
    }

    private String getWorksheetNameFromRangeIfExists(String cellRange) {
        String regex = "(?:((?:'.+'!)|(?:\\w+!))?(?:[A-Z]+[0-9]+){1}(?::[A-Z]+[0-9]+)?)";
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(cellRange);
        String worksheetName = "";
        if (m.find()) {
            if (m.group(1) == null) {
                return null;
            }
            worksheetName = m.group(1);
        } else {
            return null;
        }
        if (worksheetName.startsWith("'")) {
            return worksheetName.substring(1, worksheetName.length() - 2);
        }
        return worksheetName.substring(0, worksheetName.length() - 1);
    }

    private AddressRange getAddressRangeFromString(String cellRange) {
        int colStart, colEnd, rowStart, rowEnd;
        colStart = getColStart(cellRange);
        rowStart = getRowStart(cellRange);
        colEnd = getColEnd(cellRange);
        rowEnd = getRowEnd(cellRange);
        if (colEnd == -1 || rowEnd == -1) {
            colEnd = colStart;
            rowEnd = rowStart;
        }
        return new AddressRange(colStart, colEnd, rowStart, rowEnd);
    }

    private int getColStart(String cellRange) {
        String rangeStartRegex = "(?:(?:(?:'.+'!)|(?:\\w+!))?(?:([A-Z]+)[0-9]+){1}(?::[A-Z]+[0-9]+)?)";
        Pattern rangePattern = Pattern.compile(rangeStartRegex);
        Matcher rangeMatcher = rangePattern.matcher(cellRange);
        String colStartString = "";
        if (rangeMatcher.find()) {
            colStartString = rangeMatcher.group(1);
            return Util.columnIndexFromColumnString(colStartString);
        }
        return -1;
    }

    private int getRowStart(String cellRange) {
        String rowStartRegex = "(?:(?:(?:'.+'!)|(?:\\w+!))?(?:[A-Z]+([0-9]+)){1}(?::[A-Z]+[0-9]+)?)";
        Pattern rowStartPattern = Pattern.compile(rowStartRegex);
        Matcher rowMatcher = rowStartPattern.matcher(cellRange);
        if (rowMatcher.find()) {
            String rowString = rowMatcher.group(1);
            return Integer.parseInt(rowString) - 1;
        }
        return -1;
    }

    private int getColEnd(String cellRange) {
        String rangeStartRegex = "(?:(?:(?:'.+'!)|(?:\\w+!))?(?:[A-Z]+[0-9]+){1}(?::([A-Z]+)[0-9]+)?)";
        Pattern rangePattern = Pattern.compile(rangeStartRegex);
        Matcher rangeMatcher = rangePattern.matcher(cellRange);
        String colStartString = "";
        if (rangeMatcher.find() && rangeMatcher.group(1) != null) {
            colStartString = rangeMatcher.group(1);
            return Util.columnIndexFromColumnString(colStartString);
        }
        return -1;
    }

    private int getRowEnd(String cellRange) {
        String rowEndRegex = "(?:(?:(?:'.+'!)|(?:\\w+!))?(?:[A-Z]+[0-9]+){1}(?::[A-Z]+([0-9]+))?)";
        Pattern rowEndPattern = Pattern.compile(rowEndRegex);
        Matcher rowMatcher = rowEndPattern.matcher(cellRange);
        if (rowMatcher.find() && rowMatcher.group(1) != null) {
            String rowString = rowMatcher.group(1);
            return Integer.parseInt(rowString) - 1;
        }
        return -1;
    }


}
