package cz.cvut.kbss.amaplas.exp.dataanalysis.validate;

import cz.cvut.kbss.amaplas.exp.common.Checker;
import cz.cvut.kbss.amaplas.exp.dataanalysis.io.CSVDataReader;
import cz.cvut.kbss.amaplas.exp.dataanalysis.io.SparqlDataReader;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class CheckImportedData {

    public void checkData(String endpoint, String[] files){
        // produce a statistical report for each of the sources and then compare the reports
        System.out.println("Reading data...");

        // read data from graph db
        System.out.println("reading data from SPARQL endpoint...");
        List<Result> allR = readDataFromEndpoint(endpoint);//.stream().filter(r -> r.wp.equals("OH-LZC/H-17 HMV3+WIFI+CABMOD")).collect(Collectors.toList());;

        // read data from excel
        System.out.println("reading data from csv...");
        List<Result> allE = readDataFromCSV(files);//.stream().filter(r -> r.wp.equals("OH-LZC/H-17 HMV3+WIFI+CABMOD")).collect(Collectors.toList());

        System.out.println("comparing data...");
        compare(allE, allR);
    }

    public void compare(List<Result> ver1, List<Result> ver2){
        System.out.println(Checker.diffSize(ver1, ver2, "number of time logs"));

        // compare task sets
        Map<String, List<Result>> v1 = ver1.stream().collect(Collectors.groupingBy(r -> r.wp));
        Map<String, List<Result>> v2 = ver2.stream().collect(Collectors.groupingBy(r -> r.wp));

        System.out.println(Checker.diffSize(v1, v2, "number of work packages"));
        compare(v1, v2);
    }

    public void compare(Map<String, List<Result>> ver1, Map<String, List<Result>> ver2){
        List<String> wpsAll = new ArrayList<>(ver1.keySet());
        wpsAll.addAll(ver2.keySet());
        wpsAll = wpsAll.stream().distinct().sorted().collect(Collectors.toList());
        int i = 0 ;
        for(String wpId : wpsAll) {
            if(i > 100){
                break;
            }
            i ++;
            List<Result> wp1 = ver1.get(wpId);
            List<Result> wp2 = ver2.get(wpId);
            System.out.println(Checker.diffRef(wpId, wp1, wp2, "wp "));
            if(wpId.isEmpty()){
                if(!wp1.isEmpty()) {
                    System.out.println(String.format("\tfound a wp with empty wp id. An example instance is:", wpId));
                    System.out.println("\t\t" + wp1.get(0).id);
                }
            }
            if(wp1 == null)
                wp1 = Collections.emptyList();
            if(wp2 == null)
                wp2 = Collections.emptyList();

            Map<String, List<Result>> log1F0 = wp1.stream().collect(Collectors.groupingBy(Result::form0));
            Map<String, List<Result>> log2F0 = wp2.stream().collect(Collectors.groupingBy(Result::form0));
//            System.out.println(diffSize(wp1, wp2, "\tnumber of time logs"));
            if(wp1.size() != wp2.size()){

//                System.out.println(diffSize(log1F0, log2F0, "\tnumber of time logs"));
                if(log1F0.size() != log2F0.size()){
                    Set<String> diff = new HashSet<>(log1F0.keySet());
                    diff.removeAll(log2F0.keySet());
                    List<String> diffl = new ArrayList<>(diff);
                    Map<String, List<Result>> byShiftGroup = diffl.stream().sorted().flatMap(s -> log1F0.get(s).stream())
                            .collect(Collectors.groupingBy(r -> r.shiftGroup));
                    byShiftGroup.remove("");
                    if(!byShiftGroup.isEmpty()){
                        System.out.println(Checker.diffSize(log1F0, log2F0, "\tnumber of time logs"));
//                        System.out.println(diffl.stream().sorted().flatMap(s -> log1F0.get(s).stream().map(r -> r.id)).collect(Collectors.joining(", ")));
                        byShiftGroup.entrySet().stream()
                                .map(e -> String.format(
                                        "\t\t%s - %s",
                                        e.getKey(),
                                        e.getValue().stream()
                                                .map(r -> r.id)
                                                .collect(Collectors.joining(", ")))
                                ).forEach(s -> System.out.println(s));
                    }
                }


//                Set<Result> common = new HashSet<>(wp1);
//                common.retainAll(wp2);
//                Set<Result> diff = new HashSet<>(wp1);
//                diff.addAll(wp2);
//                diff.removeAll(common);
//                List<Result> diffA = diff.stream().sorted(Comparator.comparing(r -> r.start)).collect(Collectors.toList());
////                System.out.println(String.format(
////                        "\t\twp1 size = %d, wp2 size = %d, common size = %d, diff size = %d",
////                        wp1.size(), wp2.size(), common.size(), diffA.size()));
//                System.out.println(String.format("\tExample of time log - %s", diffA.stream()
//                        .sorted(Comparator.comparing(r -> r.start))
////                        .limit(10)
//                        .map(r -> r.id)
//                        .collect(Collectors.joining(", "))
//                ));
            }
            Map<TaskType, List<Result>> tts1 = wp1.stream().collect(Collectors.groupingBy(r -> r.taskType));
            Map<TaskType, List<Result>> tts2 = wp2.stream().collect(Collectors.groupingBy(r -> r.taskType));
            System.out.println(Checker.diffSize(tts1, tts2, "\tnumber of different task types"));
            Set<TaskType> diff = new HashSet<>(tts1.keySet());
            diff.removeAll(tts2.keySet());

//            for( : ){
//
//            }

        }
    }

    public List<Result> readDataFromEndpoint(String endpoint){
//        String query = ResourceUtils.loadResource(SparqlDataReader.DA_TASK_SO_WITH_WP_SCOPE);
//        ResultSet rs = QueryExecutionFactory
//                .sparqlService(endpoint, query)
//                .execSelect();
//        List<Result> ret = SparqlDataReader.convert(rs);
        List<Result> ret = new SparqlDataReader().readDataNamedQuery(SparqlDataReader.DA_TASK_SO_WITH_WP_SCOPE, endpoint);
//        ret.forEach(r -> r.taskType.type = r.taskType.type.replace("%20", " "));
//        for(int i = 0; i < ret.size(); i ++) {
//            id
//        }
        return ret;
    }

    protected List<Result> readDataFromCSV(String[] files){
        List<Result> all = new ArrayList<>();
        for(String file : files){
            System.out.println(String.format("reading data from excel file \"%s\"", file));
//            List<Result> fromFile = new ExcelDataReader().readData(file);
            List<Result> fromFile = new CSVDataReader().readData(file);
            all.addAll(fromFile);
        }
        return all;
    }

    public static Date date(String date, String time){
        String datetime = constructDateString(date, time);
        try {
            return SparqlDataReader.df.parse(datetime);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String constructDateString(String date, String time){
        return String.format("%sT%s:00+0200",
                date.replaceFirst("(\\d+)\\.(\\d+)\\.(\\d+)", "$3-$2-$1"),
                time
        );
    }

    public static void testDateStringConstruction(){
        String date = "30.12.2017";
        String time = "17:00";
        String datetime = constructDateString(date, time);
        String datetimeExpected = "2017-12-30T17:00:00+0200";
        System.out.println(datetime);
        System.out.println(datetimeExpected);
        System.out.println(datetimeExpected.equals(datetime));
    }

    public static void testDateParsing(){
//        String date = "14-03-2017T15:04:00Z"; - fails
//        String date = "14-03-2017T15:04:00Z+0200"; - fails
        String date = "30.12.2017";
        String time = "17:00";
        String datetime = constructDateString(date, time);
        String datetimeExpected = "2017-12-30T17:00:00+0200";
        System.out.println(datetime);
        System.out.println(datetimeExpected);
        System.out.println(datetimeExpected.equals(datetime));
//        String date = "14-03-2017T15:04:00+0200";
        date = datetime;
        try {
            Date d = SparqlDataReader.df.parse(date);
            System.out.println(d);
            System.out.println(SparqlDataReader.df.format(d));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//        testDateStringConstruction();
//        testDateParsing();
        String dataDir = "c:\\Users\\kostobog\\Documents\\skola\\projects\\2019-CSAT-doprava-2020\\input\\data_2020-02\\";
        String endpoint = "http://localhost:7200/repositories/csat-data-01";
        CheckImportedData checker = new CheckImportedData();
        checker.checkData(endpoint, new String[]{dataDir + "2017_1_2.csv"});
//        checker.checkData(endpoint, new String[]{dataDir + "2017_1_2.csv", dataDir + "2018_1_2.csv", dataDir + "2019_1_2.csv"});
    }
}
