package cz.cvut.kbss.amaplas.exp.dataanalysis.algs;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreatePrefixTree {


    public static List<Pair<String,String>> createTrie(Reader r) throws IOException {

        BufferedReader br = new BufferedReader(r);

        return createTrie(br.lines()).distinct().collect(Collectors.toList());
    }

    public static Stream<Pair<String,String>> createTrie(Stream<String> treePaths) throws IOException {
        return treePaths.flatMap(tp -> sequenceToEdges(taskTypePrefixedTreeElements(tp)).stream());
    }

    protected static List<Pair<String,String>> sequenceToEdges(List<String> seq){
        List<Pair<String,String>> ret = new ArrayList<>(seq.size() - 1);
        for(int i = 0; i < seq.size() - 1; i++){
            ret.add(Pair.of(seq.get(i), seq.get(i + 1)));
        }
        return ret;
    }

    protected static Pattern taskTypeComponentPattern = Pattern.compile("(\\s*[^\\w\\d]?[\\d\\w]{1,2})");
    public static List<String> taskTypePrefixedTreeElements(String str){
        Matcher m = taskTypeComponentPattern.matcher(str);
        List<String> prefixed = new ArrayList<>();
        String prefix = "";
        while(m.find()){
            prefix = prefix + m.group(1);
            prefixed.add(prefix);
        }
        return prefixed;
    }


//
//    public static String[] splitFixedLength(String str, int w){
//        String[] list = new String[(str.length()+w-1)/w];
//
//        for(int i = 0, start=0; i < list.length; i ++, start += w){
//            int end = start + w;
//            if(end > str.length())
//                end = str.length();
//            list[i] = str.substring(start, end);
//        }
//        return list;
//    }
//
//    protected static void toPrefixedLR(String[] list){
//        toPrefixedLR(list, "", false);
//    }
//    protected static void toPrefixedLR(String[] list, String sep, boolean useSepAt0){
//        String prefix = "";
//        for(int i = 0; i < list.length; i++){
//            if(i > 0 || useSepAt0)
//                prefix = prefix + sep;
//            prefix = prefix + list[i];
//            list[i] = prefix;
//        }
//    }

    public static void main(String[] args) throws IOException{
        String file = "c:\\Users\\kostobog\\Documents\\skola\\projects\\2019-CSAT-doprava-2020\\code\\aircraft-maintenance-planning-system\\aircraft-maintenance-planning-model\\example-instance-data\\task-type-codes.csv";
        String output = "c:\\Users\\kostobog\\Documents\\skola\\projects\\2019-CSAT-doprava-2020\\code\\aircraft-maintenance-planning-system\\aircraft-maintenance-planning-model\\example-instance-data\\task-type-codes-hierarchy.csv";
        List<Pair<String,String>> trieEdges = null;
        try(FileReader fr = new FileReader(file)){
            trieEdges = createTrie(fr);
        }
        if(trieEdges == null)
            return;

        try(PrintWriter w = new PrintWriter(new FileWriter(output))){
            w.println("parent\tchild");
            for(Pair<String,String> trieEdge : trieEdges){
                w.print(trieEdge.getKey());
                w.print("\t");
                w.println(trieEdge.getValue());
            }
        }
    }
}
