package cz.cvut.kbss.amaplas.exp.dataanalysis.algs;

import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.Sequence;
import ca.pfv.spmf.algorithms.sequentialpatterns.fournier2008_seqdim.SequenceDatabase;

import java.util.List;

public class MySequenceAlg {

    public static void extract(SequenceDatabase sd, double support, int minLength, int maxLength, int min){
//        sd.getSequences().stream().forEach(s -> s.getItemsets().stream().forEach( is -> {
//
//                }
//        ));

        List<Sequence> sequences = sd.getSequences();
        for(int i = 0; i < sequences.size() - 1; i ++){
//            i.
            for(int j = i + 1; j < sequences.size(); j ++){

            }
        }
    }


}
