package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences;

import cz.cvut.kbss.amaplas.exp.common.JGraphTUtils;
import cz.cvut.kbss.amaplas.exp.common.ResourceUtils;
import cz.cvut.kbss.amaplas.exp.dataanalysis.io.SparqlDataReader;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.graphics.GMatrix;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.*;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.seqalg.FilterTransitiveEdgesAlg;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.connectivity.KosarajuStrongConnectivityInspector;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//import org.jgrapht.alg.TransitiveReduction;
//import org.jgrapht.graph.DefaultDirectedGraph;

public class ExtractData {

    private static final Logger LOG = LoggerFactory.getLogger(ExtractData.class);

    public void execute(String endpoint) throws IOException {
        String query = ResourceUtils.loadResource(SparqlDataReader.DA_TASK_SO_WITH_WP_SCOPE);
        ResultSet rs = QueryExecutionFactory
                .sparqlService(endpoint, query)
                .execSelect();

        List<Result> results = SparqlDataReader.convertToTimeLog(rs);

        Map<GroupDate, List<Result>> grouped = results.stream()
                .collect(Collectors.groupingBy(r -> toGroupDay(r)));

        List<TaskType> allTasks = results.stream().map(r -> r.taskType).sorted().distinct().collect(Collectors.toList());
        Map<WPTask, Long> freqs = results.stream().map(r -> toWPTask(r))
                .distinct()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        freqs.keySet().stream().collect(Collectors.groupingBy(e -> e.type, Collectors.counting())).entrySet()
                .stream()
                        .sorted(Comparator.comparingLong((Map.Entry<String, Long> e) -> e.getValue()).reversed())
                        .forEach(e -> System.out.println(String.format("%d, %s", e.getValue(), e.getKey())));

//        extractSequenceUsingRestrictions(results);
//        print(rs);
    }

