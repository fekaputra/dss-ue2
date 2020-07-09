package at.ac.tuwien.student.e01526624.backend.domain.excel;

import java.util.ArrayList;
import java.util.List;

public class CalculatedCell extends Cell {
    private String formula;
    private List<Cell> dependentOn = new ArrayList<>();

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public void addDependentCell(Cell cell) {
        this.dependentOn.add(cell);
    }

    public List<Cell> getDependentOn() {
        return dependentOn;
    }

    public void resetDependenOn() {
        this.dependentOn = new ArrayList<>();
    }
}
