package cz.cvut.kbss.amaplas.exp.dataanalysis;

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.*;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import cz.cvut.kbss.amaplas.exp.common.ResourceUtils;
import cz.cvut.kbss.amaplas.exp.dataanalysis.io.SequencesIO;
import cz.cvut.kbss.amaplas.exp.dataanalysis.model.Task;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.ResultSetMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.binding.BindingMap;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnalyzeSequences {

    public void execute(String endpoint, String outputDir, String fileNamePrefix) throws IOException {
        List<Task> tasks = loadSequenceData(endpoint);
        extractPatterns(endpoint, tasks, outputDir, fileNamePrefix);
    }


    public void extractPatterns(String endpoint, List<Task> tasks, String outputDir, String fileNamePrefix) throws IOException {
        SequencesIO.Registry registry = new SequencesIO.Registry();
        SequenceDatabase sd = SequencesIO.build(tasks, registry);

        AlgoFournierViger08 algo = new AlgoFournierViger08(0.40,
                0, 150, 1,
                300, null, false, false);

        Sequences sequences = algo.runAlgorithm(sd);
//        fix sequence pattern ids
        SequencesIO.assignUniqueIdsToSequences(sequences);
        // write raw data
        SequencesIO.write(sequences, registry, outputDir, fileNamePrefix);

        // create and table for preview
        createSequencePatternPreviewTable(endpoint, sequences, registry, new File(outputDir, fileNamePrefix + "sp-preview.csv").getAbsolutePath());

        //
        createSequencePatternEdgeTable(sequences, registry, new File(outputDir, fileNamePrefix + "seq-edge-list.csv").getAbsolutePath());

//        List<List<Task>> taskGroups = new ArrayList<>(tasks.stream().collect(Collectors.groupingBy(t -> t.start)).values());
//        taskGroups.sort(Comparator.comparingLong(l -> l.get(0).start));
//        taskGroups.forEach(l -> System.out.println(l.get(0).start));

//        tasks.stream()
//                .collect(Collectors.groupingBy(t -> t.wp)).entrySet().stream()
//                .map(e -> Pair.of(e.getKey(), e.getValue().stream().collect(Collectors.groupingBy(t -> t.start))))
//                .map(p -> p. )
//                .collect(Collectors.groupingBy(e -> e.getValue().start)).entrySet()
//                .stream()
//                .map(e -> e.)
        return ;
    }

    protected List<Task> loadSequenceData(String endpoint){
        String query = ResourceUtils.loadResource("/queries/task-execution-starts-in-wp.sparql");
        ResultSet rs = QueryExecutionFactory.sparqlService(endpoint, query).execSelect();
        List<Task> result = new ArrayList<>();


        while(rs.hasNext()){
            QuerySolution qs = rs.next();
            Task task = new Task(
                    uriLastPart(qs.get("task").toString()),
                    ((XSDDateTime)qs.get("start").asLiteral().getValue()).asCalendar().getTimeInMillis(),
                    uriLastPart(qs.get("wp").toString())
            );
            result.add(task);
//            System.out.println(task);
        }
        return result;
    }


    protected void createSequencePatternPreviewTable(String endpoint, Sequences ss, SequencesIO.Registry r, String file) throws IOException {

        String queryStr = ResourceUtils.loadResource("/queries/task-squence-pattern-preview.sparql");
        Query q = QueryFactory.create(queryStr);

        // construct values
        Var pid = Var.alloc("pid");
        Var wp1 = Var.alloc("wp1");
        Var type = Var.alloc("type");
        Var index = Var.alloc("index");
        // patternId, wp, task type, task label, session start, session end
        // VALUES (?pid, ?wp, ?taskType


        List<Binding> bindings = ss.getLevels().stream()
                .flatMap(l -> l.stream()
                        .flatMap(s -> s.getSequencesID().stream().map(seqId -> Pair.of(r.getVal(seqId), s)))
                        .flatMap(p -> p.getValue().getItemsets().stream()
                                .flatMap(is ->
                                        is.getItems().stream().map(i -> {
                                            BindingMap bm = new BindingHashMap();
                                            bm.add(pid, NodeFactory.createLiteral(Integer.toString(p.getValue().getId())));
                                            bm.add(wp1, NodeFactory.createLiteral(p.getKey()));
                                            bm.add(type, NodeFactory.createLiteral(r.getVal(i.getId())));
                                            bm.add(index, NodeFactory.createLiteral(Long.toString(is.getTimestamp())));
                                            return (Binding)bm;
                                        })
                                ))
        ).collect(Collectors.toList());

        q.setValuesDataBlock(Arrays.asList(pid, wp1, type, index), bindings);

        ResultSet res = QueryExecutionFactory.sparqlService(endpoint, q).execSelect();
        try(FileOutputStream fos = new FileOutputStream(file)) {
            ResultSetMgr.write(fos, res, Lang.CSV);
        }
    }

    protected void createSequencePatternEdgeTable(Sequences ss, SequencesIO.Registry r, String file) throws IOException {
        try(PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("patternId;from;to;from-index;to-index");
            for (Sequence s : ss.getLevels().stream().flatMap(l -> l.stream()).collect(Collectors.toList())) {
                for (int i = 0; i < s.getItemsets().size() - 1; i++) {
                    Itemset is = s.getItemsets().get(i);
                    Itemset it = s.getItemsets().get(i+1);
                    pw.print(s.getId());
                    pw.print(";");
                    pw.print(is.getItems().stream().map(item -> r.getVal(item.getId())).collect(Collectors.joining(",")));
                    pw.print(";");
                    pw.print(it.getItems().stream().map(item -> r.getVal(item.getId())).collect(Collectors.joining(",")));
                    pw.print(";");
                    pw.print(is.getTimestamp());
                    pw.print(";");
                    pw.println(it.getTimestamp());
                }
            }
        }
    }


    protected String uriLastPart(String uri){
        return uri.replaceAll("^.*/([^/]+)$", "$1");
    }

//    static class Workpackage{
//        public String
//    }

    //    static class TaskSequencePattern{
//        protected List<List<Task>> sequence;
//
//    }

    protected BiMap<Long, Object> createIndex(Collection<? extends Object> col, Function<Object, Object> valueFunction){
        BiMap map = HashBiMap.create();

        long i = 0;
        for(Object obj : col){
            Object val = valueFunction.apply(obj);
            if(!map.containsValue(val)){
                map.put(i, val);
                i++;
            }
        }
        return map;
    }


    public static void main(String[] args) throws IOException {

        new AnalyzeSequences().execute(
                "http://localhost:7200/repositories/doprava-2020-csat-example-data",
                "c:\\Users\\kostobog\\Documents\\skola\\projects\\2019-CSAT-doprava-2020\\code\\aircraft-maintenance-planning-system\\aircraft-maintenance-planning-model\\example-instance-data\\sequence-pattern-analysis\\",
                "test-data-sequence-patterns-"
                );
    }
}
