package cz.cvut.kbss.amaplas.planners;

import cz.cvut.kbss.amaplas.algs.timesequences.TimeSequenceMatrix;
import cz.cvut.kbss.amaplas.model.TaskExecution;
import cz.cvut.kbss.amaplas.model.TaskType;
import cz.cvut.kbss.amaplas.model.Workpackage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NaivePlanner {
    public static final NaivePlanner planner = new NaivePlanner();

    public List<SequencePattern> plan(List<Workpackage> history, Set<TaskType> taskTypes ){
        // construct history DB
        // construct direct order patterns
        TimeSequenceMatrix patternExtractor = new TimeSequenceMatrix();
        List<List<TaskExecution>> sequences = history.stream().map(w -> new ArrayList<>(w.getTaskExecutions()))
                .collect(Collectors.toList());
        sequences.forEach(tel -> tel.sort(Comparator.comparing(te -> te.getStart())));
        List<SequencePattern> supportedPatterns = patternExtractor.execute(sequences, 1,
                // not only adjacent sequence patterns
                false,
                null,
                (r1, r2) -> true
        );


        List<SequencePattern> plan = new ArrayList<>();

        // construct plan based on direct historical orders
        supportedPatterns.stream()
                .filter(p -> p.direct)
                .filter(p -> taskTypes.containsAll(p.pattern))
                .forEach(plan::add);
        Set<List<TaskType>> pats = plan.stream().map(p -> p.pattern).collect(Collectors.toSet());

        //
        supportedPatterns.stream()
                .filter(p -> !p.direct)
                .filter(p -> !pats.contains(p.pattern))
                .filter(p -> taskTypes.containsAll(p.pattern))
                .forEach(plan::add);
        return plan;
    }
}
