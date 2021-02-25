package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences;

import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import cz.cvut.kbss.amaplas.exp.common.Checker;
import cz.cvut.kbss.amaplas.exp.common.ResourceUtils;
import cz.cvut.kbss.amaplas.exp.dataanalysis.io.CSVDataReader;
import cz.cvut.kbss.amaplas.exp.dataanalysis.io.SparqlDataReader;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.diff.SequencePatternDiff;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.*;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.planners.OriginalPlanner;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.planners.ReuseBasedPlanner;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.seqalg.FilterTransitiveEdgesAlg;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PlannerExperiments extends ExtractData{
    private static final Logger LOG = LoggerFactory.getLogger(PlannerExperiments.class);
    
//    public static final String DA_TASK_SO_WITH_WP_SCOPE = "/queries/analysis/task-start-order-with-wp-and-scope.sparql";
    public static String endpoint = "http://localhost:7200/repositories/csat-data-01";

    protected ByteArrayOutputStream planReportByteOS = new ByteArrayOutputStream();
    protected PrintStream planReports = new PrintStream(planReportByteOS);

    protected Map<String, Set<TaskType>> plannedByScope = new HashMap<>();

    public void buildPlan(String inputDataFile, String outputDir) {
//        String query = ResourceUtils.loadResource(SparqlDataReader.DA_TASK_SO_WITH_WP_SCOPE);
//        ResultSet rs = QueryExecutionFactory
//                .sparqlService(endpoint, query)
//                .execSelect();
//
//        List<Result> results = SparqlDataReader.convertToTimeLog(rs);
        List<Result> results = new CSVDataReader().readData(inputDataFile).stream()
                .filter(r -> r.scope != null && Scope.isPriority(r.scope) && "TC".equals(r.taskType.taskcat)) // filter loaded data
                .collect(Collectors.toList());
        Set<String> scopes = results.stream().map(r -> r.scope)
                .filter(s -> s!= null).collect(Collectors.toSet());

        List<String[]> stats = calculateTaskTypeScopeStats(results);
        writeCSV(stats, outputDir + "taskType-scope-stats.csv");
        stats = calculateTaskTypeScopeStatsPerWP(results);
        writeCSV(stats, outputDir + "wp-taskType-scope-stats.csv");

        Map<TaskType, String> taskScopeMap = findBestScopeForTaskTypes(results);
        Map<String, Set<TaskType>> scopeTaskMap = new HashMap<>();
                taskScopeMap.entrySet().stream()
                .collect(Collectors.groupingBy(e -> e.getValue(), Collectors.toSet())).entrySet()
                .forEach(e -> scopeTaskMap.put(e.getKey(), e.getValue().stream().map(ee -> ee.getKey()).collect(Collectors.toSet())));
        writeMap(taskScopeMap, outputDir + "taskScopeMap.csv", "task type", "scope");

        // order
        Function<Result, Long> orderBy = r -> r.start.getTime();
        Comparator<Result> order = Comparator.comparing(orderBy);
        long minDistance = ((long) 2) * 24L * 60 * 60 * 1000; // milliseconds

        Map<String, List<Result>> plans = results.stream()
                .collect(Collectors.groupingBy(r -> r.wp));


        // select test wp to plan
        Pair<String, List<Result>> testPlan = selectPlan(plans);
        List<Result> testWP = testPlan.getValue();
        Set<String> testWPScopes = testWP.stream().map(r -> r.scope).collect(Collectors.toSet());
        Set<String> commonScopes = scopes.stream().filter(s -> testWPScopes.contains(s)).collect(Collectors.toSet());;

//        List<Result> testWP = plans.get(testPlan.getKey());
        System.out.println("work package selected for planning test - " + testPlan.getKey());
        System.out.println("number of work logs in the selected WP - " + testWP.size());
        System.out.println("with number of different task types performed by different scope groups - " + testWP.stream().collect(Collectors.groupingBy(r -> new TaskScopeType(r.taskType, r.scope))).size());
        System.out.println(String.format("number of scopes groups in history %d", scopes.size()));
        System.out.println(String.format("number of scopes groups in chosen test WP %d", testWPScopes.size()));
        System.out.println(String.format("number of scopes common for history and WP %d", commonScopes.size()));
//        System.out.println("with number of different task types performed by different scope groups - " + testPlan.getValue().size());
        Set<TaskType> taskTypes = testPlan.getValue().stream().map(r -> r.taskType).collect(Collectors.toSet());
        System.out.println("with number of different task types " + taskTypes.size());
        // remove the chosen wp to be planned from the history.
        plans.remove(testPlan.getKey());
        Set<TaskType> unsupportedTasks = getUnsupportedTasks(plans, taskTypes);
        System.out.println("number of unsupported tasks - " + unsupportedTasks.size());

        String wpFN = testPlan.getKey().replaceAll("[\\/+ ]", "_");

        // plan just tasks
        Map<String, List<Result>> taskStartPlans = historyPlans(plans, orderBy, r -> r.taskType);
        planExperiment(taskStartPlans, testPlan, testWP, taskTypes, outputDir, "");

        // do the same for each scope in history
        taskStartPlans = historyPlans(plans, orderBy, r -> r.taskType.type + r.scope);
        planExperiment(taskStartPlans, testPlan, testWP, taskTypes, outputDir, "");


        // plan separate scopes separately
        Set<String> nonPlannedScopes = new HashSet<>();
        for(String scope : scopes){
            Map<String, List<Result>> taskScopeStartPlans = new HashMap<>();
            Set<TaskType> typeSetPerScope = new HashSet<>(taskTypes);
            typeSetPerScope.retainAll(scopeTaskMap.get(scope));
            if(typeSetPerScope == null)
                continue;
            taskStartPlans.entrySet().stream().map(e -> Pair.of(
                    e.getKey(),
                    e.getValue().stream().filter(r -> scope.equals(r.scope) && typeSetPerScope.contains(r.taskType)).collect(Collectors.toList())
            )).filter(p -> !p.getValue().isEmpty()).forEach(p -> taskScopeStartPlans.put(p.getKey(), p.getValue()));

//            boolean ret = planExperiment(taskScopeStartPlans, testPlan, testWP, taskTypes, outputDir, scope + "-");
            boolean ret = planExperiment(taskScopeStartPlans, testPlan, testWP, typeSetPerScope, outputDir, scope + "-");
            if(!ret)
                nonPlannedScopes.add(scope);
        }

        planReports.println(String.format("By scope planning has planned %d from total %d",
                plannedByScope.values().stream().mapToInt(Set::size).sum(),
                taskTypes.size())
        );



//        Map<String, List<Result>> taskScopeStartPlans = planHistory(plans, order, r -> r.taskType);


//        Map<String, List<Result>> taskScopeStartPlans = new HashMap<>();
//
//        // Select only work log events which correspond to task starts in their respective WPs.
//        plans.entrySet().stream()
//                // leave only starts of different tasks types executed by different scope groups in each plan
//                .map(e -> Pair.of(e.getKey(), e.getValue().stream()
////                                .collect(Collectors.groupingBy(r -> new TaskScopeType(r.taskType, r.scope)))
//                                .collect(Collectors.groupingBy(r -> r.taskType))
//                                .entrySet().stream()
//                                .map(pe -> pe.getValue().stream().collect(Collectors.minBy(order)).orElse(null))
////                                .filter(r -> r != null)
//                                .sorted(order)
//                                .collect(Collectors.toList()))
//                )
//                .forEach(p -> taskScopeStartPlans.put(p.getKey(), p.getRight()));


//                .filter(p -> p.getValue().)
//                .collect(Collectors.groupingBy(r -> new TaskScopeType(r.taskType, r.scope)))
//                .entrySet().stream()
//                .filter(f -> f.)values().stream().map(l -> l.stream()
////                    .sorted(order)
//                        .collect(Collectors.groupingBy(r -> r.taskType))
//                        .entrySet().stream().map(e -> e.getValue().stream()
//                                .collect(Collectors.minBy(order)).get()
//                        )
//                        .sorted(order)
//                        .collect(Collectors.toList())
//        ).collect(Collectors.toList());

//        // create the plans
//        Map<String, List<Result>> testWPs = new HashMap<>();
//        results.stream()
//                .collect(Collectors.groupingBy(r -> r.wp))
//                .entrySet().stream()
//                .filter(e -> e.getValue().size() >= 20)
////                .sorted(Comparator.comparing(e -> -e.getValue().stream().mapToLong(r -> r.start.getTime()).min().getAsLong()))
////                .limit(5)
//                .forEach(e -> testWPs.put(e.getKey(), e.getValue().stream().collect(Collectors.groupingBy(r -> r.taskType))
//                        .entrySet().stream().map(ee -> ee.getValue().stream()
//                                .collect(Collectors.minBy(order)).get()
//                        ).collect(Collectors.toList()))
//                );




//        TimeDiagram td = new TimeDiagram();
//        td.createImage(testWP, outputDir + "wp-" + wpFN, td::drawByDay);

////        construct the history database
        // print plan reports
        System.out.println(planReportByteOS.toString());

        Set<String> plannedScopes = testWPScopes.stream().filter(s -> !nonPlannedScopes.contains(s)).collect(Collectors.toSet());;
        System.out.println(String.format("Planned scopes %d - %s", plannedScopes.size(), plannedScopes.stream().collect(Collectors.joining(", "))));
        System.out.println(String.format("Not planned scopes %d - %s", nonPlannedScopes.size(), nonPlannedScopes.stream().collect(Collectors.joining(", "))));
    }


    protected boolean planExperiment(Map<String, List<Result>> plans,  Pair<String, List<Result>> testPlan, List<Result> testWP, Set<TaskType> taskTypes, String outputDir, String prefix){
        String pathPrefix = outputDir + prefix;
        // export original plan
        checkOrder(testWP);
//        List<SequencePattern> originalPlan = OriginalPlanner.planner.plan(testPlan.getValue());


//        List<SequencePattern> naivePlan = NaivePlanner.planner.plan(plans, taskTypes);
//        writeAsGraphml(naivePlan, outputDir + "plan-naive-" + testPlan.getKey().replaceAll("[\\/]", "_") + ".graphml");

        List<SequencePattern> reusePlan = ReuseBasedPlanner.planner.planConnected(plans, taskTypes, ExtractData::calculateSetSimilarity);
        List<SequencePattern> reusePlanD = ReuseBasedPlanner.planner.planDisconnected(plans, taskTypes, ExtractData::calculateSetSimilarity);

        if(reusePlan.isEmpty() && reusePlanD.isEmpty()){
            planReports.println(String.format("Could not construct plan for plan \"%s\"", prefix));
            return false;
        }


        // hack
        if(prefix != null && !prefix.isEmpty())
            plannedByScope.put(prefix, reusePlan.stream().flatMap(s -> s.pattern.stream()).collect(Collectors.toSet()));

        FilterTransitiveEdgesAlg p = FilterTransitiveEdgesAlg.filterTransitiveEdges(reusePlan);
        p.filteredSequencePatterns.forEach(s -> s.removed = 1);
        writeAsGraphml(reusePlan, pathPrefix + "plan-reused-connected.graphml");
        reportPlan(taskTypes, reusePlan, prefix + "plan-reused-connected");


        writeAsGraphml(reusePlanD, pathPrefix + "plan-reused-disconnected.graphml");
        reportPlan(taskTypes, reusePlanD, prefix + "plan-reused-disconnected");



        List<SequencePattern> originalPlan = OriginalPlanner.planner.planSameTime(testPlan.getValue());
        writeAsGraphml(originalPlan, pathPrefix + "wp-org-plan.graphml");
        reportPlan(taskTypes, originalPlan, prefix + "wp-org-plan");

        List<SequencePattern> diffPlan = new SequencePatternDiff().calculateDiff(originalPlan, reusePlanD);
        writeAsGraphml(diffPlan, pathPrefix + "plan-org-reused-disconnected-diff.graphml");



//        List<SequencePattern> sequencePatterns = extractSupportedPatterns(taskScopeStartPlans, order, 1, minDistance);
//        List<SequencePattern> plan = sequencePatterns.stream()
//                .filter(s -> taskTypes.containsAll(s.pattern)).collect(Collectors.toList());
//
//
//        System.out.println();
//        writeAsGraphml(plan, outputDir + "plan-" + testPlan.getKey().replaceAll("[\\/]", "_") + ".graphml");
//
//        FilterTransitiveEdgesAlg pp = FilterTransitiveEdgesAlg.filterTransitiveEdges(sequencePatterns);
//        List<SequencePattern> dSequencePatterns = pp.filteredSequencePatterns;
//        List<SequencePattern> plan2 = dSequencePatterns.stream()
//                .filter(s -> taskTypes.containsAll(s.pattern)).collect(Collectors.toList());
//        writeAsGraphml(plan2, outputDir + "plan-reduced-" + testPlan.getKey().replaceAll("[\\/]", "_") + ".graphml");
//        // tasks that start at the same time



        System.out.println("FINISHED");
        return true; // planning had some plans
    }

    public void reportPlan(Set<TaskType> taskTypes, List<SequencePattern> plan, String planLabel){

        Set<TaskType> plannedTasks = plan.stream().flatMap(s -> s.pattern.stream()).collect(Collectors.toSet());
        Set<TaskType> nonPlannedTasks = taskTypes.stream().filter(t -> !plannedTasks.contains(t)).collect(Collectors.toSet());
        Map<String, List<SequencePattern>> wps = plan.stream().filter(s -> !s.instances.isEmpty())
                .collect(Collectors.groupingBy(s -> s.instances.stream().findFirst().map(i -> i.get(0).wp).orElse(null)));
//                .collect(Collectors.groupBy(s -> s.instances.stream().findFirst().map(i -> i.get(0).wp).orElse(null)))
//                .filter(s -> s!=null).collect(Collectors.toSet());
        planReports.println(String.format("plan \"%s\" has planned %d of total %d task types, %d task types are not planned",
                planLabel, plannedTasks.size(), taskTypes.size(), nonPlannedTasks.size()));

        wps.entrySet().forEach(e ->  planReports.println(
                String.format("\twp \"%s\" was used to plan %d task types into %d ordered task pairs",
                        e.getKey(),
                        e.getValue().stream().flatMap(s -> s.pattern.stream()).distinct().count(),
                        e.getValue().size())
        ));
        planReports.println();
        planReports.println();
        planReports.println();

    }

    /**
     * Create a task plan ordered by the order comparator taking the minimum element(s) of each groupId
     * @param plans
     * @param orderBy
     * @param groupId
     */
    public Map<String, List<Result>> historyPlans(Map<String, List<Result>> plans, Function<Result, Long> orderBy, Function<Result, Object> groupId){
        Map<String, List<Result>> planHistory = new HashMap<>();

        Comparator<Result> order = Comparator.comparing(orderBy);
//        Function<Result, Object> fullGroupId = r -> Arrays.asList(orderBy.apply(r), groupId.apply(r));

        plans.entrySet().stream()
                // leave only starts of different tasks types executed by different scope groups in each plan
                .map(e -> Pair.of(e.getKey(), e.getValue().stream()
//                                .collect(Collectors.groupingBy(r -> new TaskScopeType(r.taskType, r.scope)))
                                .collect(Collectors.groupingBy(groupId))
                                .entrySet().stream()
                                .map(pe -> pe.getValue().stream()
//                                        .collect(Collectors.groupingBy(orderBy)).entrySet().stream()
                                        .collect(Collectors.minBy(order)).orElse(null))
//                                .map(pe -> pe.getValue().stream().collect(Collectors.minBy(order)).orElse(null))
//                                .filter(r -> r != null)
                                .sorted(order)
                                .collect(Collectors.toList()))
                )
                .forEach(p -> planHistory.put(p.getKey(), p.getRight()));
        return planHistory;
    }



    public Pair<String, List<Result>> selectPlan(Map<String, List<Result>> plans){

//         get last plan with more than 100 tasks
        Pair<String, Long> chosen = plans.entrySet().stream()
                .filter(e -> e.getValue().stream().map(r -> r.taskType).collect(Collectors.toSet()).size() > 100)
                .filter(e -> !e.getKey().startsWith("NFU"))
                .map(e -> Pair.of(e.getKey(), e.getValue().stream().mapToLong(r -> r.start.getTime()).min().orElse(Long.MIN_VALUE)))
                .max(Comparator.comparing(p -> p.getValue())).orElse(null);

        if(chosen != null)
            return Pair.of(chosen.getKey(), plans.get(chosen.getKey()));
        return null;
    }


    public int countBadOrders(List<Result> l){
        if(l.size() < 2)
            return 0;
        int badOrders = 0;
        Result r1 = l.get(0);
        for(int i = 1; i < l.size(); i++){
            Result r2 = l.get(i);
            if(r1.start.getTime() > r2.start.getTime()){
                badOrders ++;
            }
            r1 = r2;
        }
        return badOrders;
    }
    public boolean checkOrder(List<Result> l){
        int badOrders = countBadOrders(l);
        System.out.println("checkOrder - bad orders = " + badOrders);
        return badOrders > 0;
    }

    public boolean checkOrder(Collection<SequencePattern> plan){
        if(plan.isEmpty())
            return true;

        int badOrders = 0;
        for(SequencePattern sp : plan){
            for(List<Result> l : sp.instances){
                int bo = countBadOrders(l);
                badOrders += bo;
            }
        }
        System.out.println("checkOrder - bad orders = " + badOrders);
        return badOrders > 0;
    }




    public List<SequencePattern> extractSupportedPatterns(Map<String, List<Result>> sequencess,
                                                          Comparator<Result> order,
                                                          float support,
                                                          long minDistance
    ) {

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
                        Math.abs(r2.start.getTime() - r1.start.getTime()) < minDistance &&
                                r1.scope.equals(r2.scope)
        );


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

        FilterTransitiveEdgesAlg p = FilterTransitiveEdgesAlg.filterTransitiveEdges(supportedPatterns);


//        // order test work packages
//        Map<String, List<SequencePattern>> plans = new HashMap<>();
//        for (Map.Entry<String, List<Result>> wp : testWPs.entrySet()) {
//            Set<TaskType> tts = wp.getValue().stream().map(r -> r.taskType).collect(Collectors.toSet());
//            List<SequencePattern> plan = p.filteredSequencePatterns.stream()
//                    .filter(s -> tts.containsAll(s.pattern)).collect(Collectors.toList());
//            plans.put(wp.getKey(), plan);
//            FilterTransitiveEdgesAlg pp = FilterTransitiveEdgesAlg.filterTransitiveEdges(supportedPatterns);
////            writeAsGraphml(pp.filteredSequencePatterns, outFilePrefix + wp.getKey().replaceAll("[\\/]", "_") + "-plan.graphml");
//        }


//        Pair<List<SequencePattern>, FeedbackEdgeSetResult<TaskType, SequencePattern>> p = FilterTransitiveEdgesAlg.filterTransitiveEdges(supportedPatterns);
        return p.filteredSequencePatterns;
    }

    public Set<TaskType> getUnsupportedTasks(Map<String, List<Result>> history, Collection<TaskType> tasks ){
        Set<TaskType> allTasks = history.entrySet().stream().flatMap(e -> e.getValue().stream().map(r -> r.taskType)).collect(Collectors.toSet());
        Set<TaskType> ret = new HashSet<>(tasks);
        ret.removeAll(allTasks);
        return ret ;
    }


    public static Map<TaskType, String> findBestScopeForTaskTypes(List<Result> results){
        Map<TaskType, String> taskScopeMap = new HashMap<>();
        results.stream().collect(Collectors.groupingBy(r -> r.taskType)).entrySet().stream()
                .forEach(e -> taskScopeMap.put(
                        e.getKey(),
                        e.getValue().stream().collect(Collectors.groupingBy(r -> r.scope)).entrySet().stream()
                                .max(Comparator.comparing(ee -> ee.getValue().size()))
                                .map(ee -> ee.getKey()).orElseGet(null)
                        )
                );
        return taskScopeMap;
    }

    public static List<String[]> calculateTaskTypeScopeStats(List<Result> results){
        List<String[]> ret = new ArrayList<>();
        ret.add(new String[]{// header
                "task type", "scope", "#logs", "#wp", "duration in WPs",
                "min#wp", "max#wp", "avg#wp",
                "minTime", "maxTime", "avgTime"});

        Function<Result, String> taskScope = r -> r.taskType.type + "," + r.scope;
        ToLongFunction<Result> durationF = r -> (r.end.getTime() - r.start.getTime())/1000;

        Function<Collection<Result>, Stream<Map.Entry<String, List<Result>>>> groupByWP = l ->  l.stream()
                .collect(Collectors.groupingBy(r -> r.wp)).entrySet().stream();

        Map<String, List<Result>> taskScopeGroups = results.stream()
                .collect(Collectors.groupingBy(taskScope));
        for(Map.Entry<String, List<Result>> e : taskScopeGroups.entrySet()) {
            Result s = e.getValue().get(0);
            IntSummaryStatistics nWPStats = groupByWP.apply(e.getValue()).mapToInt(en -> en.getValue().size()).summaryStatistics();
            LongSummaryStatistics tWPStats = groupByWP.apply(e.getValue()).mapToLong(en -> en.getValue().stream()
                    .mapToLong(durationF).sum()).summaryStatistics();
            String[] row = new String[]{
                    s.taskType.type,
                    s.scope,
                    e.getValue().size() + "",
                    e.getValue().stream().map(r -> r.wp).distinct().count() + "",
                    tWPStats.getSum() + "",
                    nWPStats.getMin() + "",
                    nWPStats.getMax() + "",
                    nWPStats.getAverage() + "",
                    tWPStats.getMin() + "",
                    tWPStats.getMax() + "",
                    tWPStats.getAverage() + ""
            };
            ret.add(row);
        }
        return ret;
    }

    public static List<String[]> calculateTaskTypeScopeStatsPerWP(List<Result> results){
        List<String[]> ret = new ArrayList<>();
        ret.add(new String[]{// header
                "wp", "task type", "scope",
                "#logs", "starting time%", "total duration",
                "minDur", "maxDur", "avgDur"}
        );

        Function<Result, String> taskScopeWp = r -> r.wp + "," + r.taskType.type + "," + r.scope;
        ToLongFunction<Result> durationF = r -> (r.end.getTime() - r.start.getTime())/1000;
        ToLongFunction<Result> startTime = r -> r.start.getTime()/1000;

        Map<String, List<Result>> wpTaskScopeGroups = results.stream().collect(Collectors.groupingBy(taskScopeWp));
        for(Map.Entry<String, List<Result>> e : wpTaskScopeGroups.entrySet()) {
            Result s = e.getValue().get(0);
            LongSummaryStatistics tWPStats = e.getValue().stream()
                    .mapToLong(durationF).summaryStatistics();
            String[] row = new String[]{
                    s.wp,
                    s.taskType.type,
                    s.scope,
                    e.getValue().size() + "",
                    e.getValue().stream().mapToLong(startTime).min().getAsLong() + "",
                    tWPStats.getSum() + "",
                    tWPStats.getMin() + "",
                    tWPStats.getMax() + "",
                    tWPStats.getAverage() + ""
            };
            ret.add(row);
        }
        return ret;
    }




    public static void writeCSV(List<String[]> data, String file){
        try(ICSVWriter w = new CSVWriterBuilder(new FileWriter(file)).build()){
            for(String[] row: data){
                w.writeNext(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writeMap(Map<?,?> map, String file, String keyLabel, String valueLabel){
        try(ICSVWriter w = new CSVWriterBuilder(new FileWriter(file)).build()){
            w.writeNext(new String[]{keyLabel, valueLabel});
            for(Map.Entry<?,?> e : map.entrySet()){
                w.writeNext(new String[]{e.getKey().toString(),e.getValue().toString()});
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testLists(){
        Set<List<Integer>> set = new HashSet<>();
        set.add(Arrays.asList(1,2));
        set.add(Arrays.asList(2,3));

        System.out.println(set.contains(Arrays.asList(2,3)));
        System.out.println(set.contains(Arrays.asList(1,3)));


        Set<List<TaskType>> set2 = new HashSet<>();
        TaskType t1 = new TaskType("t1", "t1");
        TaskType t2 = new TaskType("t2", "t2");
        TaskType t3 = new TaskType("t3", "t3");
        set2.add(Arrays.asList(t1,t2));
        set2.add(Arrays.asList(t2,t3));

        System.out.println(set2.contains(Arrays.asList(t2,t3)));
        System.out.println(set2.contains(Arrays.asList(t3,t2)));
        System.out.println(set2.contains(Arrays.asList(t1,t3)));
    }

    public static void main(String[] args) {
        String root = "c:\\Users\\kostobog\\Documents\\skola\\projects\\2019-CSAT-doprava-2020\\code\\aircraft-maintenance-planning-system\\aircraft-maintenance-planning-model\\example-instance-data\\seqpats-scope\\";
        String inputDir = "c:\\Users\\kostobog\\Documents\\skola\\projects\\2019-CSAT-doprava-2020\\input\\data_2020-02\\2017-2019.csv";
        String outputFolder = root + "d01-17-20-planning-001-f\\";
        File outDir = new File(outputFolder);
        if(!outDir.exists()){
            LOG.info("creating output folder \"{}\"", outDir.getAbsolutePath() );
            outDir.mkdirs();
        }
        new PlannerExperiments().buildPlan(inputDir, outputFolder);
    }
}
