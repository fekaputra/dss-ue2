package at.ac.tuwien.student.e01526624.backend.resources.dto;

import at.ac.tuwien.student.e01526624.backend.domain.ExcelWorkbook;

import java.util.List;

public class WorkbookDto {
    private String filename;
    private String filehash;
    private List<String> worksheets;

    public WorkbookDto() {
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilehash() {
        return filehash;
    }

    public void setFilehash(String filehash) {
        this.filehash = filehash;
    }

    public List<String> getWorksheets() {
        return worksheets;
    }

    public void setWorksheets(List<String> worksheets) {
        this.worksheets = worksheets;
    }

    public static WorkbookDto fromDomain(ExcelWorkbook workbook) {
        WorkbookDto dto = new WorkbookDto();
        dto.setFilehash(workbook.getSha1());
        dto.setFilename(workbook.getFilename());
        return dto;
    }
}
