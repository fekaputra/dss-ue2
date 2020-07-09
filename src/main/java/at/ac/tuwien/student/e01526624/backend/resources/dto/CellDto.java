package at.ac.tuwien.student.e01526624.backend.resources.dto;

import at.ac.tuwien.student.e01526624.backend.domain.excel.CalculatedCell;
import at.ac.tuwien.student.e01526624.backend.domain.excel.Cell;
import at.ac.tuwien.student.e01526624.backend.domain.excel.IncorrectValueTypeException;

import java.util.ArrayList;
import java.util.List;

public class CellDto {
    private int row;
    private int column;
    private String worksheet;
    private String value;
    private String formula;
    private List<CellDto> dependentOn;

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

    public String getWorksheet() {
        return worksheet;
    }

    public void setWorksheet(String worksheet) {
        this.worksheet = worksheet;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public List<CellDto> getDependentOn() {
        return dependentOn;
    }

    public void setDependentOn(List<CellDto> dependentOn) {
        this.dependentOn = dependentOn;
    }

    public static CellDto fromDomain(Cell cell) {
        CellDto dto = new CellDto();
        dto.setRow(cell.getRow());
        dto.setColumn(cell.getColumn());
        dto.setWorksheet(cell.getWorksheet());
        if (cell instanceof CalculatedCell) {
            CalculatedCell ccell = (CalculatedCell) cell;
            dto.setFormula(ccell.getFormula());
            List<CellDto> dependentCells = new ArrayList<>();
            for (Cell dCell : ccell.getDependentOn()) {
                dependentCells.add(fromDomainWithoutDependencies(dCell));
            }
            dto.setDependentOn(dependentCells);
        }
        try {
            if (cell.getValueType() != null) {
                switch (cell.getValueType()) {
                    case STRING:
                        dto.setValue(cell.getStringValue());
                        break;
                    case BOOLEAN:
                        dto.setValue(Boolean.toString(cell.getBooleanValue()));
                        break;
                    case NUMERIC:
                        dto.setValue(Double.toString(cell.getNumericValue()));
                        break;
                }
            }
        } catch (IncorrectValueTypeException exception) {

        }
        return dto;
    }

    private static CellDto fromDomainWithoutDependencies(Cell cell) {
        CellDto dto = new CellDto();
        dto.setRow(cell.getRow());
        dto.setColumn(cell.getColumn());
        dto.setWorksheet(cell.getWorksheet());
        if (cell instanceof CalculatedCell) {
            CalculatedCell ccell = (CalculatedCell) cell;
            dto.setFormula(ccell.getFormula());
        }
        try {
            if (cell.getValueType() != null) {
                switch (cell.getValueType()) {
                    case STRING:
                        dto.setValue(cell.getStringValue());
                        break;
                    case BOOLEAN:
                        dto.setValue(Boolean.toString(cell.getBooleanValue()));
                        break;
                    case NUMERIC:
                        dto.setValue(Double.toString(cell.getNumericValue()));
                        break;
                }
            }
        } catch (IncorrectValueTypeException exception) {

        }
        return dto;
    }
}
