package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences;

import cz.cvut.kbss.amaplas.exp.common.ResourceUtils;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.SequencePattern;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Seq;
import org.jgrapht.alg.TransitiveReduction;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.awt.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//import org.jgrapht.alg.TransitiveReduction;
//import org.jgrapht.graph.DefaultDirectedGraph;

public class ExtractData {
    public static final String DA_TASK_SO_WITH_WP_SCOPE = "/queries/analysis/task-start-order-with-wp-and-scope.sparql";


    protected static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

    public void execute(String endpoint) throws IOException {
        String query = ResourceUtils.loadResource(DA_TASK_SO_WITH_WP_SCOPE);
        ResultSet rs = QueryExecutionFactory
                .sparqlService(endpoint, query)
                .execSelect();

        List<Result> results = load(rs);

        Map<GroupDate, List<Result>> grouped = results.stream()
                .collect(Collectors.groupingBy(r -> toGroupDay(r)));

        List<String> allTasks = results.stream().map(r -> r.type).sorted().distinct().collect(Collectors.toList());
        Map<WPTask, Long> freqs = results.stream().map(r -> toWPTask(r))
                .distinct()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        freqs.keySet().stream().collect(Collectors.groupingBy(e -> e.type, Collectors.counting())).entrySet()
                .stream()
                        .sorted(Comparator.comparingLong((Map.Entry<String, Long> e) -> e.getValue()).reversed())
                        .forEach(e -> System.out.println(String.format("%d, %s", e.getValue(), e.getKey())));

        extractSequenceUsingRestrictions(results);
//        print(rs);
    }

    public void executeSequenceExtraction(String endpoint) throws IOException {
        String query = ResourceUtils.loadResource(DA_TASK_SO_WITH_WP_SCOPE);
        ResultSet rs = QueryExecutionFactory
                .sparqlService(endpoint, query)
                .execSelect();

        List<Result> results = load(rs);
        // parameters
        float supportFraction = 0.6f;
//        long minDistance = 7*24*60*60*1000; // milliseconds
        long minDistance = 1*24*60*60*1000; // milliseconds
        Function<Result, Date> orderBy = r -> r.end;
        Comparator<Result> order = Comparator.comparing(orderBy);



        long numberOfWPs = results.stream().map(r -> r.wp).distinct().count();
        float support = supportFraction*numberOfWPs;

        // filter out task types which do not satisfy the expected support
        Map<String, Long> rrr = results.stream().map(r -> Pair.of(r.type, r.wp)).distinct().collect(Collectors.groupingBy(p -> p.getKey(), Collectors.counting()))
                .entrySet().stream().filter(e -> e.getValue() >= support).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        rrr.entrySet().stream().sorted(Comparator.comparing(e -> e.getValue())).forEach(r -> System.out.println(r.getValue() + ", " + r.getKey()));

        //

        // split into WP sequences
        Map<String, List<Result>> sequencess = results.stream()
                // filter sequences select only tasks which have the expected support
                .filter(r -> rrr.containsKey(r.type))
                .collect(Collectors.groupingBy(r -> r.wp));


        // find sequence pattern of length 2 with required support
//        List<SequencePattern> supportedPairs = calculateSupportedOrderedPairs(sequencess, numberOfWPs, support, minDistance);
        List<List<Result>> startSequences = sequencess.values().stream().map(l -> l.stream()
                    .sorted(order)
                    .collect(Collectors.groupingBy(r -> r.type))
                    .entrySet().stream().map(e -> e.getValue().stream()
                                    .collect(Collectors.minBy(order)).get()
                    ).collect(Collectors.toList())
        ).collect(Collectors.toList());

        List<SequencePattern> supportedPatterns = new TimeSequenceMatrix().execute(startSequences, support, true);

        supportedPatterns = filterTransitiveEdges(supportedPatterns);

        // print pattern edges
        supportedPatterns.stream().forEach(p -> System.out.println(p.instances.size() + ";" + String.join(";", p.pattern)));

        // print nodes
        System.out.println();
        System.out.println();
        System.out.println();

        Set<String> usedTypes = new HashSet<>();
        List<Result> nodes = supportedPatterns.stream().flatMap(p -> p.instances.stream().flatMap(i -> i.stream()))
                .map(r -> usedTypes.add(r.type) ? r : null)
                .filter(r -> r != null)
                .distinct()
                .collect(Collectors.toList());

//        // calculate maximal group of groups starting each task
//        Map<String, List<String>> taskTeamOfGroups = new HashMap<>();
//        nodes.stream().map(r -> Pair.of(r.type, r.scope)).collect(Collectors.groupingBy(p -> p.getKey()))
//                .entrySet().forEach(e -> taskTeamOfGroups.put(e.getKey(), e.getValue().stream().map(p -> p.getValue()).collect(Collectors.toList())));

        System.out.println(Result.header(";") );
        nodes.forEach(r -> System.out.println(r.toString(";")));


        // print graph edges
        // print graph nodes
        System.out.println(supportedPatterns.size());
    }


