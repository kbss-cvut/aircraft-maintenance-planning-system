package cz.cvut.kbss.amaplas.exp.graphml;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

public class Graphml2Owl {


    public void execute(String input, String outputDir, String ns, String prefix) throws IOException {
        File in = new File(input);
        String inName = in.getName();
        File outputGraph = new File(outputDir,  input.substring(0, inName.lastIndexOf(".")) + "-graph.ttl");
        File outputSchema = new File(outputDir, input.substring(0, inName.lastIndexOf(".")) + "-schema.ttl");
        String nsp = ns;
        if(!nsp.endsWith("/") && !nsp.endsWith("#"))
            nsp += "/";
        Model graph = new Graphml2JenaModel().execute(input, nsp, prefix);
        write(graph, outputGraph.getCanonicalPath());

        Model schema = new GraphToSchema().constructSchemaFromGraph(graph);
        Resource onto = schema.createResource(ns);
        onto
                .addProperty(RDF.type, OWL2.Ontology)
                .addLiteral(DCTerms.created, new Date())
                .addLiteral(DCTerms.creator, Graphml2Owl.class.getCanonicalName());

        write(schema, outputSchema.getCanonicalPath());
    }

    public static void write(Model m, String out) throws IOException {
        try (Writer w = new FileWriter(out)) {
            m.write(w, "ttl");
        }
    }

    public static void script_1() throws IOException {
        String input = "c:\\Users\\kostobog\\Documents\\skola\\projects\\2019-CSAT-doprava-2020\\code\\aircraft-maintenance-planning-system\\aircraft-maintenance-planning-model\\drafts\\maintenance-planning-model-B.graphml";
        String ns = "http://csat.cz/ontologies/planning-schema/";
        new Graphml2Owl().execute(input, ".", ns, "csat-mpmb");
    }


    public static void main(String[] args) throws IOException {
        System.out.println("Running Graphml2OWL");
//        script_1();
        System.out.println(new File(".").getCanonicalPath());
        try {
            String input = args[0];
            String ns = args[1];
            String prefix = args[2];
            String outputDir = ".";
            if(args.length > 3)
                outputDir = args[3];
            new Graphml2Owl().execute(input, outputDir, ns, prefix);
        }catch(Exception e){
            System.out.println("");
            System.out.println("could not execute Graphml2Owl");
            System.out.println("help:");
            System.out.println("description: given a uml class diagram in the graphml format create a owl schema (ttl)");
            System.out.println("form: run Graphml2Owl in namespace");
            System.out.println("arg1: in - a diagram in the graphml format, e.g. giagram.graphml");
            System.out.println("arg2: namespace - the namespace used to create IRIs of the output schema");
            System.out.println("arg3: outputDir - OPTIONAL, the directory where the output will be placed, by default it is saved in the dir from which the command was called.");
            System.out.println("output1: in-graph.ttl - contains rdf representation of the graph from the input file");
            System.out.println("output2: in-schema.ttl - contains owl schema corresponding to the input");
            System.out.println("");
            System.out.println("example: run Graphml2Owl diagram.graphml http://example.com/ontology-namespace/");
            e.printStackTrace();
        }
        System.out.println("Done.");
    }
}
