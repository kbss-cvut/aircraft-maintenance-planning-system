package cz.cvut.kbss.amaplas.exp.dataanalysis.inputs;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import cz.cvut.kbss.amaplas.config.DataRepositoryConfig;
import cz.cvut.kbss.amaplas.exp.dataanalysis.io.SparqlDataReader;
import cz.cvut.kbss.amaplas.exp.dataanalysis.io.SparqlDataReaderRDF4J;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;
import cz.cvut.kbss.amaplas.services.RevisionHistory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnalyzeTCCodes {

    public void run(){
        List<TaskType> taskTypes =  __loadTCDefinitionsFromWorkSessionData();
        List<TaskType> taskTypesDefinitions =  __loadTCDefinitions(
                "http://localhost:7200/repositories/csat-data-03",
                "<http://onto.fel.cvut.cz/ontologies/csat-maintenance/>",
                null,
                null
                );
        // compare the results
        String resultDir = "c:\\Users\\kostobog\\Documents\\skola\\projects\\2019-CSAT-doprava-2020\\input\\data_2021-04\\normalized\\";
        compareTCDefs(taskTypesDefinitions, taskTypes, (tdL, tdR) ->
                {
                    return is_TCCode_Match_v3(tdL.getCode(), tdR.getCode());
                },
                resultDir,
                "prefix-based"
        );

        compareTCDefs(taskTypesDefinitions, taskTypes, (tdL, tdR) ->
                {
                    return is_TCCode_Match_v2(tdL.getCode(), tdR.getCode());
                },
                resultDir,
                "equality"
        );
    }

    public static String prepareTCDef_Code(String s){
        return s.endsWith("/1.0") ? s.substring(0, s.length() - 4) : s;
    }

    public static boolean is_TCCode_Match_v1(String sl, String sr){
        String slFixed = prepareTCDef_Code(sl);
        return isStrMatch_v1(slFixed, sr);
    }

    public static boolean is_TCCode_Match_v2(String sl, String sr){
        String slFixed = prepareTCDef_Code(sl);
        return isStrMatch_v2(slFixed, sr);
    }

    public static boolean is_TCCode_Match_v3(String sl, String sr){
        String slFixed = prepareTCDef_Code(sl);
        return isStrMatch_v3(slFixed, sr);
    }

    public static boolean isStrMatch_v1(String sl, String sr){
        return sr.equals(sl);
    }

    public static boolean isStrMatch_v2(String sl, String sr){
        return sr.startsWith(sl);
    }

    /**
     * Checks if sl is a prefix of sr and if the character after the prefix is not alphanumeric.
     * @param sl
     * @param sr
     * @return
     */
    public static boolean isStrMatch_v3(String sl, String sr){
        // TODO - this is a simplified match, implement with split and test that it does not fail
        return sr.startsWith(sl) && (sr.length() == sl.length() || (
                    !Character.isDigit(sr.charAt(sl.length())) &&
                    !Character.isAlphabetic(sr.charAt(sl.length()))
                )
        );
    }

    /**
     * selects the best match where multiple different codes on the left match the same code on the right.
     * @param matches
     * @return
     */
    public static boolean hasUniqueMatchWithLongestLeft(List<Match> matches){
        List<Match> maxMatches = getMatchesMaxLength(matches,  m -> m.left().getCode().length());
        return  maxMatches.size() == 1;
    }
    public static boolean hasUniqueMatchWithShortestRight(List<Match> matches){
        List<Match> maxMatches = getMatchesMaxLength(matches,  m -> m.right().getCode().length());
        return  maxMatches.size() == 1;
    }

//    public static List<Match> getMatchesWithLongestLeft(List<Match> matches){
//        return getMatchesMaxLength(matches, m -> m.left().type.length());
//    }

    public static List<Match> getMatchesMaxLength(List<Match> matches, Function<Match, Integer> length){
        Map<Integer, List<Match>> lengthMap = matches.stream().collect(Collectors.groupingBy(length));
        int max = lengthMap.keySet().stream().mapToInt(i -> i).max().orElse(-1);
        if(max < 0)
            throw new IllegalArgumentException("Cannot find maximum integer in a list of integers");

        List<Match> maxMatches = lengthMap.get(max);
        return  maxMatches;
    }

    public static List<Match> getMatchesMinLength(List<Match> matches, Function<Match, Integer> length){
        Map<Integer, List<Match>> lengthMap = matches.stream().collect(Collectors.groupingBy(length));
        int max = lengthMap.keySet().stream().mapToInt(i -> i).min().orElse(-1);
        if(max < 0)
            throw new IllegalArgumentException("Cannot find maximum integer in a list of integers");

        List<Match> maxMatches = lengthMap.get(max);
        return  maxMatches;
    }

    public void compareTCDefs(List<TaskType> dsL, List<TaskType> dsR, BiPredicate<TaskType, TaskType> predicate, String dir, String predicateName){
        ComparisonResult cr = new ComparisonResult();
        cr.compare(dsL, dsR, predicate);
        cr.printResults(dir, predicateName, new String[]{"tc-left", "tc-right", "s-match", "m-match-left", "m-match-right"});
    }

    public static class ComparisonResult{
//        public List<TaskType> noMatchL;
//        public List<TaskType> noMatchR;
        public List<Match> singleMatch;
        public List<Match> multyMatchL;
        public List<Match> multyMatchR;
        public List<Match> outerJoin;

        public void compare(List<TaskType> dsL, List<TaskType> dsR, BiPredicate<TaskType, TaskType> predicate){
            Set<TaskType> notMatchedL = new HashSet<>(dsL);
            Set<TaskType> notMatchedR = new HashSet<>(dsR);
            List<Match> allMatches = new ArrayList<>();

            // find all matches
            for(TaskType dL : dsL){
                for(TaskType dR : dsR){
                    boolean match = predicate.test(dL, dR);
                    if(match){
                        allMatches.add(new Match(dL, dR));
                        notMatchedL.remove(dL);
                        notMatchedL.remove(dR);
                    }
                }
            }

            outerJoin = new ArrayList<>(allMatches);
            outerJoin.addAll(notMatchedL.stream().map(t -> new Match(t, (TaskType)null)).collect(Collectors.toList()));
            outerJoin.addAll(notMatchedR.stream().map(t -> new Match(t, (TaskType)null)).collect(Collectors.toList()));

//            // set no match results
//            noMatchL = new ArrayList<>(notMatchedL);
//            noMatchR = new ArrayList<>(notMatchedR);
//
//            // find matches with common left or right sides
            allMatches.stream().collect(Collectors.groupingBy(m -> m.left().getCode())).values()
                    .stream()
                    .filter(l -> l.size() > 1)
//                    .filter(l -> !hasUniqueMatchWithShortestRight(l))
                    .flatMap(c -> c.stream())
                    .forEach(m -> m.multiMatchL = true);
//                    .sorted(Comparator.comparing(m -> m.left().type)).collect(Collectors.toList());
            allMatches.stream().collect(Collectors.groupingBy(m -> m.right().getCode())).values()
                    .stream()
                    .filter(l -> l.size() > 1)
                    .filter(l -> !hasUniqueMatchWithLongestLeft(l))
                    .flatMap(c -> c.stream())
                    .forEach(m -> m.multiMatchR = true);
//                    .sorted(Comparator.comparing(m -> m.right().type)).collect(Collectors.toList());
            allMatches.stream()
                    .filter(m -> ! (m.multiMatchL || m.multiMatchR))
                    .forEach(m -> m.singleMatch = true);


//                    collect(Collectors.groupingBy(Match::right)).values().stream()
//                    .filter(l -> l.size() > 1)
//                    .flatMap(c -> c.stream())
//                    .forEach(m -> m.multiMatchR = true);
//            singleMatch = new ArrayList<>(allMatches);
//            singleMatch.removeAll(multyMatchL);
//            singleMatch.removeAll(multyMatchR);
        }

        public void printResults(String dir, String rel, String header[]){
            try(ICSVWriter w = new CSVWriterBuilder(new FileWriter(dir + "tc-def-match-" + rel + ".csv")).build()){
                w.writeNext(header);
                outerJoin.stream().map(this::toRow).forEach(w::writeNext);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String[] toRow(Match m){
            Function<Boolean, String> b2s = (b) -> b ? "T" : "F";
            String c1 = Optional.ofNullable(m.left()).map(t -> t.getCode()).orElse("");
            String c2 = Optional.ofNullable(m.right()).map(t -> t.getCode()).orElse("");
            return new String[]{c1, c2, b2s.apply(m.singleMatch), b2s.apply(m.multiMatchL) , b2s.apply(m.multiMatchR) };
        }
    }

    public static class Match{
        public TaskType l;
        public TaskType r;
        public boolean singleMatch;
        public boolean multiMatchL;
        public boolean multiMatchR;

        public TaskType left(){
            return l;
        }

        public TaskType right(){
            return r;
        }

        public Match(TaskType l, TaskType r) {
            this.l = l;
            this.r = r;
        }

    }



    /**
     * Ad-hock method to load data to be analyzed
     */
    public List<TaskType> __loadTCDefinitionsFromWorkSessionData(){
//        SparqlDataReaderRDF4J reader = new SparqlDataReaderRDF4J();
//        List<TaskType> taskTypes = reader.readTaskTypes(
//                SparqlDataReader.DA_TASK_SO_WITH_WP_SCOPE_OLD,
//                "http://localhost:7200/repositories/csat-data-02",
//                SparqlDataReaderRDF4J::convertToTaskType
//                );
        DataRepositoryConfig repoConfig = new DataRepositoryConfig();
        repoConfig.setUrl("http://localhost:7200/repositories/csat-data-02");
        RevisionHistory service = new RevisionHistory(repoConfig);
//        service.setRepositoryUrl("http://localhost:7200/repositories/csat-data-02");
        Map<String, List<Result>> map =  service.getAllClosedRevisionsWorkLog(false);
        List<TaskType> taskTypes = map.values().stream()
                .flatMap(l -> l.stream())
                .map(r -> r.taskType)
                .distinct()
                .filter(t -> "task_card".equals(t.getTaskcat()))
                .collect(Collectors.toList());
        taskTypes.forEach(t -> t.setCode(t.getCode().replaceFirst("[A-Z]+-","")));
        taskTypes.forEach(t -> t.setScope(t.getScope().replaceFirst("_group","")));

//        taskTypes.forEach(t -> System.out.println(Stream.of(t.acmodel, t.type, t.label).collect(Collectors.joining(", "))));
        return taskTypes;
    }

    public static List<TaskType> __loadTCDefinitions(String url, String graph, String username, String password){
        SparqlDataReaderRDF4J reader = new SparqlDataReaderRDF4J();
        List<TaskType> taskTypes = reader.readTaskDefinitions(
                SparqlDataReader.TASK_TYPES_DEFINITIONS,
                url,
                graph,
                username, password,
                SparqlDataReaderRDF4J::convertToTaskTypeDefinition
                );
//        taskTypes.forEach(t -> t.type = t.type.replaceFirst("/1.0",""));
//        taskTypes.forEach(t -> System.out.println(Stream.of(t.type, t.mpdtask, t.label, t.acmodel, t.area, t.phase, t.scope).collect(Collectors.joining(", "))));
        return taskTypes;
    }

    public static void manualTestCodeMatchVersions(){
        System.out.println(is_TCCode_Match_v2("572161-01-1/1.0", "572161-01-10"));
        System.out.println(is_TCCode_Match_v2("572161-01-10/1.0", "572161-01-10"));

        System.out.println(is_TCCode_Match_v3("572161-01-1/1.0", "572161-01-10"));
        System.out.println(is_TCCode_Match_v3("572161-01-10/1.0", "572161-01-10"));
    }

    public static void main(String[] args) {
        new AnalyzeTCCodes().run();
    }
}
