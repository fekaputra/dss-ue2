package at.ac.tuwien.student.e01526624.backend.service;

import org.apache.jena.ontology.*;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.*;
import org.apache.jena.tdb.TDBFactory;
import org.springframework.stereotype.Component;

@Component
public class ModelManager {


    public static final String NS = "http://www.jacobpalecek.com/01526624/dss-ue2-ss20/excel#";
    public static final String MODEL_NAME = "ExcelRdfModel";
    public static final String DATASET_LOCATION = "target/ds";
    public static final String CELL_URI = NS + "Cell";
    public static final String PURE_CELL_URI = NS + "PureCell";
    public static final String CALCULATED_CELL_URI = NS + "CalculatedCell";
    public static final String CELL_VALUE_URI = NS + "cell_value";
    public static final String CELL_WORKSHEET_URI = NS + "worksheet";
    public static final String CELL_WORKBOOK_URI = NS + "workbook";
    public static final String CELL_COLUMN_URI = NS + "cell_column";
    public static final String CELL_ROW_URI = NS + "cell_row";
    public static final String CELL_USESVALUEFROM_URI = NS + "usesValueFrom";
    public static final String CELL_FORMULA_URI = NS + "cell_formula";
    public static final String FILE_NAME_URI = NS + "filename";
    public static final String FILE_HASH_URI = NS + "filehash";
    public static final String EXCEL_FILE_URI = NS + "ExcelFile";

    private OntModel excelModel;
    private OntModel inferencedExcelModel;
    private Dataset dataset;

    public OntClass cell;
    public OntClass pureCell;
    public OntClass calculatedCell;
    public OntClass excelFile;
    public DatatypeProperty cellValue;
    public DatatypeProperty cellWorksheet;
    public DatatypeProperty cellColumn;
    public DatatypeProperty cellRow;
    public DatatypeProperty cellFormula;
    public DatatypeProperty filename;
    public DatatypeProperty filehash;
    public ObjectProperty cellUsesValueFrom;
    public ObjectProperty cellWorkbook;


    public ModelManager() {
        this.initialize();
    }

    public void beginTx() {
        if (!this.dataset.isInTransaction()) {
            this.dataset.begin(ReadWrite.WRITE);
        }
    }

    public void endTx() {
        this.dataset.commit();
        this.dataset.end();
    }

    public OntModel getExcelModel() {
        return excelModel;
    }

    public OntModel getInferencedExcelModel() {
        return inferencedExcelModel;
    }

    public void saveModel() {
        if (this.dataset.isInTransaction()) {
            this.dataset.commit();
            this.dataset.end();
        }
        this.dataset.begin(ReadWrite.WRITE);
//        this.dataset.replaceNamedModel(MODEL_NAME, model);
        this.dataset.commit();
        this.dataset.end();
    }


    private void initialize() {
        this.dataset = TDBFactory.createDataset(DATASET_LOCATION);
        if (!this.dataset.containsNamedModel(MODEL_NAME)) {
            createInitialDataset();
        }
        loadModels();
        loadOntologyArtifacts();
    }

    private void loadModels() {
        this.dataset.begin(ReadWrite.READ);
        Model base = this.dataset.getNamedModel(MODEL_NAME);
        this.excelModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, base);
        this.inferencedExcelModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, base);
        this.dataset.commit();
        this.dataset.end();
    }

    private void loadOntologyArtifacts() {
        this.dataset.begin(ReadWrite.READ);
        this.cell = this.excelModel.getOntClass(CELL_URI);
        this.pureCell = this.excelModel.getOntClass(PURE_CELL_URI);
        this.excelFile = this.excelModel.getOntClass(EXCEL_FILE_URI);
        this.calculatedCell = this.excelModel.getOntClass(CALCULATED_CELL_URI);
        this.cellValue = this.excelModel.getDatatypeProperty(CELL_VALUE_URI);
        this.cellWorksheet = this.excelModel.getDatatypeProperty(CELL_WORKSHEET_URI); // not working somehow
        this.cellRow = this.excelModel.getDatatypeProperty(CELL_ROW_URI);
        this.cellColumn = this.excelModel.getDatatypeProperty(CELL_COLUMN_URI);
        this.cellFormula = this.excelModel.getDatatypeProperty(CELL_FORMULA_URI);
        this.filehash = this.excelModel.getDatatypeProperty(FILE_HASH_URI);
        this.filename = this.excelModel.getDatatypeProperty(FILE_NAME_URI);
        this.cellWorkbook = this.excelModel.getObjectProperty(CELL_WORKBOOK_URI);
        this.cellUsesValueFrom = this.excelModel.getObjectProperty(CELL_USESVALUEFROM_URI);
        this.dataset.end();
    }

    private void createInitialDataset() {
        System.out.println("Creating initial dataset");
        this.dataset.begin(ReadWrite.WRITE);
        OntModel base = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        base.read("ontology/excel_ontology.owl");
        this.dataset.addNamedModel(MODEL_NAME, base);
        this.dataset.commit();
        this.dataset.end();
    }

}