    public List<SequencePattern> calculateSupportedOrderedPairs(Map<String, List<Result>> sequencess, long numberOfWPs, float support, long maximumduration){
        List<Map.Entry<String,List<Result>>> ss = new ArrayList<>(sequencess.entrySet());
        ss.forEach(l -> l.getValue().sort(Comparator.comparing(r -> r.start)));

        Function<Result, String> patternFunction = r -> r.type;

        Map<String, SequencePattern> foundPatterns = new HashMap<>();
        for (int i = 0; i < numberOfWPs - support  ; i ++){
            List<Result> wpi = ss.get(i).getValue();
            List<SequencePattern> newPatterns = new ArrayList<>();
            // generate pairs
            Set<String> usedTypes = new HashSet<>();
            System.out.println();
            for(int j1 = 0; j1 < wpi.size() - 1; j1 ++){
                Result r1 = wpi.get(j1);
                if(usedTypes.contains(r1.type))
                    continue;
                usedTypes.add(r1.type);
                for(int j2 = j1 + 1; j2 < wpi.size(); j2 ++){
                    Result r2 = wpi.get(j2);
                    if(usedTypes.contains(r2.type))
                        continue;
//                    usedTypes.add(r2.type);
                    // additional conditions
                    // r1.date.equals(r2.date) && // starting the same day not just within a given duration between the tasks, e.g. 24 hours
                    // r1.scope.equals(r2.scope) && // same group
                    if(r2.type != r1.type &&  r1.start.before(r2.start) && r2.start.getTime() - r1.start.getTime() < maximumduration){
                        SequencePattern sp = new SequencePattern(patternFunction, r1, r2);
                        String patternId = sp.patternId();
                        if(foundPatterns.get(patternId) == null) {
                            foundPatterns.put(patternId, sp);
                            newPatterns.add(sp);
                        }
                    }
                }
            }

            // calculate support of patterns
            for(int j = i + 1; j < numberOfWPs; j ++) {

                List<Result> wpj = ss.get(i).getValue();
                // count pair occurrence
                for(SequencePattern p : newPatterns) {

                    int supportedComps = 0;
                    for(int k1 = 0; k1 < wpj.size() - 1; k1 ++){
                        for(int k2 = k1 + 1; k2 < wpj.size(); k2 ++){
                            if(p.addIfInstance(wpj.get(k1), wpj.get(k2))) {
                                k1 = k2 = wpj.size();// break both cycles
                            }
                        }
                    }


//                    int k = 0;
//                    for(; k < wpj.size(); k ++){
//                        if(wpj.get(k).type.equals(p.pattern.get(0))){
//                            break;
//                        }
//                    }
//                    for(; k < wpj.size(); k ++){
////                        if(wpj.get(k).type.equals(p.r2.type)){
////                            p.support = p.support + 1;
////                            if(p.support > support && !foundPatterns.contains(p.r1.type + "-" + p.r2.type)){
////                                foundPatterns.add(p);
////                                foundPatterns.add(p.r1.type + "-" + p.r2.type);
////                            }
////                            break;
////                        }
//                    }
                }
            }

        }

        // filter reverse edges
//        support
        return foundPatterns.values().stream().filter( r -> r.instances.size() >= support).collect(Collectors.toList());
    }

//    public List<SequencePattern> filterTransitiveEdgesWithSameSupport(List<SequencePattern> patterns){
    public static List<SequencePattern> filterTransitiveEdges(List<SequencePattern> patterns){
        // assuming patterns are of length 2
//        DirectedGra

        DefaultDirectedGraph<String, SequencePattern> g = new DefaultDirectedGraph<>(SequencePattern.class);
        for(SequencePattern sp : patterns){
            for(String v : sp.pattern){
                if(!g.containsVertex(v)){
                    g.addVertex(v);
                }
            }
            g.addEdge(sp.pattern.get(0), sp.pattern.get(1), sp);

            g.outgoingEdgesOf(sp.pattern.get(0));
            g.outgoingEdgesOf(sp.pattern.get(1));
        }

        System.out.println(String.format("computing transitive reduction of graph with %d nodes and %d edges", g.vertexSet().size(), g.edgeSet().size()));


        TransitiveReduction.INSTANCE.reduce(g);
        System.out.println(String.format("done computing transitive reduction. Graph now has %d nodes and %d edges", g.vertexSet().size(), g.edgeSet().size()));
        return new ArrayList<>(g.edgeSet());
    }

