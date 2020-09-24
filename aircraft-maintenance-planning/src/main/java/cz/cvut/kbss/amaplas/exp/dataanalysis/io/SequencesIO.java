package cz.cvut.kbss.amaplas.exp.dataanalysis.io;

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.*;
import cz.cvut.kbss.amaplas.exp.dataanalysis.model.Task;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.eclipse.rdf4j.repository.Repository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class SequencesIO {


//    public static SequenceDatabase loadFromFile(){
//
//    }


    public static SequenceDatabase build(List<Task> tasks, Registry registry){
        tasks = new ArrayList<>(tasks);
        tasks.sort(Comparator.comparingLong(t -> t.start));
        tasks.sort(Comparator.comparing(t -> t.wp));

        SequenceDatabase sd = new SequenceDatabase();
        Sequence currentSeq = null;
        String currentWp = null;
        Itemset currentItemset = null;
        long currentTime = -1;
        long l = 0;
        for(Task t : tasks){
            if(!t.wp.equals(currentWp)){
                currentSeq = new Sequence(registry.getId(t.wp));
                sd.addSequence(currentSeq);
                currentWp = t.wp;
                currentTime = -1;
                l = 0;
            }

            if(currentTime != t.start){
                currentItemset = new Itemset();
                currentItemset.setTimestamp(l ++);
                currentSeq.addItemset(currentItemset);
                currentTime = t.start;
            }

            currentItemset.addItem(new ItemSimple(registry.getId(t.task)));
        }
        return sd;
    }

    public static void assignUniqueIdsToSequences(Sequences sequences){
        int[] id = {0};
        sequences.getLevels().stream()
                .flatMap(l -> l.stream())
                .forEach(s -> s.setID(id[0]++));

    }

    /**
     * the list is already sorted
     * first element corresponds to the wp, the rest of the row contains task type codes
     * @param list
     * @return
     */
    public static SequenceDatabase build(List<List<Integer>> list){

        SequenceDatabase sequenceDatabase = new SequenceDatabase();
        long time = 0;
        Integer currentSequence = -1;
        Sequence s = null;
        for(int i = 0; i < list.size(); i++){
            List<Integer> row = list.get(i);
            if(row.size() < 2 )
                continue;
            if(!currentSequence.equals(row.get(0))){
                time = 0;
                currentSequence = row.get(0);
                if(s != null){
                    sequenceDatabase.addSequence(s);
                }
                s = new Sequence(i);
            }


            Itemset itemset = new Itemset();
            itemset.setTimestamp(time);
            row.stream().skip(1L).forEach(ttc -> itemset.addItem(new ItemSimple(ttc)));
        }
        return  sequenceDatabase;
    }

    /**
     *
     * @param ss a sequence patterns found in the sequence database
     */
    public static void write(Sequences ss, Registry registry, String outputDir, String fileNamePrefix) throws IOException {
        // save task sequence patterns
        File f = new File(outputDir, fileNamePrefix + "-tsp.csv");
        try(PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            writeTaskSequencePatterns(pw, ss, registry);
        }

        // save task sequence pattern elements
        f = new File(outputDir, fileNamePrefix + "-tspe.csv");
        try(PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            writeTaskSequencePatternElements(pw, ss, registry);
        }

        // save task sequence pattern per workpackage
        f = new File(outputDir, fileNamePrefix + "-tsp_wp.csv");
        try(PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            writeTaskSequencePatternPerWorkPackage(pw, ss, registry);
        }

    }


//    public static Model persistInTripleStore(Sequences sequences){
//        Model m = ModelFactory.createDefaultModel();
//        for(List<Sequence> ss : sequences.getLevels()){
//            for(Sequence s : ss) {
//
//            }
//        }
//
////        RDF.re
//        return m;
////        QueryExecutionFactory.sparqlService("", "")
////        UpdateRequest ur = UpdateFactory.create("PREFIX : <http://example.com#> \n INSERT{ GRAPH <http://example.com> {:a :b :c.} } WHERE {}");
////        UpdateProcessor up = UpdateExecutionFactory.createRemote(ur, endpoint, );
////        up.execute();
//    }

    protected static void writeTaskSequencePatterns(PrintWriter pw, Sequences ss, Registry registry) throws IOException{
        // save task sequence patterns
        // print header
        pw.println("patternId;support;sequence_size");
        List<List<Sequence>> levels = ss.getLevels();
        for(List<Sequence> seqs: levels){
            for(Sequence s : seqs){
                // - patternId
                pw.print(s.getId());
                pw.print(";");
                // - probability
                pw.print(s.getSequencesID().size());
                pw.print(";");
                // - sequence size
                pw.println(s.size());
            }
        }
    }

    protected static void writeTaskSequencePatternElements(PrintWriter pw, Sequences ss, Registry registry) throws IOException{
        // save task sequence patterns
        // print header
        pw.println("patternId;time_index;task_type_code");
        List<List<Sequence>> levels = ss.getLevels();
        for(List<Sequence> seqs: levels){
            for(Sequence s : seqs){
                for(Itemset is : s.getItemsets()){
                    for(ItemSimple item : is.getItems()) {
                        // - patternId
                        pw.print(s.getId());
                        pw.print(";");
                        // - time index
                        pw.print(is.getTimestamp());
                        pw.print(";");
                        // - tcc
                        pw.println(registry.getVal(item.getId()));
                    }
                }
            }
        }
    }

    protected static void writeTaskSequencePatternPerWorkPackage(PrintWriter pw, Sequences ss, Registry registry) throws IOException{
        // save task sequence patterns
        // print header
        pw.println("patternId;wp_id");
        List<List<Sequence>> levels = ss.getLevels();
        for(List<Sequence> seqs: levels){
            for(Sequence s : seqs){
                for(Integer seqId : s.getSequencesID()){
                    // - patternId
                    pw.print(s.getId());
                    pw.print(";");
                    // - wp_id
                    pw.println(registry.getVal(seqId));
                }
            }
        }
    }

    public static class Registry {

        protected BidiMap<String,Integer> registry = new DualHashBidiMap<>();
        protected int lastIndex = 0;

        public Integer getId(String str){
            Integer ret = registry.get(str);
            if(ret == null){
                ret = lastIndex;
                registry.put(str, lastIndex ++);
            }
            return ret;
        }

        public boolean exists(String str){
            return registry.containsKey(str);
        }

        public String getVal(Integer id){
            return registry.getKey(id);
        }

        public List<List<Integer>> toIds(List<List<String>> in ){
            return in.stream().map(l ->
                    l.stream().map(this::getId).collect(Collectors.toList())
            ).collect(Collectors.toList());
        }

        public List<List<String>> toVals(List<List<Integer>> in ){
            return in.stream().map(l ->
                    l.stream().map(this::getVal).collect(Collectors.toList())
            ).collect(Collectors.toList());
        }
    }
}
