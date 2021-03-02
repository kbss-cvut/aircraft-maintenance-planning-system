package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.diff;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Diff;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.SequencePattern;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;

import java.util.*;
import java.util.stream.Collectors;

public class SequencePatternDiff {

    /**
     * The diff values are stored in sequence patterns of input parameter <code>to</code>
     * @param from
     * @param to
     */
    public List<SequencePattern> calculateDiff(Collection<SequencePattern> from, Collection<SequencePattern> to){
        // create a set of type patterns
        List<SequencePattern> diff = new ArrayList<>();
        Map<List<TaskType>, SequencePattern> map = new HashMap<>();
        for(SequencePattern sp : from){
            SequencePattern spn = new SequencePattern(sp);
            spn.diff = Diff.FROM;
            map.put(spn.pattern, spn);
        }

        diff.addAll(map.values());
        for(SequencePattern sp : to){
            SequencePattern spn = map.get(sp.pattern);
            if(spn != null) {
                spn.diff = Diff.BOTH;
            }else{
                spn = new SequencePattern(sp);
                spn.diff = Diff.TO;
                diff.add(spn);
            }
        }
        return diff;
    }
}