    public void extractSequenceUsingRestrictions(List<Result> results){
        // order by date
        System.out.println("nodes");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println(Result.header());
        results.forEach(r -> System.out.println(r.toString()));
        for(int i = 0; i < results.size() - 1; i ++) {
            Result r = results.get(i);

        }


        Set<List<String>> pairs = new HashSet<>();
        Set<String> visitedType = new HashSet<>();
        Set<String> visitedWpAndType = new HashSet<>();
        Map<String, String> directEdgeCondition = new HashMap<>();
        for(int i = 0; i < results.size() - 1; i ++){
            Result r1 = results.get(i);
//            if(visitedType.contains(r1.type))
            String wpAndType = r1.wp + r1.type;
            if(!visitedWpAndType.add(wpAndType))
                continue;

//            visitedWpAndType.add(r1.wp + r1.type);
            visitedType.add(r1.type);
            Date start2 = null;
            for(int j = i + 1; j < results.size() ; j ++){
                Result r2 = results.get(j);
                if(start2 != null && start2.equals(r2.start)){
                    break; // stop extracting redundant start order edges
                }

                if(
                        r1.date.equals(r2.date) &&
                        r1.wp.equals(r2.wp) &&
                        r1.start.compareTo(r2.start) < 0 &&
                        r1.scope.equals(r2.scope) &&
                        !visitedType.contains(r2.type) &&
                        !r1.type.equals(r2.type)
                ){
                    if(start2 == null)
                        start2 = r2.start;
                     List<String> l = Arrays.asList(r1.type, r2.type);
//                    List<String> rl = Arrays.asList(r2.type, r1.type);
                    pairs.add(l);
                }
            }
        }
        System.out.println("edges");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("t1,t2");
        pairs.forEach(l -> System.out.println(l.stream().collect(Collectors.joining(","))));
    }


    protected List<String> getColors(int count){
        return IntStream.range(0,count)
                .mapToObj(i -> Color.getHSBColor(((float)i)/count,0.5f, 0.5f))
                .map(c -> (String.format("%06x",c.getRGB())))
                .collect(Collectors.toList());
    }

    public static void print(ResultSet rs) {
//        CSVPrinter p = new CSVPrinter(System.out, CSVFormat.DEFAULT);
//        p.pr
        while(rs.hasNext()){
            QuerySolution qs = rs.next();
            System.out.println(rs.getResultVars().stream().map(n -> qs.get(n).toString()).collect(Collectors.joining("; ")));
        }
    }


