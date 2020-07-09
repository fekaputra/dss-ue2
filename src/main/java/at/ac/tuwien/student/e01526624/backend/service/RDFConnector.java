package at.ac.tuwien.student.e01526624.backend.service;

import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.*;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_GreaterThanOrEqual;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.nodevalue.NodeValueInteger;
import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RDFConnector {
    ModelManager manager;


    @Autowired
    public RDFConnector( ModelManager modelManager, ExcelFileReader xlsxReader) {
        this.manager = modelManager;

//        test();
    }

    private void test() {
        this.manager.beginTx();
        OntModel excelModel = this.manager.getExcelModel();
        excelModel.write(System.out);
        Individual i = manager.pureCell.createIndividual();
//
//        System.out.println(i.getURI());

        System.out.println("Pure?  " + i.hasOntClass(manager.pureCell));
        System.out.println("Calculated?  " + i.hasOntClass(manager.calculatedCell));
        System.out.println("base cell?  " + i.hasOntClass(manager.cell));
//        Individual infInd = this.manager.getInferencedExcelModel().getIndividual("WB!C3");
//        System.out.println("Infind: base cell?  " + infInd.hasOntClass(manager.cell));


        i.addLiteral(manager.cellValue, 21);


        manager.saveModel();
        this.manager.beginTx();
        excelModel.write(System.out);
        this.manager.endTx();

        //new stuff
        this.manager.beginTx();
        for (int j = 0; j < 20; j++) {
            addRandomCell(excelModel);
        }
        queryValues(excelModel);
        queryAgain(excelModel);
        this.manager.endTx();
    }

    private void addRandomCell(OntModel excelModel) {
        Individual c = excelModel.createIndividual(manager.pureCell);
        c.addLiteral(manager.cellValue, randomInt());
        c.addLiteral(manager.cellWorksheet, "FirstWorkBook");
        c.addLiteral(manager.cellColumn, randomInt());
        c.addLiteral(manager.cellRow, randomInt());
    }

    private void queryValues(OntModel excelModel) {
        String queryString = "PREFIX xlsx: <http://www.semanticweb.org/01526624/dss-ue2-ss20/excel#> SELECT ?cell ?column ?row where {" +
                "?cell xlsx:cell_value ?value ." +
                "?cell xlsx:cell_column ?column ." +
                "?cell xlsx:cell_workbook ?workbook ." +
                "?cell xlsx:cell_row ?row ." +
                "FILTER (?value >= 42)" +
                "}";
        Query query = QueryFactory.create(queryString);
        QueryExecution qe = QueryExecutionFactory.create(query, excelModel);
        ResultSet rs = qe.execSelect();
        while (rs.hasNext()) {
            QuerySolution qs = rs.next();
            Resource r = qs.getResource("cell");
            Individual ind = excelModel.getOntResource(r).asIndividual();
            Double val = ind.getProperty(manager.cellValue).getObject().asLiteral().getDouble();
            System.out.println("Value is: " + val);
        }

//        ResultSetFormatter.out(System.out, rs, query);

    }

    private void queryAgain(OntModel excelModel) {
        Triple firstTriple = Triple.create(Var.alloc("cell"), Var.alloc("c_res"), Var.alloc("val"));
        Expr e = new E_GreaterThanOrEqual(new ExprVar("val"), new NodeValueInteger(42));
        ElementTriplesBlock block = new ElementTriplesBlock();
        block.addTriple(firstTriple);
        ElementFilter filter = new ElementFilter(e);
        ElementGroup body = new ElementGroup();
        body.addElement(filter);
        body.addElement(block);

        Query q = QueryFactory.make();
        q.setQueryPattern(body);
        q.setQuerySelectType();
        q.addResultVar("cell");
        q.addResultVar("val");

        QueryExecution qe = QueryExecutionFactory.create(q, excelModel);
        ResultSet rs = qe.execSelect();
        ResultSetFormatter.out(System.out, rs, q);

//        Op op;
//        BasicPattern pattern = new BasicPattern();
//        pattern.add(firstTriple);

    }

    private int randomInt() {
        return 0 + (int) (Math.random() * ((100 - 0) + 1));
    }

}