    public void similarityAnalysis(String endpoint, String outputDir) {
        String query = ResourceUtils.loadResource(SparqlDataReader.DA_TASK_SO_WITH_WP_SCOPE);
        ResultSet rs = QueryExecutionFactory
                .sparqlService(endpoint, query)
                .execSelect();

        List<Result> results = SparqlDataReader.convertToTimeLog(rs);

        Map<String, List<Result>> byWp = new HashMap<>();
        results.stream().collect(Collectors.groupingBy(r->r.wp))
                .entrySet().stream()
                .filter(e -> e.getValue().stream().map(r -> r.taskType).count() > 20)
                .forEach(e -> byWp.put(e.getKey(), e.getValue()));

        results = results.stream().filter(r -> byWp.containsKey(r.wp)).collect(Collectors.toList());

//        List<String> ats = results.stream().filter(r -> r.acType == null).map(r -> r.acmodel).distinct().collect(Collectors.toList());
        List<Pair<String, List<Result>>> byACType = results.stream()
                .collect(Collectors.groupingBy(r -> r.acType, Collectors.toList()))
                .entrySet().stream()
                .map(e -> Pair.of(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        for(Pair<String, List<Result>> p : byACType){
            List<Pair<String, List<Result>>> byScopeGroup = p.getValue().stream()
                    .collect(Collectors.groupingBy(r -> r.scope))
                    .entrySet().stream()
                    .map(e -> Pair.of(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
            String pref = outputDir + p.getKey() + "-";
            similarityAnalysis(p.getValue(), pref);
            for(Pair<String, List<Result>> p2 :  byScopeGroup) {
                pref = outputDir + p.getKey() + "-" + p2.getKey() + "-";
                similarityAnalysis(p2.getValue(), pref);
            }
        }
    }

    protected void similarityAnalysis(List<Result> list, String pref){
        if(list == null || list.isEmpty())
            return; // do create analise output files for empty input list
        calculateWPSimilarities(list, pref + "wp-sim-map");
        wpTaskCardSimMap(list, pref + "tc-sim-map");
        wpCondProbMap(list, pref + "tc-cond-prob-map", pref + "tc-dep-map.png");
    }

    public void wpCondProbMap(List<Result> results, String file, String depMapFile) {
//        List<TaskType> tcs = results.stream().map(r -> r.taskType)
//                .distinct().sorted()
//                .collect(Collectors.toList());
//
//        List<Pair<TaskType, Set<String>>> supportMap = new HashMap<>();
        List<Pair<TaskType, Set<String>>> sup = results.stream().collect(Collectors.groupingBy(r -> r.taskType))
                .entrySet().stream()
                .map(e -> Pair.of(
                        e.getKey(),
                        e.getValue().stream().map(r -> r.wp).collect(Collectors.toSet()))
                ).collect(Collectors.toList());

        double all = sup.size();
        double[][] copr = new double[sup.size()][sup.size()];
        double[][] deps = new double[sup.size()][sup.size()];


        // calculate conditional probability
        for (int i = 0; i < sup.size(); i++) {
            Pair<TaskType, Set<String>> p1 = sup.get(i);
            if (p1.getValue().isEmpty()) {
                System.out.println(String.format("The task type (%s) does not have assigned a package", p1.getKey().getCode()));
                Arrays.fill(copr[i], 0);
                continue; // the result is zero
            }
            for (int j = i + 1; j < sup.size(); j++) {

                Pair<TaskType, Set<String>> p2 = sup.get(j);
                if (p2.getValue().isEmpty())
                    continue;
                Set<String> sup12 = new HashSet(p1.getValue());
                sup12.retainAll(p2.getValue());
                copr[i][j] = (double) sup12.size() / (double) p2.getValue().size();
                copr[j][i] = (double) sup12.size() / (double) p1.getValue().size();
//                if(!sup.isEmpty() ) {
//                    deps[i][j] = (double) sup12.size() / (double) p1.getValue().size();
//                    deps[j][i] = (double) sup12.size() / (double) p2.getValue().size();
//                }

            }
            // normalize dependencies
//            DoubleSummaryStatistics stats = DoubleStream.of(deps[i]).summaryStatistics();
//            double depMax = stats.getMax();
//            for(int j = 0; j < sup.size(); j++) {
//                deps[i][j] = deps[i][j]/depMax;
//            }
//            double depMaxNorm = DoubleStream.of(deps[i]).max().orElse(1);
//            depMax = depMax;
//            PriorityQueue<Double> q = new PriorityQueue<>();
//            for(int j = i + 1; j < sup.size(); j++) {
//                if(q.add(copr[i][j]) && q.size() > 10){
//                    q.
//                }
//
//            }
//            deps[i]
        }

        // csv or copr
        GMatrix gMatrix = new GMatrix(copr);
        Function<Integer, String> taskLabel = i -> sup.get(i).getKey().getCode();
        GMatrix.linearTable(file + ".csv",
                "Task Type", "Task Type", "CondProb",
                taskLabel, taskLabel, gMatrix
                );

        // reorder rows

        VectorSort.sort(copr, (r1, r2) -> -VectorSort.dimMatchDistance(r1, r2, 1000, 1));
        copr = VectorSort.transpose(copr);
        VectorSort.sort(copr, (r1, r2) -> -VectorSort.dimMatchDistance(r1, r2, 100, 1));
        copr = VectorSort.transpose(copr);
        // create image
        Color zero = new Color(1f,1f,1f);
        matrixToFile(file + ".png", new GMatrix(copr), 2, Color.WHITE,
                shade -> shade == 0 ?
                        zero :
                        new Color((float) shade, (float) shade, 1f)
            );


//        Arrays.sort(deps, (r1, r2) -> -VectorSort.dimMatchDistance(r1, r2, 1, 1));
//        deps = VectorSort.transpose(deps);
//        VectorSort.sort(deps, (r1, r2) -> -VectorSort.dimMatchDistance(r1, r2, 100, 1));
//        deps = VectorSort.transpose(deps);
//        saveMap(deps, depMapFile);
    }

//    public void saveMap(double[][] map, String file){
//        int psize = 2;
//        int totalWidth = psize * map[0].length;
//        int totalHeight = psize * map.length;
//        BufferedImage bi = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = bi.createGraphics();
//        g.setBackground(Color.WHITE);
//        g.clearRect(0,0, totalWidth, totalHeight);
//
//        for(int i = 0; i < map.length; i++){
//            for(int j = 0; j < map[0].length; j++){
//                double shade = map[i][j];
//                if(shade == 0)
//                    g.setColor(new Color(1f,1f,1f));
//                else {
//                    try {
//                        g.setColor(new Color((float) shade, (float) shade, 1f));
//                    }catch(IllegalArgumentException e){
//                        e.printStackTrace();
//                    }
//                }
////                if(i == j)
////                    g.setColor(new Color(1f,1f, 1f));
//                g.fillRect(j*psize, i*psize, psize, psize);
//            }
//        }
//
//        try {
//            ImageIO.write(bi, "PNG", new File(file));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public void wpTaskCardSimMap(List<Result> results, String file){
        Map<TaskType, Integer> supportMap = new HashMap<>();
        // supports
        results.stream().collect(Collectors.groupingBy(r -> r.taskType))
                .entrySet()
                .forEach(e -> supportMap.put(e.getKey(), (int)e.getValue().stream().map(r -> r.wp).distinct().count()));
        // ordered taskTypes
        List<TaskType> tcs = results.stream().map(r -> r.taskType)
                .distinct()
                .sorted(Comparator.comparing(t -> supportMap.get(t)).reversed().thenComparing(t -> ((TaskType) t).getCode()))
                .collect(Collectors.toList());
        IntDictionary<TaskType> dict = new IntDictionary<>(tcs);
        List<Pair<String, BitSet>> pkgs = results.stream()
                .collect(Collectors.groupingBy( r -> r.wp)).entrySet().stream()
                .map(e -> Pair.of(
                        e.getKey(),
                        dict.translate(e.getValue().stream().map(r -> r.taskType).collect(Collectors.toList())))
                )
                .collect(Collectors.toList());

        GMatrix first = new GMatrix(
                i -> (j) -> pkgs.get(i).getValue().get(j) ? 1f : 0f,
                new Rectangle(dict.getElements().size(), pkgs.size()));
        GMatrix gMatrix = first.transpose();
        GMatrix.toTable(file + ".csv", i -> pkgs.get(i).getKey(), i -> dict.elements.get(i).getCode(), gMatrix);
// TODO fix ordering
//        pkgs.sort((p1, p2) -> VectorSo/rt.bitsetComp(p1.getValue(), p2.getValue(), 2, 2));

//        // draw similarity map to PNG image
//        int psize = 2;
//        int totalWidth = psize * dict.getElements().size();
//        int totalHeight = psize * pkgs.size();
//
//        BufferedImage bi = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = bi.createGraphics();
//        g.setBackground(Color.WHITE);
//        g.clearRect(0,0, totalWidth, totalHeight);
//
//        for(int i = 0; i < pkgs.size(); i++){
//            BitSet bs = pkgs.get(i).getValue();
//            for(int j = 0; j < pkgs.get(0).getRight().length(); j++){
//                float shade = bs.get(j) ? 1f : 0f;
//                if(shade == 0)
//                    g.setColor(new Color(1f,1f,1f));
//                else
//                    g.setColor(new Color(0f,0f, shade));
////                if(i == j)
////                    g.setColor(new Color(1f,1f, 1f));
//                g.fillRect(j*psize, i*psize, psize, psize);
//            }
//        }

//        int psize = 2;
//        Color zero = new Color(1f,1f,1f);
//        GMatrix gMatrix = new GMatrix(i -> (j) -> pkgs.get(i).getValue().get(j) ? 1f : 0f, new Rectangle(dict.getElements().size(),pkgs.size()));
//        BufferedImage bi = createImage(gMatrix, psize,
//                Color.WHITE, s -> s == 0 ? zero : new Color(0f,0f, s));
//
//
//        try {
//            ImageIO.write(bi, "PNG", new File(file));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        int psize = 2;
        Color zero = new Color(1f,1f,1f);
        gMatrix = new GMatrix(i -> (j) -> pkgs.get(i).getValue().get(j) ? 1f : 0f, new Rectangle(dict.getElements().size(),pkgs.size()));
        matrixToFile(file + ".png", gMatrix, psize, Color.WHITE, s -> s == 0 ? zero : new Color(0f,0f, s));
    }

    public void calculateWPSimilarities(List<Result> results, String file){
        List<Pair<String, Set<String>>> pkgs = toPackageTasks(results);

        double[][] simmap = calculateWPSimilarities(pkgs);

        GMatrix gMatrix = new GMatrix(simmap);
        Function<Integer, String> wpLabel = i -> pkgs.get(i).getKey();
        GMatrix.linearTableNonSymmetric(file + ".csv",
                "WP", "WP", "task set similarity",
                wpLabel, wpLabel,
                gMatrix
                );
//        sort by rows
        VectorSort.sort(simmap, (r1, r2) -> -VectorSort.dimMatchDistance(r1,r2,100, 1));
        simmap = VectorSort.transpose(simmap);
        VectorSort.sort(simmap, (r1, r2) -> -VectorSort.dimMatchDistance(r1,r2,100, 1));

        // draw similarity map to PNG image
//        int psize = 6;
//        int totalWidth = psize*pkgs.size();
//        int totalHeight = psize*pkgs.size();
//        Color zero = new Color(1f,1f,1f);
//        Function<Float, Color> colorFunction = shade -> new Color(0f,0f, shade);
//
//        BufferedImage bi = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = bi.createGraphics();
//        g.setBackground(Color.WHITE);
//        g.clearRect(0,0, totalWidth, totalHeight);
//
//        for(int i = 0; i < simmap.length; i++){
//            for(int j = 0; j < simmap[i].length; j++){
//                float shade = (float) simmap[i][j];
//                if(shade == 0)
//                    g.setColor(new Color(1f,1f,1f));
//                else
//                    g.setColor(new Color(0f,0f, shade));
////                if(i == j)
////                    g.setColor(new Color(1f,1f, 1f));
//                g.fillRect(j*psize, i*psize, psize, psize);
//            }
//        }

//        int psize = 6;
//        Color zero = new Color(1f,1f,1f);
//        GMatrix gMatrix = new GMatrix(simmap);
//        BufferedImage bi = createImage(gMatrix, psize,
//                Color.WHITE, s -> s == 0 ? zero : new Color(0f,0f, s));
//
//
//        try {
//            ImageIO.write(bi, "PNG", new File(file));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        int psize = 6;
        Color zero = new Color(1f,1f,1f);
        gMatrix = new GMatrix(simmap);
        matrixToFile(file + ".png", gMatrix, psize, Color.WHITE, s -> s == 0 ? zero : new Color(0f,0f, s));
    }

    public static List<Pair<String, Set<String>>> toPackageTasks(List<Result> results){
        return results.stream()
                .collect(Collectors.groupingBy( r -> r.wp)).entrySet().stream()
                .map(e -> Pair.of(e.getKey(), e.getValue().stream().map(r -> r.taskType.getCode()).collect(Collectors.toSet())))
                .collect(Collectors.toList());
    }

    public static double[][] calculateWPSimilarities(List<Pair<String, Set<String>>> pkgs){
        double[][] simmap = new double[pkgs.size()][];
        for(int i = 0; i < pkgs.size() ; i++){
            simmap[i] = new double[pkgs.size()];
        }
        for(int i = 0; i < pkgs.size() ; i++){
            Pair<String, Set<String>> p1 = pkgs.get(i);
            simmap[i][i] = 0d;// ignore the self similarity
            for(int j = i + 1; j < pkgs.size(); j++){
                Pair<String, Set<String>> p2 = pkgs.get(j);
//                HashSet<String> and = new HashSet<>(p1.getValue());
//                and.retainAll(p2.getValue());
//                simmap[i][j] = 2d*and.size()/(p1.getValue().size() + p2.getValue().size());
                simmap[i][j] = calculateSetSimilarity(p1.getValue(), p2.getValue());
                simmap[j][i] = simmap[i][j];
            }
        }
        return simmap;
    }

    public static double calculateSetSimilarity(Set<?> p1, Set<?> p2){
        HashSet<?> and = new HashSet<>(p1);
        and.retainAll(p2);
        return 2d*and.size()/(p1.size() + p2.size());
    }

    public static void matrixToFile(String fileName, GMatrix gMatrix, int psize, Color bgc, Function<Float, Color> coloring){
        BufferedImage bi = createImage(gMatrix, psize, bgc, coloring);
        try {
            ImageIO.write(bi, "PNG", new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BufferedImage createImage(GMatrix m, int psize, Color bgc, Function<Float, Color> coloring){
        BufferedImage bi = new BufferedImage((int) m.getWidth(), (int)m.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bi.createGraphics();
        g.setBackground(bgc);
        g.clearRect(0,0, (int) m.getWidth(), (int) m.getHeight());
        m.draw(g, psize, coloring);
//        drawMatrix(g, matrix, psize, coloring);
        return bi;
    }

//    public static void drawMatrix(Graphics2D g, double[][] matrix, int psize, Function<Float, Color> coloring){
//        for(int i = 0; i < matrix.length; i++){
//            for(int j = 0; j < matrix[i].length; j++){
//                float shade = (float) matrix[i][j];
//                g.setColor(coloring.apply(shade));
//                g.fillRect(j*psize, i*psize, psize, psize);
//            }
//        }
//    }
//
//    public static void drawMatrix(Graphics2D g, BiFunction<Integer, Integer, Float> shadeFunction, Rectangle r, int psize, Function<Float, Color> coloring){
//        for(int i = r.y; i < r.y + r.height; i++){
//            for(int j = r.x; j < r.x + r.width; j++){
//                float shade = shadeFunction.apply(i,j);
//                g.setColor(coloring.apply(shade));
//                g.fillRect(j*psize, i*psize, psize, psize);
//            }
//        }
//    }

    public void executeSequenceExtraction(String endpoint, String outputDir, String outputPrefix, Integer supportSize, Float supportPercentage, int minDistanceDays) throws IOException {
        // other parameters
//        Set<String> acModels = AircraftType.types.get("B").getModels(); // new HashSet<>(Arrays.asList("738", "73G", "73H", "73J", "73W", "B735"));
//        Set<String> acModels = Stream.of("738", "73G", "73H", "73J", "73W", "B735").collect(Collectors.toSet());

        // load data from endpoint
        String query = ResourceUtils.loadResource(SparqlDataReader.DA_TASK_SO_WITH_WP_SCOPE);
        ResultSet rs = QueryExecutionFactory
                .sparqlService(endpoint, query)
                .execSelect();

        List<Result> results = SparqlDataReader.convertToTimeLog(rs);
//        results = createArtificialDataByExample(results, 10, Arrays.asList(0.8,0.1), 0);
        long numberOfWPs = results.stream().map(r -> r.wp).distinct().count();

        System.out.println("number of packages in data set : " + numberOfWPs );
        // frequencies of task types
        System.out.println("frequencies of task types");
        results.stream().collect(Collectors.groupingBy(r -> r.taskType, Collectors.counting()))
                .entrySet().forEach(e -> System.out.println(String.format("%s;%d", e.getKey().getCode(), e.getValue())));

        System.out.println();
        System.out.println();
        System.out.println("frequencies of task type categories ");
        results.stream().collect(Collectors.groupingBy(r -> r.taskType.getTaskcat(), Collectors.counting()))
                .entrySet().forEach(e -> System.out.println(String.format("%s;%d", e.getKey(), e.getValue())));

        results = results.stream()
//                .filter(r -> acModels.contains(r.acmodel))
                .collect(Collectors.toList());
        numberOfWPs = results.stream().map(r -> r.wp).distinct().count();
        System.out.println("number of packages in data set : " + numberOfWPs );

        // selec t only one type of ac

        // set parameters for the pattern extraction
//        float supportFraction = ((float) supportSize)/ 100.0f;// 0.6f;
//        long minDistance = 7*24*60*60*1000; // milliseconds
        long minDistance = ((long)minDistanceDays) * 24L * 60 * 60 * 1000; // milliseconds
        Function<Result, Long> orderBy = r -> r.start.getTime();
        Comparator<Result> order = Comparator.comparing(orderBy);
        float support = supportSize != null ?
                supportSize :
                1.0f * supportPercentage * numberOfWPs;

        // filter out task types which do not satisfy the expected support - this helps to reduce the number of tasks which need to be
        Map<TaskType, Long> taskFrequency = results.stream().map(r -> Pair.of(r.taskType, r.wp)).distinct()
                .collect(Collectors.groupingBy(p -> p.getKey(), Collectors.counting()));
        Map<TaskType, Long> rrr = taskFrequency.entrySet().stream().filter(e -> e.getValue() >= support).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
//        rrr.entrySet().stream().sorted(Comparator.comparing(e -> e.getValue())).forEach(r -> System.out.println(r.getValue() + ", " + r.getKey()));

        //

        // TODO - NOTE COMMENTED
//        Map<String, List<Result>> testWPs = new HashMap<>();
//        results.stream()
//                .collect(Collectors.groupingBy(r -> r.wp))
//                .entrySet().stream()
//                .filter(e -> e.getValue().size() > 100)
////                .sorted(Comparator.comparing(e -> -e.getValue().stream().mapToLong(r -> r.start.getTime()).min().getAsLong()))
////                .limit(5)
//                .forEach(e -> testWPs.put(e.getKey(), e.getValue().stream().collect(Collectors.groupingBy(r -> r.taskType))
//                        .entrySet().stream().map(ee -> ee.getValue().stream()
//                                .collect(Collectors.minBy(order)).get()
//                        ).collect(Collectors.toList()))
//                );
////        testWPs.


        // split into WP sequences
        Map<String, List<Result>> sequencess = results.stream()
                // filter sequences select only tasks which have the expected support
//                .filter(r -> !testWPs.containsKey(r.wp)) // TODO - NOTE COMMENTED
//                .filter(r -> rrr.containsKey(r.taskType)) // TODO - NOTE COMMENTED
                .collect(Collectors.groupingBy(r -> r.wp));

        List<String> scopes = results.stream().map(r -> r.scope)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // analyze the merge of all scopes
//        analyzeSequences();

        // analyze sequences per scope

        List<Pair<String, List<Result>>> byACType = results.stream()
                .collect(Collectors.groupingBy(r -> r.acType, Collectors.toList()))
                .entrySet().stream()
                .map(e -> Pair.of(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        for (Pair<String, List<Result>> p : byACType){
            Map<String, List<Result>> byWp = p.getValue().stream().collect(Collectors.groupingBy(r -> r.wp));
            analyzeSequences(byWp, order, support, minDistance, outputDir + outputPrefix + p.getKey() + "-");//, testWPs);

            List<Pair<String, List<Result>>> byScope = p.getValue().stream()
                    .collect(Collectors.groupingBy(r -> r.scope, Collectors.toList()))
                    .entrySet().stream()
                    .map(e -> Pair.of(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());

            for (Pair<String, List<Result>> p2 : byScope) {
                // group by WP
                byWp = p2.getValue().stream().collect(Collectors.groupingBy(r -> r.wp));
                analyzeSequences(byWp, order, support, minDistance, outputDir + outputPrefix + p.getKey() + "-" + p2.getKey() + "-");//, testWPs);
            }
        }

//        for(String scope : scopes) {
//            // filter data to have just one scope
//            Map<String, List<Result>> filtered = new HashMap<>();
//            sequencess.entrySet().forEach(e ->{
//                        List<Result> f = new ArrayList<>();
//                        for(Result r : e.getValue()) {
//                            if (r.scope.equals(scope)) {
//                                f.add(r);
//                            }
//                        }
//                        filtered.put(e.getKey(), f);
//                    }
//            );
//            analyzeSequences(filtered, order, support, minDistance, outputDir + outputPrefix + "-" + scope + "-", testWPs);
//        }

    }


    public void analyzeSequences(Map<String, List<Result>> sequencess,
                                        Comparator<Result> order,
                                        float support,
                                        long minDistance,
                                        String outFilePrefix//,
//                                        Map<String, List<Result>> testWPs
                                        ){

//        List<SequencePattern> supportedPairs = calculateSupportedOrderedPairs(sequencess, numberOfWPs, support, minDistance);
        // select only the first task sessions according to the specified order, make sure the wps tasks are sorted according to the order.
        List<List<Result>> startSequences = sequencess.values().stream().map(l -> l.stream()
//                    .sorted(order)
                    .collect(Collectors.groupingBy(r -> r.taskType))
                    .entrySet().stream().map(e -> e.getValue().stream()
                                    .collect(Collectors.minBy(order)).get()
                    )
                .sorted(order)
                .collect(Collectors.toList())
        ).collect(Collectors.toList());



        // find sequence pattern of length 2 with required support, satisfying the given condition
        TimeSequenceMatrix patternExtractor = new TimeSequenceMatrix();
        List<SequencePattern> supportedPatterns = patternExtractor.execute(startSequences, support,
//                // adjacent sequence patterns
//                true,
//                patternExtractor::preferBiEdgeWithBiggerSupport,
//                (r1, r2) -> true
                // sequence patterns within a time interval
                false,
                patternExtractor::preferBiEdgeWithBiggerSupport,
                (r1, r2) ->
                        Math.abs(r2.start.getTime() - r1.start.getTime()) < minDistance
//                                &&
//                        r1.scope.equals(r2.scope)
        );

        if(supportedPatterns.isEmpty())
            return; // do not create empty files


        SequencePattern.calculateSupportClasses(supportedPatterns);



//        Set<SequencePattern> processedByScope = new HashSet<>();
//        for(String scope : scopes) {
//            List<SequencePattern> list = supportedPatterns.stream().filter(p -> {
//                Set<String> s = p.instances.stream().flatMap(l -> l.stream().map(r -> r.scope)).collect(Collectors.toSet());
//                return s.size() == 1 && s.contains(scope);
//            }).collect(Collectors.toList());
//            processedByScope.addAll(list);
//        }
//        Set<SequencePattern> notProcessedByScope = new HashSet<>(supportedPatterns);
//        notProcessedByScope.removeAll(processedByScope);
//        if(!notProcessedByScope.isEmpty()) {
//            System.out.println("ERRROROR");
//        }

//        }
//        if(true)
//            return;
//        String scope

        // Print no conflict sequence patterns
        List<SequencePattern> nonConflictSupportedPatterns = supportedPatterns.stream()
                .filter(sp -> sp.reverse == null).collect(Collectors.toList());
//        if(!nonConflictSupportedPatterns.isEmpty()) {
//            FilterTransitiveEdgesAlg p = FilterTransitiveEdgesAlg.filterTransitiveEdges(nonConflictSupportedPatterns);
//            if (!p.filteredSequencePatterns.isEmpty()) {
//                ToCSV.writeEdges(p.filteredSequencePatterns, outFilePrefix + "task-noconflict-seqpat-patterns.csv");
//                writeAsGraphml(p.filteredSequencePatterns, outFilePrefix + "task-noconflict-seqpat.graphml");
//            }
//        }

        FilterTransitiveEdgesAlg p = FilterTransitiveEdgesAlg.filterTransitiveEdges(supportedPatterns);

        nonConflictSupportedPatterns = p.filteredSequencePatterns.stream()
                .filter(sp -> sp.reverse == null).collect(Collectors.toList());
        if(!nonConflictSupportedPatterns.isEmpty()) {
            FilterTransitiveEdgesAlg p2 = FilterTransitiveEdgesAlg.filterTransitiveEdges(nonConflictSupportedPatterns);
            if (!p2.filteredSequencePatterns.isEmpty()) {
                ToCSV.writeEdges(p2.filteredSequencePatterns, outFilePrefix + "task-noconflict-2-seqpat-patterns.csv");
                writeAsGraphml(p2.filteredSequencePatterns, outFilePrefix + "task-noconflict-2-seqpat.graphml");
            }
        }

        // TODO - NOTE COMMENTED
//        // order test work packages
//        Map<String, List<SequencePattern>> plans = new HashMap<>();
//        for(Map.Entry<String, List<Result>> wp : testWPs.entrySet()){
//            Set<TaskType> tts = wp.getValue().stream().map(r -> r.taskType).collect(Collectors.toSet());
//            List<SequencePattern> plan = p.filteredSequencePatterns.stream()
//                    .filter(s -> tts.containsAll(s.pattern)).collect(Collectors.toList());
//            plans.put(wp.getKey(), plan);
//            FilterTransitiveEdgesAlg pp = FilterTransitiveEdgesAlg.filterTransitiveEdges(supportedPatterns);
//            writeAsGraphml(pp.filteredSequencePatterns, outFilePrefix + wp.getKey().replaceAll("[\\/]", "_") + "-plan.graphml");
//        }

//        Pair<List<SequencePattern>, FeedbackEdgeSetResult<TaskType, SequencePattern>> p = FilterTransitiveEdgesAlg.filterTransitiveEdges(supportedPatterns);


        List<SequencePattern> filteredSupportedPatterns = p.filteredSequencePatterns;

        if(!filteredSupportedPatterns.isEmpty()) {
            ToCSV.writeEdges(filteredSupportedPatterns, outFilePrefix + "task-seqpat-patterns.csv");
            ToCSV.writeSupportInstances(filteredSupportedPatterns, outFilePrefix + "task-seqpat-instances.csv");
            writeAsGraphml(filteredSupportedPatterns, outFilePrefix + "task-seqpat.graphml");
            // export support classes
        }
        if(!filteredSupportedPatterns.isEmpty() || !nonConflictSupportedPatterns.isEmpty())
            ToCSV.writeSupportClasses(outFilePrefix + "task-seqpat-support-classes.csv", filteredSupportedPatterns, nonConflictSupportedPatterns);
//        int i = 0;
//        for(Set<SequencePattern> eqClass : SequencePattern.supportEquivalenceGraphs(supportedPatterns)){
//            ToGraphml.writePatterns(asGraph(eqClass), outFilePrefix + "-seqpat-eqClass-" + i + ".graphml");
//            i++;
//        }


        // export cycles which were processed
        if(!p.feedbackEdgeSet.getCycles().isEmpty()) {
            ToCSV.writePaths(p.feedbackEdgeSet.getCycles(), outFilePrefix + "task-seqpat-found-cycles.csv");
            ToCSV.writePathsPerRow(p.feedbackEdgeSet.getCycles(), outFilePrefix + "task-seqpat-found-cycles-per-row.csv");
        }
//        i = 0;
//        for(GraphPath<TaskType, SequencePattern> cycle: p.feedbackEdgeSet.getCycles()){
//            ToGraphml.writePatterns(asGraph(cycle.getEdgeList()), outFilePrefix + "-task-seqpat-foudn-cycle-" + i + ".graphml");
//            i++;
//        }


        // export the cycles in the finx    al pattern graph. The cycles represent the conflicting patterns found in
        // different support equivalence-classes
        List<GraphPath<TaskType,SequencePattern>> cycles = JGraphTUtils.findDirectedCycles(asGraph(filteredSupportedPatterns));
        if(!cycles.isEmpty()) {
            ToCSV.writePaths(cycles, outFilePrefix + "task-seqpat-cycles.csv");
            ToCSV.writePathsPerRow(cycles, outFilePrefix + "task-seqpat-cycles-per-row.csv");
        }
//        i = 0;
//        for(GraphPath<TaskType, SequencePattern> cycle: cycles){
//            ToGraphml.writePatterns(asGraph(cycle.getEdgeList()), outFilePrefix + "-task-seqpat-cycle-" + i + ".graphml");
//            i++;
//        }

//        // print pattern edges
        System.out.println(String.format("found %d supported sequence patterns.", filteredSupportedPatterns.size()));
    }



    public void extractNestedTaskPatterns(String endpoint, String outputDir, String outputPrefix, int supportPercentage){
        String query = ResourceUtils.loadResource(SparqlDataReader.DA_TASK_SO_WITH_WP_SCOPE);
        ResultSet rs = QueryExecutionFactory
                .sparqlService(endpoint, query)
                .execSelect();

        List<Result> results = SparqlDataReader.convertToTimeLog(rs);
        float supportFraction = ((float)supportPercentage)/100.0f;
        long numberOfWPs = results.stream().map(r -> r.wp).distinct().count();
        float support = supportFraction*numberOfWPs;

        Map<TaskType, Long> rrr = results.stream().map(r -> Pair.of(r.taskType, r.wp)).distinct()
                .collect(Collectors.groupingBy(p -> p.getKey(), Collectors.counting()))
                .entrySet().stream().filter(e -> e.getValue() >= support)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
        rrr.entrySet().stream().sorted(Comparator.comparing(e -> e.getValue()))
                .forEach(r -> System.out.println(r.getValue() + ", " + r.getKey()));

        // split into WP sequences
        Map<String, List<Result>> sessionSequences = results.stream()
                // filter sequences select only tasks which have the expected support
                .filter(r -> rrr.containsKey(r.taskType))
                .collect(Collectors.groupingBy(r -> r.wp));


        // find nesting pattern of length 2 with required support
//        List<SequencePattern> supportedPairs = calculateSupportedOrderedPairs(sequencess, numberOfWPs, support, minDistance);
        // construct task sequence, fix end times and durations
        Map<String, List<Result>> taskSequences = new HashMap<>();
        for(Map.Entry<String, List<Result>> e : sessionSequences.entrySet()){
            List<Result> s = new ArrayList<>();
            taskSequences.put(e.getKey(), s);
            List<Result> seq = e.getValue();
            Map<TaskType, Result> visitedTasks = new HashMap<>();
            seq.sort(Comparator.comparing(r -> r.start.getTime()));
            for(Result r : seq){
                Result nr = visitedTasks.get(r.taskType);
                if(nr == null){
                    nr = new Result(r);
                    visitedTasks.put(nr.taskType, nr);
                    s.add(nr);
                }else{
                    nr.end = r.end;
                    nr.dur += r.dur;
                }
            }
        }
        TimeSequenceMatrix patternExtractor = new TimeSequenceMatrix();
        List<SequencePattern> supportedPatterns = patternExtractor.execute(
                new ArrayList<>(taskSequences.values()),
                support,
                false,
                patternExtractor::preferBiEdgeWithBiggerSupport,
                (r1, r2) ->
                        r1.start.getTime() < r2.start.getTime() && r1.end.getTime() > r2.end.getTime() &&
                        r1.scope.equals(r2.scope)
        );

        SequencePattern.calculateSupportClasses(supportedPatterns);
        FilterTransitiveEdgesAlg filterEdgeResult = FilterTransitiveEdgesAlg.filterTransitiveEdges(supportedPatterns);
        supportedPatterns = filterEdgeResult.filteredSequencePatterns;

        DefaultDirectedGraph<String, String> g = ToGraphml.toStringGraph(supportedPatterns);
        DefaultDirectedGraph<String, String> gNoCommonTasks = ToGraphml.toStringGraph(supportedPatterns);
        DefaultDirectedGraph<String, String> gOnlyCommonTasks = ToGraphml.toStringGraph(supportedPatterns);



        while(true){
               List<String> toRemove = gNoCommonTasks.vertexSet().stream()
//                    .sorted(Comparator.comparing(gCommonTasks::inDegreeOf).thenComparing(gCommonTasks::outDegreeOf))
                    .filter(v -> gNoCommonTasks.inDegreeOf(v) > 1 || (gNoCommonTasks.inDegreeOf(v) == 0 && gNoCommonTasks.outDegreeOf(v) == 0))
                    .collect(Collectors.toList());
            if(toRemove.isEmpty())
                break;
            gNoCommonTasks.removeAllVertices(toRemove);
        }

        while(true){
            List<String> toRemove = gOnlyCommonTasks.vertexSet().stream()
                    .sorted(Comparator.comparing(gOnlyCommonTasks::inDegreeOf).thenComparing(gOnlyCommonTasks::outDegreeOf))
                    .filter(v -> gOnlyCommonTasks.inDegreeOf(v) < 2 && gOnlyCommonTasks.outDegreeOf(v) == 0).collect(Collectors.toList());
            if(toRemove.isEmpty())
                break;
            gOnlyCommonTasks.removeAllVertices(toRemove);
        }

        // construct graph which shows which tasks have common nested tasks
        SimpleGraph<String, String> gWithCommonTasks = new SimpleGraph<>(() -> new String(), () -> new String(), true);
        for(String v : gOnlyCommonTasks.vertexSet()){
            List<String> es = new ArrayList<>(gOnlyCommonTasks.incomingEdgesOf(v));
            for(int i = 0 ; i < es.size() - 1; i ++){
                String v1 = gOnlyCommonTasks.getEdgeSource(es.get(i));
                gWithCommonTasks.addVertex(v1);
                for(int j = i + 1 ; j < es.size(); j ++){
                    String v2 = gOnlyCommonTasks.getEdgeSource(es.get(j));
                    String e = Stream.of(v1, v2).sorted().collect(Collectors.joining("-"));
                    gWithCommonTasks.addVertex(v2);
                    if(gWithCommonTasks.addEdge(v1, v2, e))
                        gWithCommonTasks.setEdgeWeight(e, 1);
                    else
                        gWithCommonTasks.setEdgeWeight(e, gWithCommonTasks.getEdgeWeight(e) + 1);
                }
            }
        }

        // print pattern edges
        ToCSV.writeEdges(supportedPatterns, outputDir + outputPrefix + "-nested-task-patterns-edges.csv");
        ToCSV.writeSupportInstances(supportedPatterns, outputDir + outputPrefix + "-nested-task-patterns-nodes.csv");
        ToGraphml.write(g, outputDir + outputPrefix + "-nested-task-patterns.graphml");
        ToGraphml.write(gNoCommonTasks, outputDir + outputPrefix + "-nested-task-patterns-no-common-tasks.graphml");
        ToGraphml.write(gOnlyCommonTasks, outputDir + outputPrefix + "-nested-task-patterns-common-tasks.graphml");
        ToGraphml.write(gWithCommonTasks, outputDir + outputPrefix + "-nested-task-patterns-with-common-tasks.graphml");

        System.out.println(String.format("found %d supported nested task patterns.", supportedPatterns.size()));
    }

    public void temporalMaps(String endpoint, String outputDir, String outputPrefix) throws IOException {
            // load data from endpoint
            String query = ResourceUtils.loadResource(SparqlDataReader.ALL_TASKS);
            ResultSet rs = QueryExecutionFactory
                    .sparqlService(endpoint, query)
                    .execSelect();
            List<Result> results = SparqlDataReader.convertToTimeLog(rs);
            long numberOfWPs = results.stream().map(r -> r.wp).distinct().count();

            // set parameters for the pattern extraction
            float supportFraction = 0.6f;
////        long minDistance = 7*24*60*60*1000; // milliseconds
//            long minDistance = 1*24*60*60*1000; // milliseconds
            Function<Result, Long> orderBy = r -> r.start.getTime();
            Comparator<Result> order = Comparator.comparing(orderBy);

            float support = supportFraction*numberOfWPs;
            results.stream()
                    .collect(Collectors.groupingBy(r -> SparqlDataReader.day.format(r.start), Collectors.counting()))
                    .entrySet()
                    .stream().sorted(Comparator.comparing(e -> e.getKey()))
                    .forEach(e -> System.out.println(e.getKey() + ";" +e.getValue()));





        TimeDiagram td = new TimeDiagram();
        td.createImage(results, outputDir + outputPrefix + "-time-diagram", td::drawByDay);
        results.stream().collect(Collectors.groupingBy(r -> r.wp)).entrySet().forEach(e ->
        {
            e.getValue().sort(Comparator.comparing(r -> r.start));
            td.createImage(e.getValue(), outputDir + outputPrefix + "-" + e.getKey().replaceAll("[\\\\/]", "_"), td::drawByDay);
        });
    }


    protected void writeAsGraphml(Collection<SequencePattern> patterns, String fileName){
        ToGraphml.writePatterns(asGraph(patterns), fileName);
    }

    public List<SequencePattern> calculateSupportedOrderedPairs(Map<String, List<Result>> sequencess, long numberOfWPs, float support, long maximumduration){
        List<Map.Entry<String,List<Result>>> ss = new ArrayList<>(sequencess.entrySet());
        ss.forEach(l -> l.getValue().sort(Comparator.comparing(r -> r.start)));

        Function<Result, TaskType> patternFunction = r -> new TaskType(r.taskType);

        Map<String, SequencePattern> foundPatterns = new HashMap<>();
        for (int i = 0; i < numberOfWPs - support  ; i ++){
            List<Result> wpi = ss.get(i).getValue();
            List<SequencePattern> newPatterns = new ArrayList<>();
            // generate pairs
            Set<TaskType> usedTypes = new HashSet<>();
            System.out.println();
            for(int j1 = 0; j1 < wpi.size() - 1; j1 ++){
                Result r1 = wpi.get(j1);
                if(usedTypes.contains(r1.taskType))
                    continue;
                usedTypes.add(r1.taskType);
                for(int j2 = j1 + 1; j2 < wpi.size(); j2 ++){
                    Result r2 = wpi.get(j2);
                    if(usedTypes.contains(r2.taskType))
                        continue;
//                    usedTypes.add(r2.type);
                    // additional conditions
                    // r1.date.equals(r2.date) && // starting the same day not just within a given duration between the tasks, e.g. 24 hours
                    // r1.scope.equals(r2.scope) && // same group
                    if(!r2.taskType.equals(r1.taskType) &&  r1.start.before(r2.start) && r2.start.getTime() - r1.start.getTime() < maximumduration){
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

    public static DefaultDirectedWeightedGraph<TaskType, SequencePattern> asGraph(Collection<SequencePattern> patterns){
        DefaultDirectedWeightedGraph<TaskType, SequencePattern> g = new DefaultDirectedWeightedGraph<>(SequencePattern.class);
        for(SequencePattern sp : patterns){
            for(TaskType v : sp.pattern){
                if(!g.containsVertex(v)){
                    g.addVertex(v);
                }
            }
            g.addEdge(sp.pattern.get(0), sp.pattern.get(1), sp);
            g.setEdgeWeight(sp, sp.instances.size());
        }
        return g;
    }


    /**
     * Remove edges to break strong connectivity before we remove transitive edges.
     * In each strongly connected component
     * 	- find the minimal edge set which when removed it breaks the strong connectivity in the component.
     * 	- for each edge find all paths from the target to the source. If there are any paths found remove the edge from the connected component.
     */
    public static Set<SequencePattern> findMinimalStrongConnectiveEdges(DefaultDirectedGraph<String, SequencePattern> g){
        KosarajuStrongConnectivityInspector<String, SequencePattern> alg = new KosarajuStrongConnectivityInspector<>(g);
        AllDirectedPaths<String, SequencePattern> pathsAlg = new AllDirectedPaths<>(g);


        // find the edges to be removed
        List<String[]> foundEdges = new ArrayList<>();
//        for(Set<String> nodes: alg.stronglyConnectedSets()){
////            Graph<String, SequencePattern> scg = new AsSubgraph<String, SequencePattern>(g, nodes);
////            CycleDetector<String, SequencePattern> cd = new CycleDetector<>(scg);
//
//            for(String sn : nodes ){
//                for(SequencePattern p : g.outgoingEdgesOf(sn)){
//                    String tn = g.getEdgeTarget(p);
//                    List<GraphPath<String, SequencePattern>> paths = pathsAlg.getAllPaths(tn, sn, true, Integer.MAX_VALUE);
//                    if(!paths.isEmpty()){
//                        foundEdges.add(new String[]{sn, tn});
//                    }
//                }
//            }
//        }

        Set<SequencePattern> breakingEdges = new HashSet<>();
//        for(Set<String> nodes: alg.stronglyConnectedSets()){
        for(Graph<String, SequencePattern> comp: alg.getStronglyConnectedComponents()){

//            Graph<String, SequencePattern> scg = new AsSubgraph<String, SequencePattern>(g, nodes);
//            CycleDetector<String, SequencePattern> cd = new CycleDetector<>(scg);
//            if(comps.vertexSet().size() > 1){

            // find the cycles in each component
            Set<SequencePattern> es = new HashSet<>(comp.edgeSet());
            Set<Set<SequencePattern>> cycles = new HashSet<>();

            while(!es.isEmpty()) {
                // for edge es with source and target nodes find the paths which connect target with source
                SequencePattern e = es.iterator().next();
                String s = comp.getEdgeSource(e);
                String t = comp.getEdgeTarget(e);
                List<GraphPath<String, SequencePattern>> paths = pathsAlg
                        .getAllPaths(t, s, true, Integer.MAX_VALUE);
                // construct cycles with found paths and edge e.
                paths.stream()
                        .filter(p -> p.getEdgeList().size() > 1)
                        .map(p -> new HashSet<>(p.getEdgeList()))
                        .forEach(c -> {
                            c.add(e);
//                            c.add(es);
                            if (es.removeAll(c))
                                cycles.add(c);
                        });
            }

            // order the edges along the number of cycles to which the edge belongs
            Map<SequencePattern, Set<Set<SequencePattern>>> edgeInCycles = new HashMap<>();
            cycles.stream().forEach(c -> c.stream().forEach(ee -> edgeInCycles.put(ee, new HashSet<>())));
            cycles.stream().forEach(c -> c.stream().forEach(ee -> edgeInCycles.get(ee).add(c)));
            List<SequencePattern> edges = new ArrayList(edgeInCycles.keySet());
            edges.sort(Comparator.comparing(ee -> edgeInCycles.get(ee).size()).reversed());

            // remove edges according to the new order util there is no cycle for which an edge was not removed
            Set<Set<SequencePattern>> brokenCycles = new HashSet<>();
            Set<SequencePattern> feedbackEdgeSet = new HashSet<>();
            while(brokenCycles.size() < cycles.size()) {
                Iterator<SequencePattern> iter = edges.iterator();
//                if(!iter.hasNext()) // this should never happen
//                    break;

                SequencePattern e = iter.next();
                // break cycles, remember broken cycles in brokenCycles and remove all
                Set<Set<SequencePattern>> cs = edgeInCycles.get(e);
                if(brokenCycles.addAll(cs)) {
                    cs.forEach(edges::removeAll);
                    feedbackEdgeSet.add(e);
                }
            }

//                paths.forEach(g -> g.getEdgeList());
//
//                Map<SequencePattern, Set<GraphPath<String, SequencePattern>>> edgeParticipation = new HashMap<>();
//                for(String n : nodes){
//                    g.outgoingEdgesOf(n).forEach(e -> edgeParticipation.put(e, new HashSet<>()));
//                }
//                for(GraphPath<String, SequencePattern> p : paths){
//                    for(SequencePattern edge : p.getEdgeList()){
//                        edgeParticipation.get(edge).add(p);
//                    }
//                }
//                // select the edges which will break all the cycles
//                List<Map.Entry<SequencePattern, Set<GraphPath<String, SequencePattern>>>> list = edgeParticipation
//                        .entrySet().stream().collect(Collectors.toList());
//                list.sort(Comparator.comparingInt(e -> e.getValue().size()));
//                Set<GraphPath<String, SequencePattern>> brokenCycles = new HashSet<>();
//
//                // TODO - the following algorithm does not select the minimal set of edges which will break all the cycles. Fix the algorithm
//                for(int i = list.size() - 1; i > -1; i --){
//                    Map.Entry<SequencePattern, Set<GraphPath<String, SequencePattern>>> ep = list.get(i);
//                    if(brokenCycles.addAll(ep.getValue())){
//                        breakingEdges.add(ep.getKey());
//                    }
//                }
//            }
        }

        return breakingEdges;
        // remove the found edges
//        System.out.println("Minimal edges which break strong connectivity");
//        for(String[] edge : foundEdges){
//            g.removeEdge(edge[0],edge[1]);
//
//        }
    }

    public static void checkTransitiveReduction(DefaultDirectedGraph<String, SequencePattern> g, DefaultDirectedGraph<String, SequencePattern> gr){
        AllDirectedPaths<String, SequencePattern> alg= new AllDirectedPaths<>(gr);
        // find edges in g for which there is no path in in gr
        System.out.println("edges which there is no path in reduced graph");
        System.out.println("source; target");
        for(SequencePattern sp : g.edgeSet()){
            String sn = g.getEdgeSource(sp);
            String tn = g.getEdgeTarget(sp);
            if(gr.getEdge(sn, tn) != null)
                continue;
            List<GraphPath<String, SequencePattern>> paths = alg.getAllPaths(sn, tn, true, Integer.MAX_VALUE);
            if(paths.isEmpty()){
                System.out.println(String.format("%s; %s", sn, tn));
            }
        }

        System.out.println("##########################################");
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


        Set<List<TaskType>> pairs = new HashSet<>();
        Set<TaskType> visitedType = new HashSet<>();
        Set<String> visitedWpAndType = new HashSet<>();
        Map<String, String> directEdgeCondition = new HashMap<>();
        for(int i = 0; i < results.size() - 1; i ++){
            Result r1 = results.get(i);
//            if(visitedType.contains(r1.type))
            String wpAndType = r1.wp + r1.taskType.getCode();
            if(!visitedWpAndType.add(wpAndType))
                continue;

//            visitedWpAndType.add(r1.wp + r1.type);
            visitedType.add(r1.taskType);
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
                        !visitedType.contains(r2.taskType) &&
                        !r1.taskType.equals(r2.taskType)
                ){
                    if(start2 == null)
                        start2 = r2.start;
                     List<TaskType> l = Arrays.asList(r1.taskType, r2.taskType);
//                    List<String> rl = Arrays.asList(r2.type, r1.type);
                    pairs.add(l);
                }
            }
        }
        System.out.println("edges");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("------------------------------------------------------------------------------------------");
        System.out.println("t1,t2");
        pairs.forEach(l -> System.out.println(l.stream().map(t -> t.getCode()).collect(Collectors.joining(","))));
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


    public static List<Result> createArtificialDataByExample(List<Result> in, int size, Collection<Double> probabilities){
        return createArtificialDataByExample(in, size, probabilities, 0);
    }
    public static List<Result> createArtificialDataByExample(List<Result> in, int size, Collection<Double> probabilities, long seed){
        Map<String, List<Result>> sequences = in.stream()
                .collect(Collectors.groupingBy(r -> r.wp));

        List<String> wps = sequences.keySet().stream().sorted().collect(Collectors.toList());
        List<Double> ps = new ArrayList<>(probabilities);

        // distribution function
        for(int i = 1; i < ps.size(); i ++){
            ps.set(i, ps.get(i-1) + ps.get(i));
        }
        ps.add(1.);

        Map<String, Double> probs = new HashMap<>();
        for(int i = 1; i < wps.size(); i++){
            probs.put(wps.get(i), ps.get(i));
        }


        List<Result> out = new ArrayList<>(size);
        Random r = new Random(seed);
        for(int i = 0; i < size; i ++){
            double un  = r.nextDouble();
            int wpIndex = 0;
            for(int j = 0; j < ps.size() - 1; j++){
                if(un > ps.get(j) && ps.get(j+1) > un)
                    wpIndex = j;
            }
            if(wpIndex  < in.size()){
                // copy a new example
                String wp = wps.get(wpIndex);
                String wpName = wp + "-copy-" + i;
                List<Result> newWP = sequences.get(wp).stream()
                        .map(res -> new Result(res))
                        .collect(Collectors.toList());
                newWP.forEach(res -> res.wp = wpName);
                out.addAll(newWP);
            }
        }
        return out;
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
            type = r.taskType.getCode();
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
                sp.pattern = Stream.of(i + "", j + "").map(t -> new TaskType(t, null)).collect(Collectors.toList());
                ps.add(sp);
            }
        }
//        SequencePattern sp = new SequencePattern();
//        sp.pattern = Arrays.asList("1","0");
//        ps.add(sp);
        FilterTransitiveEdgesAlg res = FilterTransitiveEdgesAlg.filterTransitiveEdges(ps);
        List<SequencePattern> reduced = res.filteredSequencePatterns;
    }

    public static void testDateTimeOrderingAndDistance(){
        String date1 = "2017-05-07T16:00:00";
        String date2 = "2017-05-11T14:45:00";
        long minDistance = 1*24*60*60*1000;
        try {
            Date d1 = SparqlDataReader.df.parse(date1);
            Date d2 = SparqlDataReader.df.parse(date2);
            System.out.println(d2.getTime() - d1.getTime());
            System.out.println(minDistance);
            System.out.println(d2.getTime() - d1.getTime() > minDistance);
            System.out.println(d2.getTime() - d1.getTime() < minDistance);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void expDate(){
        long l = 1494108000000L;
        Date d = new Date(l);

        System.out.println(d);
        System.out.println(d.toInstant());
        String dateStr = "2017-05-07T16:00:00+0200";

        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
        try {
            Date d1 = sf.parse(dateStr);
            System.out.println(d1);
            System.out.println(d1.toInstant());
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) throws IOException {
        System.out.println(new Date());
//        testTransitiveReduction();
//        String endpoint = "http://localhost:7200/repositories/doprava-2020-csat-example-data";
        String endpoint = "http://localhost:7200/repositories/csat-data-01";
        String root = "c:\\Users\\kostobog\\Documents\\skola\\projects\\2019-CSAT-doprava-2020\\code\\aircraft-maintenance-planning-system\\aircraft-maintenance-planning-model\\example-instance-data\\seqpats-scope\\";
        String outputFolder = root + "d01-17-20-basic-b\\";
//        String outputFolder = root + "d01-17-20\\";
//        new ExtractData().execute("http://localhost:7200/repositories/doprava-2020-csat-example-data");
        File outDir = new File(outputFolder);
        if(!outDir.exists()){
            LOG.info("creating output folder \"{}\"", outDir.getAbsolutePath() );
            outDir.mkdirs();
        }

        new ExtractData().executeSequenceExtraction(endpoint, outputFolder,"", 1, null,365);
        new ExtractData().similarityAnalysis(endpoint, outputFolder);
//        new ExtractData().extractNestedTaskPatterns(endpoint, outputFolder,"test-data", 60);
//        new ExtractData().temporalMaps(endpoint, outputFolder,"test-data");
//        testDateTimeOrderingAndDistance();


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
