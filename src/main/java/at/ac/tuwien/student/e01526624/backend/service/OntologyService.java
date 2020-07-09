package at.ac.tuwien.student.e01526624.backend.service;

import at.ac.tuwien.student.e01526624.backend.domain.ExcelWorkbook;
import at.ac.tuwien.student.e01526624.backend.domain.assertion.Assertion;
import at.ac.tuwien.student.e01526624.backend.domain.assertion.AssertionTypes;
import at.ac.tuwien.student.e01526624.backend.domain.excel.*;
import at.ac.tuwien.student.e01526624.backend.service.exceptions.CellNotFoundException;
import at.ac.tuwien.student.e01526624.backend.service.exceptions.WorkbookAlradyAddedException;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class OntologyService {

    private ModelManager manager;
    private OntModel model;
    private OntModel infModel;

    @Autowired
    public OntologyService(ModelManager manager) {
        this.manager = manager;
        this.model = manager.getExcelModel();
        this.infModel = manager.getInferencedExcelModel();
    }

    public void addWorkbook(ExcelWorkbook workbook) throws WorkbookAlradyAddedException {
        manager.beginTx();
        if ( model.listStatements(new SimpleSelector(null, manager.filehash, workbook.getSha1())).hasNext()) {
            throw new WorkbookAlradyAddedException();
        }
        Individual wb = addWorkbook(workbook.getFilename(), workbook.getSha1());

        // We maintain a mapping from location to cells here since we need this to later set up the dependencies with anonymous RDF instances
        try {
            HashMap<String, Individual> cells = new HashMap<>();
            int counter = 0;
            for (Cell cell : workbook.getCells()) {
                cells.put(
                        toStringLocation(cell.getWorksheet(), cell.getRow(), cell.getColumn()),
                        addCell(wb, cell)
                );
                counter++;
            }
            for (Cell cell : workbook.getCells()) {
                if (cell instanceof CalculatedCell) {
                    addDependenciesForCell((CalculatedCell) cell, cells);
                }
            }
            System.out.println("Added cells: " + counter);
            this.manager.saveModel();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Cell getCell(String workbookHash, String worksheet, int row, int column, boolean transitiveDependencies) throws CellNotFoundException {
        manager.beginTx();
        String queryString = "PREFIX xlsx: <" + ModelManager.NS + "> SELECT ?cell where {" +
            "?cell xlsx:cell_column ?column ." +
            "?cell xlsx:worksheet \"" + worksheet + "\" ." +
            "?cell xlsx:cell_row ?row ." +
            "?cell xlsx:workbook ?excelfile ." +
            "?excelfile xlsx:filehash \"" + workbookHash + "\" ." +
            "FILTER (?row = " + row + ") ." +
            "FILTER (?column = " + column + ") ." +
            "}";
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet rs = qe.execSelect();
        if (!rs.hasNext()) {
            manager.endTx();
            throw new CellNotFoundException();
        }
        Individual resultCell;
        if (transitiveDependencies) {
            resultCell = infModel.getOntResource(rs.next().getResource("cell")).asIndividual();
        } else {
            resultCell = model.getOntResource(rs.next().getResource("cell")).asIndividual();
        }
        Cell fullCell =  buildCellFromIndividual(resultCell, true);
        manager.endTx();
        return fullCell;
    }

    private Cell buildCellFromIndividual(Individual i, boolean resolveDependentCells) {
        Cell c;
        if (i.hasOntClass(manager.pureCell)) {
            c = new PureCell();
        } else {
            CalculatedCell ccell = new CalculatedCell();
            ccell.setFormula(i.getProperty(manager.cellFormula).getObject().asLiteral().getString());
            if (resolveDependentCells) {
                setCalculatedCellDependencies(i, ccell);
            }
            c = ccell;
        }
        c.setWorksheet(i.getProperty(manager.cellWorksheet).getObject().asLiteral().getString());
        c.setRow(i.getProperty(manager.cellRow).getObject().asLiteral().getInt());
        c.setColumn(i.getProperty(manager.cellColumn).getObject().asLiteral().getInt());
        Statement valueProp = i.getProperty(manager.cellValue);
        if (valueProp != null) {
            Literal cellVal = valueProp.getObject().asLiteral();
            switch (cellVal.getDatatype().getURI()) {
                case "http://www.w3.org/2001/XMLSchema#string":
                    c.setStringValue(cellVal.getString());
                    break;
                case "http://www.w3.org/2001/XMLSchema#int":
                    c.setNumericValue(cellVal.getInt());
                    break;
                case "http://www.w3.org/2001/XMLSchema#double":
                    c.setNumericValue(cellVal.getDouble());
                    break;
                case "http://www.w3.org/2001/XMLSchema#boolean":
                    c.setBooleanValue(cellVal.getBoolean());
                    break;
                default:
                    break;
            }
        }

        return c;
    }

    private void setCalculatedCellDependencies(Individual i, CalculatedCell calculatedCell) {
        StmtIterator statementIterator = i.listProperties(manager.cellUsesValueFrom);
        while (statementIterator.hasNext()) {
            Statement st = statementIterator.next();
            Individual dependentCell = model.getOntResource(st.getObject().asResource()).asIndividual();
            calculatedCell.addDependentCell(buildCellFromIndividual(dependentCell, false));
        }
    }

    public boolean workBookAlreadyExists(String fileHash) {
        manager.beginTx();
        String queryString = "PREFIX xlsx: <" + ModelManager.NS + "> SELECT ?wb where {" +
                "?cell xlsx:workbook ?wb ." +
                "?wb xlsx:filehash \"" + fileHash + "\" ." +
                "}";
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet rs = qe.execSelect();
        //FA6DCA67F2F859C726654D78A73898A5BFB52FD0
        if (rs.hasNext()) {
            manager.endTx();
            return true;
        }
        manager.endTx();
        return false;
    }

    public Collection<Cell> validateAssertion(Assertion assertion) {
        manager.beginTx();
        String queryString = "PREFIX xlsx: <" + ModelManager.NS + "> SELECT ?cell where {" +
                "?cell xlsx:cell_column ?column ." +
                "?cell xlsx:worksheet \"" + assertion.getWorksheet() + "\" ." +
                "?cell xlsx:cell_row ?row ." +
                "?cell xlsx:cell_value ?value ." +
                "?cell xlsx:workbook ?excelfile ." +
                "?excelfile xlsx:filehash \"" + assertion.getWorkbookHash() + "\" ." +
                "FILTER (?row >= " + assertion.getRowStart() + ") ." +
                "FILTER (?row <= " + assertion.getRowEnd() + ") ." +
                "FILTER (?column >= " + assertion.getColumnStart() + ") ." +
                "FILTER (?column <= " + assertion.getColumnEnd() + ") ." +
                (assertion.getType().equals(AssertionTypes.STR_EQ) ? buildStringFilterString(assertion) : buildNumericFilterString(assertion)) +
                "}";
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, model);
        ResultSet rs = qe.execSelect();
        List<Cell> violatingCells = new ArrayList<>();
        while (rs.hasNext()) {
            Individual resultCell = model.getOntResource(rs.next().getResource("cell")).asIndividual();
            Cell vCell = buildCellFromIndividual(resultCell, false);
            violatingCells.add(vCell);
        }
        manager.endTx();
        return violatingCells;
    }

    public Collection<ExcelWorkbook> getAllWorkbooks() {
        manager.beginTx();
        List<ExcelWorkbook> workbooks = new ArrayList<>();
        Iterator<Individual> individualIterator = model.listIndividuals(manager.excelFile);
        while (individualIterator.hasNext()) {
            workbooks.add(buildWorkbookFromIndividualWithoutCells(individualIterator.next()));
        }
        manager.endTx();
        return workbooks;
    }

    private ExcelWorkbook buildWorkbookFromIndividualWithoutCells(Individual individual) {
        ExcelWorkbook wb = new ExcelWorkbook();
        wb.setFilename(individual.getProperty(manager.filename).getObject().asLiteral().getString());
        wb.setSha1(individual.getProperty(manager.filehash).getObject().asLiteral().getString());
        return wb;
    }

    private String buildStringFilterString(Assertion assertion) {
        return "?cell xlsx:cell_value \"" + assertion.getStrVal() + "\" .";
    }

    private String buildNumericFilterString(Assertion assertion) {
        String s = "FILTER(?value ";
        switch (assertion.getType()) {
            case EQ:
                s += "!= ";
                break;
            case LT:
                s += ">= ";
                break;
            case LTE:
                s += "> ";
                break;
            case GT:
                s += "<= ";
                break;
            case GTE:
                s += "< ";
                break;
        }
        s += assertion.getNumVal();
        s += ") .";
        return s;
    }

    private Individual addWorkbook(String filename, String filehash) {
        Individual wb = manager.excelFile.createIndividual();
        wb.addLiteral(manager.filename, filename);
        wb.addLiteral(manager.filehash, filehash);
        return wb;
    }

    private Individual addCell(Individual wb, Cell cell) throws IncorrectValueTypeException {
        Individual c;
        if (cell instanceof PureCell) {
            c = manager.pureCell.createIndividual();
        } else {
            CalculatedCell calculatedCell = (CalculatedCell) cell;
            c = manager.calculatedCell.createIndividual();
            c.addLiteral(manager.cellFormula, calculatedCell.getFormula());
        }
        ValueType vt = cell.getValueType();
        if (vt != null) {
            switch (cell.getValueType()) {
                case NUMERIC:
                    c.addLiteral(manager.cellValue, cell.getNumericValue());
                    break;
                case STRING:
                    c.addLiteral(manager.cellValue, cell.getStringValue());
                    break;
                case BOOLEAN:
                    c.addLiteral(manager.cellValue, cell.getBooleanValue());
                    break;
                default:
                    System.out.println("NOTHING MATCHES!!");
                    break;
            }
        }
        c.addLiteral(manager.cellWorksheet, cell.getWorksheet());
        c.addLiteral(manager.cellColumn, cell.getColumn());
        c.addLiteral(manager.cellRow, cell.getRow());
        c.addProperty(manager.cellWorkbook, wb);
        return c;
    }

    private void addDependenciesForCell(CalculatedCell cell, HashMap<String, Individual> cellMap) {
        Individual baseCell = cellMap.get(toStringLocation(cell.getWorksheet(), cell.getRow(), cell.getColumn()));
        for (Cell dependency : cell.getDependentOn()) {
            baseCell.addProperty(
                    manager.cellUsesValueFrom,
                    cellMap.get(toStringLocation(dependency.getWorksheet(), dependency.getRow(), dependency.getColumn()))
            );
        }
    }

    private String toStringLocation(String worksheet, int row, int column) {
        return worksheet + "!" + column + ":" + row;
    }
}
