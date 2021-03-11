package cz.cvut.kbss.ontotest;

import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL2;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class check_labels {

    protected List<Message> checkMessages = new ArrayList<>();
    String[] header = new String[]{
            "resource", "rule", "msg"
    };

    public Model loadOntology(String ontoFile){
        Model m = ModelFactory.createDefaultModel();
        Lang lang = RDFLanguages.filenameToLang(ontoFile);
        try(Reader r = new FileReader(ontoFile)) {
            m.read(r, null, lang.getLabel());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return m;
    }

    public void checkOntology(String ontoFile, String outputFile){
        Model m = loadOntology(ontoFile);
        checkOntology(m);
        writeOutput(outputFile);
    }



    public void checkOntology(Model m){
        // check classes
        listResource(m, RDF.type, OWL2.Class).forEachRemaining(this::checkDescription);
        listResource(m, RDF.type, OWL2.ObjectProperty).forEachRemaining(this::checkDescription);
    }

    public ExtendedIterator<Resource> listResource(Model m, Property p, RDFNode n){
        return m.listStatements(null, p, n).mapWith(s -> s.getSubject());
    }

    public void checkDescription(Resource r){
        String rule = "description";
        hasProperty(r, RDFS.label, rule);
        hasProperty(r, RDFS.comment, rule);
    }

    // test patterns
    public void hasProperty(Resource r, Property p, String ruleParent){
        String rule = ruleParent + "." + "hasProperty." + p.getLocalName();
        RDFNode val = val(r, p);
        if(val == null){
            addMessage(r, rule, "missing");
        }
    }

    // Messages
    protected void addMessage(Resource resource, String rule, String msg){
        addMessage(new Message(resource, rule, msg));
    }

    protected void addMessage(Message m){
        checkMessages.add(m);
    }

    // low level methods
    protected void writeOutput(String out){
        try(ICSVWriter w = new CSVWriterBuilder(new FileWriter(out)).build()){
            w.writeNext(header);
            checkMessages.stream().map(Message::toRow).forEach(w::writeNext);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected String toString(RDFNode node){
        return Optional.ofNullable(node).map(n -> n.asLiteral().toString()).orElse(null);
    }

    public RDFNode val(Resource r, Property p){
        return Optional.ofNullable(r.getProperty(p)).map(s -> s.getObject()).orElse(null);
    }

    public static class Message{
        protected Resource resource;
        protected String rule;
        protected String msg;

        public Message(Resource resource, String rule, String msg) {
            this.resource = resource;
            this.rule = rule;
            this.msg = msg;
        }


        public String[] toRow(){
            return new String[]{resource.getURI(), val(rule), val(msg)};
        }

        protected String val(String in){
            return in == null ? "" : in;
        }
    }


    public static void testRun1(){
        String root = "c:\\Users\\kostobog\\Documents\\skola\\projects\\2019-CSAT-doprava-2020\\code\\aircraft-maintenance-planning-system\\aircraft-maintenance-planning-model\\vocabularies\\csat-maintenance\\";
//        String input = root + "\\csat-maintenance.ttl";
        String input = root + "\\maintenance-general-model.ttl";
//        String output = root + "\\csat-maintenance-report.csv";
        String output = root + "\\maintenance-general-model-report.csv";
        new check_labels().checkOntology(input, output);
    }

    public static void testRun(){
        String root = "c:\\Users\\kostobog\\Documents\\skola\\projects\\2019-CSAT-doprava-2020\\code\\aircraft-maintenance-planning-system\\aircraft-maintenance-planning-model\\vocabularies\\csat-maintenance\\";
        String file = root + "\\csat-maintenance.ttl";
//        String file = root + "\\maintenance-general-model.ttl";
        String outputStr = root+"\\target\\";
        File output = new File(outputStr);
        if(!output.exists())
            output.mkdirs();
        String outputName = new File(file).getName();
        outputName = outputName.substring(0,outputName.lastIndexOf("."));
        output = new File(output, outputName);

        new check_labels().checkOntology(file, output.getPath());
    }


    // TODO - take input shape library to validate the resources in the designed ontologies.
    // https://github.com/kbss-cvut/ufo-validator/blob/master/build.gradle
    // check the UFO rules
    // create new rules concerning patterns relevant for the use case, failures, components, component states (operational states,
    // failure states, maintenance states ), functions, maintenance tasks, ordering constraints (task precedence)
    //
    public static void main(String[] args) {
//        testRun();
//        testRun1();
        System.out.println("check_labels");
        String input = args[0];
        String output = args[1];
        new check_labels().checkOntology(input, output);
    }
}
