package at.ac.tuwien.student.e01526624.backend.service;

import at.ac.tuwien.student.e01526624.backend.domain.ExcelWorkbook;
import at.ac.tuwien.student.e01526624.backend.domain.assertion.Assertion;
import at.ac.tuwien.student.e01526624.backend.domain.assertion.AssertionTypes;
import at.ac.tuwien.student.e01526624.backend.domain.excel.CalculatedCell;
import at.ac.tuwien.student.e01526624.backend.domain.excel.PureCell;
import org.apache.jena.query.Dataset;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


@Component
public class ExcelFileReader implements CommandLineRunner {

    private OntologyService ontologyService;

    @Autowired
    public ExcelFileReader(OntologyService ontologyService) {
        this.ontologyService = ontologyService;
    }

    public ExcelWorkbook parseExcelFile(File file) throws Exception {
        ExcelWorkbook parsedWorkbook = new ExcelWorkbook();
        parsedWorkbook.setFilename(file.getName());
        parsedWorkbook.setSha1(calcSHA1(file));
        XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
        Collection<String> sheetNames = getSheetNamesFromWorkbook(workbook);

        for (String sheetName : sheetNames) {
            parseSheet(parsedWorkbook, workbook.getSheetAt(workbook.getSheetIndex(sheetName)));
        }
        parsedWorkbook.linkCellDependencies();
        return parsedWorkbook;
    }

    public ExcelWorkbook parseExcelFile(MultipartFile file) throws Exception {
        ExcelWorkbook parsedWorkbook = new ExcelWorkbook();
        parsedWorkbook.setFilename(file.getOriginalFilename());
        parsedWorkbook.setSha1(calcSHA1(file));
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        Collection<String> sheetNames = getSheetNamesFromWorkbook(workbook);

        for (String sheetName : sheetNames) {
            parseSheet(parsedWorkbook, workbook.getSheetAt(workbook.getSheetIndex(sheetName)));
        }
        parsedWorkbook.linkCellDependencies();
        return parsedWorkbook;
    }

    @Override
    public void run(String... args) throws Exception {
//        this.test();
    }

    private void test() {
//        File f = this.getComplexDemoFile();
//        File f = this.getEdgeCaseFile();
        File f = this.getSimpleDemoFile();
        try {
            ExcelWorkbook parsedWb =  parseExcelFile(f);
            ExcelWorkbook edgeCaseWb = parseExcelFile(this.getEdgeCaseFile());
            ExcelWorkbook complexFile = parseExcelFile(this.getComplexDemoFile());

//            boolean asdf = ontologyService.workBookAlreadyExists(parsedWb.getSha1());
//            ontologyService.addWorkbook(parsedWb);
            Collection<ExcelWorkbook> wbs = ontologyService.getAllWorkbooks();
            if (!ontologyService.workBookAlreadyExists(parsedWb.getSha1())) {
                ontologyService.addWorkbook(parsedWb);
            }
            if (!ontologyService.workBookAlreadyExists(edgeCaseWb.getSha1())) {
                ontologyService.addWorkbook(edgeCaseWb);
            }
            if (!ontologyService.workBookAlreadyExists(complexFile.getSha1())) {
                ontologyService.addWorkbook(complexFile);
            }
            at.ac.tuwien.student.e01526624.backend.domain.excel.Cell c = ontologyService.getCell(parsedWb.getSha1(), "Sheet1", 7, 6, false);
//            at.ac.tuwien.student.e01526624.backend.domain.excel.Cell c2 = ontologyService.getCell(complexFile.getSha1(), "Results_ohne Massnahmen", 7, 6, false);
            Assertion assertion = new Assertion();
            assertion.setWorkbookHash(parsedWb.getSha1());
            assertion.setWorksheet(c.getWorksheet());
            assertion.setColumnStart(0);
            assertion.setColumnEnd(2);
            assertion.setRowStart(1);
            assertion.setRowEnd(17);
            assertion.setType(AssertionTypes.GTE);
            assertion.setNumVal(-100);
            Collection<at.ac.tuwien.student.e01526624.backend.domain.excel.Cell> violatingCells = ontologyService.validateAssertion(assertion);
            boolean asdf = ontologyService.workBookAlreadyExists(parsedWb.getSha1());
            System.out.println("DONE");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void parseSheet(ExcelWorkbook parsedWb, XSSFSheet sheet) {
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Iterator<Cell> cellIterator = row.cellIterator();
            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                if (cell.getCellType().equals(CellType.FORMULA)) {
                    parsedWb.addCell(createCalculatedCell(cell));
                } else {
                    parsedWb.addCell(createPureCell(cell));
                }
            }
        }
    }

    private PureCell createPureCell(Cell base) {
        PureCell cell = new PureCell();
        cell.setRow(base.getRowIndex());
        cell.setColumn(base.getColumnIndex());
        cell.setWorksheet(base.getSheet().getSheetName());
        switch (base.getCellType()) {
            case NUMERIC:
                cell.setNumericValue(base.getNumericCellValue());
                break;
            case BOOLEAN:
                cell.setBooleanValue(base.getBooleanCellValue());
                break;
            case STRING:
                cell.setStringValue(base.getStringCellValue());
                break;
            default:
                break;
        }
        return cell;
    }

    private CalculatedCell createCalculatedCell(Cell base) {
        CalculatedCell cell = new CalculatedCell();
        cell.setRow(base.getRowIndex());
        cell.setColumn(base.getColumnIndex());
        cell.setWorksheet(base.getSheet().getSheetName());
        switch (base.getCachedFormulaResultType()) {
            case NUMERIC:
                cell.setNumericValue(base.getNumericCellValue());
                break;
            case BOOLEAN:
                cell.setBooleanValue(base.getBooleanCellValue());
                break;
            case STRING:
                cell.setStringValue(base.getStringCellValue());
                break;
            default:
                break;
        }
        cell.setFormula(base.getCellFormula());
        return cell;
    }

    private Collection<String> getSheetNamesFromWorkbook(XSSFWorkbook wb) {
        List<String> sheets = new ArrayList<>();
        int numberOfSheets = wb.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            sheets.add(wb.getSheetName(i));
        }
        return sheets;
    }


    private File getSimpleDemoFile() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        return new File(classLoader.getResource("xlsx/Simple.xlsx").getFile());
    }

    private File getComplexDemoFile() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        return new File(classLoader.getResource("xlsx/Bodenabtrag.xlsx").getFile());
    }

    private File getEdgeCaseFile() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        return new File(classLoader.getResource("xlsx/EdgeCase.xlsx").getFile());
    }


    // Not mine. Taken from: https://stackoverflow.com/questions/6293713/java-how-to-create-sha-1-for-a-file
    public static String calcSHA1(File file) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        try (InputStream input = new FileInputStream(file)) {

            byte[] buffer = new byte[8192];
            int len = input.read(buffer);

            while (len != -1) {
                sha1.update(buffer, 0, len);
                len = input.read(buffer);
            }

            return new HexBinaryAdapter().marshal(sha1.digest());
        }
    }
    public static String calcSHA1(MultipartFile file) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

        try (InputStream input = file.getInputStream()) {

            byte[] buffer = new byte[8192];
            int len = input.read(buffer);

            while (len != -1) {
                sha1.update(buffer, 0, len);
                len = input.read(buffer);
            }

            return new HexBinaryAdapter().marshal(sha1.digest());
        }
    }
}
