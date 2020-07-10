package cz.cvut.kbss.amaplas.exp.optaplanner.model;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.director.easy.EasyScoreCalculator;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MyHardSoftScoreCalculator implements EasyScoreCalculator<MaintenanceJob> {

    public Score calculateScore(MaintenanceJob maintenanceJob) {
        int hardScore = 0;
        int softScore = 0;

        Set<Integer> evenIndices = new HashSet<>();
        Set<Integer> oddIndices = new HashSet<>();
        // even values should not have the same index
        for(MaintenanceTask task : maintenanceJob.getTasks()) {
            Integer index = task.getTaskOrderIndex();
//            if(task.getTaskOrderIndex() == null)
//                hardScore += -1;
            if(task.getVal() % 2 == 0){
                if(evenIndices.contains(index)){
                    hardScore += -1;
                } else {
                    evenIndices.add(index);
                }
                if(oddIndices.contains(index)){
                    hardScore += -1;
                }
            }else{
                oddIndices.add(index);
                if(evenIndices.contains(index)){
                    hardScore += -1;
                }
            }
        }
        //
        // odd number should be together
        hardScore += maintenanceJob.getTasks().stream()
                .filter(t -> t.getTaskOrderIndex() != null)
                .filter(t -> t.getVal() % 2 == 1)
                .collect(Collectors.groupingBy(t-> t.getTaskOrderIndex()))
                .entrySet().stream()
                .mapToInt(e -> e.getValue().size() - maintenanceJob.getTaskOrderIndices().size())
                .sum();

        return HardSoftScore.of(hardScore, softScore);
    }
}