    public static List<Result> load(ResultSet rs){
        List<Result> results = new ArrayList<>();
        while(rs.hasNext()){
            QuerySolution qs = rs.next();
            try {
                results.add(load(qs));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    public static class GroupDate {
        public String scope;
        public String date;

        public GroupDate() {
        }

        public GroupDate(Result r) {
            scope = r.scope;
            date = r.date;
        }

        public GroupDate(String scope, String date) {
            this.scope = scope;
            this.date = date;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof GroupDate)) return false;
            GroupDate groupDate = (GroupDate) o;
            return scope.equals(groupDate.scope) &&
                    date.equals(groupDate.date);
        }

        @Override
        public int hashCode() {
            return Objects.hash(scope, date);
        }
    }

    public static GroupDate toGroupDay(Result r){
        return new GroupDate(r);
    }

    public static class WPTask{
        public String wp;
        public String type;


        public WPTask() {
        }

        public WPTask(Result r) {
            wp = r.wp;
            type = r.type;
        }

        public WPTask(String wp, String type) {
            this.wp = wp;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof WPTask)) return false;
            WPTask wpTask = (WPTask) o;
            return wp.equals(wpTask.wp) &&
                    type.equals(wpTask.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(wp, type);
        }
    }

    public static WPTask toWPTask(Result r){
        return new WPTask(r);
    }


    public static Result load(QuerySolution qs) throws ParseException {
        Result t = new Result();
        t.wp = qs.get("wp").toString();
        t.type = qs.get("type").toString();
        t.scope = qs.get("scope").toString();
        t.taskcat = qs.get("taskcat").toString();
        t.date = qs.get("date").toString();
        t.start = df.parse(qs.get("start").toString());
        t.end = df.parse(qs.get("end").toString());
        t.dur = qs.get("dur").asLiteral().getLong();
        return t;
    }

    static class MyEdge{
        int from;
        int to;

        public MyEdge() {

        }

        public MyEdge(int from, int to) {
            this.from = from;
            this.to = to;
        }
    }

    public static void testTransitiveReduction(){
//        DefaultDirectedGraph<Integer, MyEdge> g = new DefaultDirectedGraph<>(MyEdge.class);
//        g.addVertex(1);
//        g.addVertex(2);
//        g.addVertex(3);
//        g.addEdge(1,2, new MyEdge(1,2));
//        g.addEdge(1,3, new MyEdge(1,3));
//        g.addEdge(2,3, new MyEdge(2,3));
//        TransitiveReduction.INSTANCE.reduce(g);
//        System.out.println(g.edgeSet().size());
//        g.edgeSet().stream().forEach(e -> System.out.println(g.getEdgeSource(e) + "->" + g.getEdgeTarget(e)));

        List<Integer> ints = Arrays.asList(1,2,3);
        List<SequencePattern> ps = new ArrayList<>();
        for(int i = 0; i < ints.size() - 1; i ++){
            for(int j = i + 1; j < ints.size(); j ++){
                SequencePattern sp = new SequencePattern();
                sp.pattern = Arrays.asList(i + "", j + "");
                ps.add(sp);
            }
        }
//        SequencePattern sp = new SequencePattern();
//        sp.pattern = Arrays.asList("1","0");
//        ps.add(sp);

        List<SequencePattern> reduced = filterTransitiveEdges(ps);
    }

    public static void main(String[] args) throws IOException {
//        testTransitiveReduction();
//        new ExtractData().execute("http://localhost:7200/repositories/doprava-2020-csat-example-data");
        new ExtractData().executeSequenceExtraction("http://localhost:7200/repositories/doprava-2020-csat-example-data");


//        System.out.println(System.currentTimeMillis());
//        String query = "SELECT * {?s ?p ?o}LIMIT 10";
//        SPIF.DATE_FORMAT_FUNCTION
//        ResultSet rs = QueryExecutionFactory
//                .sparqlService("http://localhost:7200/repositories/doprava-2020-csat-example-data", query)
//                .execSelect();
//        System.out.println(rs.getResultVars().stream().collect(Collectors.joining("; ")));
//
//        while(rs.hasNext()){
//            QuerySolution qs = rs.next();
//            System.out.println(rs.getResultVars().stream().map(n -> qs.get(n).toString()).collect(Collectors.joining("; ")));
//        }
    }
}
