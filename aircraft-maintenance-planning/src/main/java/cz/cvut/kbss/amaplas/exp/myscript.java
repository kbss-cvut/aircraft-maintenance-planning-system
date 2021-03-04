package cz.cvut.kbss.amaplas.exp;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.File;

public class myscript {
	public static void main(String[] args){
		System.out.println("Hello world!");
		Model m = ModelFactory.createDefaultModel();
		m.add(m.createResource("http://A"), RDF.type, RDFS.subClassOf);
		m.write(System.out, "TTL");
//		if(args.length == 0)
//			return;
//		String f = args[0];
//		if(!f.startsWith("http")){
//			f = new File(f)
//		}
//		m.read();
	}
}