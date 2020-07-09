package at.ac.tuwien.student.e01526624.backend.resources;

import at.ac.tuwien.student.e01526624.backend.domain.ExcelWorkbook;
import at.ac.tuwien.student.e01526624.backend.domain.assertion.Assertion;
import at.ac.tuwien.student.e01526624.backend.domain.excel.Cell;
import at.ac.tuwien.student.e01526624.backend.resources.dto.AssertionDto;
import at.ac.tuwien.student.e01526624.backend.resources.dto.CellDto;
import at.ac.tuwien.student.e01526624.backend.resources.dto.CellFindRequestDto;
import at.ac.tuwien.student.e01526624.backend.resources.dto.WorkbookDto;
import at.ac.tuwien.student.e01526624.backend.service.ExcelFileReader;
import at.ac.tuwien.student.e01526624.backend.service.OntologyService;
import at.ac.tuwien.student.e01526624.backend.service.exceptions.CellNotFoundException;
import com.sun.corba.se.spi.orbutil.threadpool.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/")
public class MainResource {

    @Autowired
    OntologyService ontologyService;

    @Autowired
    ExcelFileReader excelFileReader;

    @RequestMapping(value = "/getCell", method = RequestMethod.POST)
    public ResponseEntity<CellDto> getCell(@RequestBody CellFindRequestDto find) {
        Cell cell = ontologyService.getCell(find.getWorkbookHash(), find.getWorksheet(), find.getRow(), find.getColumn(), find.isResolveTransitiveDependencies());
        return ResponseEntity.ok(CellDto.fromDomain(cell));
    }

    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public ResponseEntity<WorkbookDto> uploadExcelFile(@RequestPart(name = "file") MultipartFile file) throws Exception {
        ExcelWorkbook wb = excelFileReader.parseExcelFile(file);
        this.ontologyService.addWorkbook(wb);
        return ResponseEntity.ok(WorkbookDto.fromDomain(wb));
    }

    @RequestMapping(value = "/getAllWorkbooks", method = RequestMethod.POST)
    public ResponseEntity<List<WorkbookDto>> getAllWorkbooks() {
        Collection<ExcelWorkbook> workbooks = ontologyService.getAllWorkbooks();
        List<WorkbookDto> dtos = new ArrayList<>();
        workbooks.forEach(workbook -> dtos.add(WorkbookDto.fromDomain(workbook)));
        return ResponseEntity.ok(dtos);
    }

    @RequestMapping(value = "/checkAssertion", method = RequestMethod.POST)
    public ResponseEntity<List<CellDto>> checkAssertion(@RequestBody AssertionDto dto) {
        List<CellDto> violatingCellDtos = new ArrayList<>();
        Assertion assertion = AssertionDto.toDomain(dto);
        Collection<Cell> vCells = ontologyService.validateAssertion(assertion);
        for (Cell vc : vCells) {
            violatingCellDtos.add(CellDto.fromDomain(vc));
        }
        return ResponseEntity.ok(violatingCellDtos);
    }
}
