package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.SequencePattern;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;
import org.jgrapht.GraphPath;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ToCSV {

    public static void writeEdges(Collection<SequencePattern> patterns, String fileName){
        try(PrintStream ps = new PrintStream(new FileOutputStream(fileName))) {
            ps.println("support; supportClassId, source; target; sLabel; tLabel");
            patterns.stream().forEach(p -> ps.println(
                    p.instances.size() + ";" +
                    p.supportClassId + ";" +
                    codeAndLabelPath(p))
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeSupportClasses(String fileName, Collection<SequencePattern> ... patternss){
        Map<Integer, Set<String>> supportClasses = new HashMap<>();
        for(Collection<SequencePattern> patterns: patternss) {
            patterns.forEach(p -> supportClasses.put(p.supportClassId, p.supportSet));
        }
        try(PrintStream ps = new PrintStream(new FileOutputStream(fileName))) {
            ps.println("supportId;instanceId;supportClassSize");
            supportClasses.entrySet().stream()
                    .forEach(p -> p.getValue().stream().sorted()
                            .forEach(s -> {
                                ps.print(p.getKey() + ";");
                                ps.print(s + ";");
                                ps.println(p.getValue().size() + "");
                            })
                    );
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writePaths(Collection<GraphPath<TaskType, SequencePattern>> paths, String fileName){
        try(PrintStream ps = new PrintStream(new FileOutputStream(fileName))) {
            ps.println("cycleId; cycleSize; edgeOrder; source; target; sLabel; tLabel; support; supportClassId");
            int pathId = 0;
            for(GraphPath<TaskType, SequencePattern> path : paths) {
                int edgeOrder = 0;
                List<SequencePattern> edgeList = path.getEdgeList();
                for(SequencePattern sp : edgeList){
                    ps.print(pathId + ";");
                    ps.print(edgeList.size() + ";");
                    ps.print((edgeOrder++) + ";");
                    ps.print(codeAndLabelPath(sp) + ";");
                    ps.print(sp.instances.size() + ";" );
                    ps.println(sp.supportClassId);
                }
                pathId ++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String codeAndLabelPath(SequencePattern sp){
        return codePath(sp) + ";" + labelPath(sp);
    }

    public static String codePath(SequencePattern sp){
        return sp.pattern.stream().map(tt -> tt.type).collect(Collectors.joining(";"));
    }

    public static String labelPath(SequencePattern sp){
        return sp.instances.get(0).stream().map(r -> r.taskType.label).collect(Collectors.joining(";"));
    }

    public static void writePathsPerRow(Collection<GraphPath<TaskType, SequencePattern>> paths, String fileName){
        try(PrintStream ps = new PrintStream(new FileOutputStream(fileName))) {
            int pathId = 0;
            ps.println("cycleId; cycleSize; first-node");
            for(GraphPath<TaskType, SequencePattern> path : paths) {
                List<SequencePattern> edgeList = path.getEdgeList();
                ps.print(pathId + ";");
                ps.print(edgeList.size() + ";");
                ps.print(path.getEdgeList().stream()
                        .map(sp -> sp.pattern.get(0).type)
                        .collect(Collectors.joining(";"))
                );
                ps.println(";" + edgeList.get(edgeList.size() - 1).pattern.get(1));
                pathId++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeSupportInstances(List<SequencePattern> patterns, String fileName){
        List<Result> nodes = patterns.stream().flatMap(p -> p.instances.stream().flatMap(i -> i.stream()))
                .distinct()
                .collect(Collectors.toList());
        try(PrintStream ps = new PrintStream(new FileOutputStream(fileName))) {
            ps.println(Result.header(";"));
            nodes.forEach(r -> ps.println(r.toString(";")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     *
     * @param sp
     * @return
     */
    public static List<Map<String, String>> asMaps(SequencePattern sp){
        List<Map<String,String>> ret = new ArrayList<>();
        for(int i = 0; i < 2; i ++) { // index - 0 - process source, 1 - process target
            if (sp.instances == null || sp.instances.isEmpty())
                return null;
            TaskType tt = sp.pattern.get(i);
            List<List<Result>> instances = sp.instances;
            Result exampleSupport = instances.get(0).get(i);
            Map<String, String> map = new HashMap<>();
            map.put("task type", tt.type);
            map.put("task type label", tt.label);
            map.put("acModel", exampleSupport.acmodel);
            map.put("acType", exampleSupport.acType);
            ret.add(map);
        }
        return ret;
    }


}
