package at.ac.tuwien.student.e01526624.backend.resources.dto;

public class CellFindRequestDto {
    private int row;
    private int column;
    private String worksheet;
    private String workbookHash;
    private boolean resolveTransitiveDependencies;

    public CellFindRequestDto(int row, int column, String worksheet, String workbookHash, boolean resolveTransitiveDependencies) {
        this.row = row;
        this.column = column;
        this.worksheet = worksheet;
        this.workbookHash = workbookHash;
        this.resolveTransitiveDependencies = resolveTransitiveDependencies;
    }

    public CellFindRequestDto() {
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

    public boolean isResolveTransitiveDependencies() {
        return resolveTransitiveDependencies;
    }

    public void setResolveTransitiveDependencies(boolean resolveTransitiveDependencies) {
        this.resolveTransitiveDependencies = resolveTransitiveDependencies;
    }
}
