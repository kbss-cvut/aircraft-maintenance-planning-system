package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.function.BiFunction;

public class VectorSort {

    public static double[][] transpose(double[][] d){
        double[][] o = new double[d[0].length][];
        for(int i = 0;i < o.length; i++){
            o[i] = new double[d.length];
        }
        for(int i = 0;i < d.length; i++){
            for(int j = 0; j < d[0].length; j++){
                o[j][i] = d[i][j];
            }
        }
        return o;
    }

    public static int bitsetComp(BitSet b1, BitSet b2, float exp, float scaleDown){
        assert( b1.length() == b2.length());
        float diff = 0;
        int m = b1.length();
        for(int i = 0; i < m; i ++){
            if(b1.get(i) != b2.get(i)){
                diff += !b1.get(i) ?
                        -Math.pow(exp, (m-i)/scaleDown):
                         Math.pow(exp, (m-i)/scaleDown);
            }
        }
        return (int)diff;
    }

    public static double cosine(double[] r1, double[] r2){
        double diff = 0;
        for(int i = 0; i < r1.length; i ++){
            diff += r1[i]*r2[i]*Math.signum(r1[i] - r2[i]);
        }
        if(diff == 0 && !r1.equals(r2) ){
            diff = firstDifference(r1, r2);
        }
        return diff;
    }

    public static Integer dimMatchDistance(double[] r1, double[] r2, double dimPenalty, double diffScale){

        double diff = 0;
        for(int i = 0; i < r1.length; i ++){
            double d;
            if(r1[i] == 0 && r2[i] == 0){
                d = 0;
            }else if(r1[i] != 0 && r2[i] != 0) {
                d = diffScale * (r1[i] - r2[i]);
            }else{
                d = dimPenalty * (r1[i] - r2[i]);
            }
            diff += d;
        }
        if(diff == 0 && !r1.equals(r2)){
            diff = firstDifference(r1, r2);
        }
        return (int)diff;
    }

    public static Integer firstDifference(double[] r1, double[] r2){
        for(int i = 0; i < r1.length; i ++){
            if(r1[i] == r2[i])
                continue;
            return r1[i] < r2[i] ? -1 : 1;
        }
        return 0;
    }

    public static void sort(double[][] vectors){
        sort(vectors, 10);
    }


    public static void sort(double[][] vectors, double dimResolution){
        sort(vectors, (r1, r2) -> {
            double diff = 0;
            for(int i = 0; i < r1.length; i ++){
                double d = dimResolution*(r1[i] - r2[i]);
                diff += d*d;
            }
            return (int)Math.sqrt(diff);
        });

    }

    public static boolean sort(double[][] vectors, BiFunction<double[], double[], Integer> metric){
        try {
            Arrays.sort(vectors, (r1, r2) -> metric.apply(r1, r2));
        }catch(IllegalArgumentException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
