package cz.cvut.kbss.amaplas.exp.graphml;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

public class Graphml2Owl {


    public void execute(String input, String ns) throws IOException {
        String outputGraph = input.substring(0, input.lastIndexOf(".")) + "-graph.ttl";
        String outputSchema = input.substring(0, input.lastIndexOf(".")) + "-schema.ttl";
        Model graph = new Graphml2JenaModel().execute(input, ns, "csat-s1");
        write(graph, outputGraph);

        Model schema = new GraphToSchema().constructSchemaFromGraph(graph);
        Resource onto = schema.createResource(ns);
        onto
                .addProperty(RDF.type, OWL2.Ontology)
                .addLiteral(DCTerms.created, new Date())
                .addLiteral(DCTerms.creator, Graphml2Owl.class.getCanonicalName());

        write(schema, outputSchema);
    }

    public static void write(Model m, String out) throws IOException {
        try (Writer w = new FileWriter(out)) {
            m.write(w, "ttl");
        }
    }

    public static void script_1() throws IOException {
        String input = "c:\\Users\\kostobog\\Documents\\skola\\projects\\2019-CSAT-doprava-2020\\code\\aircraft-maintenance-planning-system\\aircraft-maintenance-planning-model\\drafts\\maintenance-planning-model-B.graphml";
        String ns = "http://csat.cz/ontologies/planning-schema/";
        new Graphml2Owl().execute(input, ns);
    }


    public static void main(String[] args) throws IOException {
        script_1();
    }
}
