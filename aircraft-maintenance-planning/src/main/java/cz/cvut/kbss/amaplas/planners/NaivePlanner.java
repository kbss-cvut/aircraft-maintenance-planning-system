package cz.cvut.kbss.amaplas.planners;

import cz.cvut.kbss.amaplas.algs.timesequences.TimeSequenceMatrix;
import cz.cvut.kbss.amaplas.model.Result;
import cz.cvut.kbss.amaplas.model.TaskType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NaivePlanner {
    public static final NaivePlanner planner = new NaivePlanner();

    public List<SequencePattern> plan(Map<String, List<Result>> history, Set<TaskType> taskTypes ){
        // construct history DB
        // construct direct order patterns
        TimeSequenceMatrix patternExtractor = new TimeSequenceMatrix();
        List<SequencePattern> supportedPatterns = patternExtractor.execute(new ArrayList<>(history.values()), 1,
//                // adjacent sequence patterns
//                true,
//                patternExtractor::preferBiEdgeWithBiggerSupport,
//                (r1, r2) -> true
                // sequence patterns within a time interval
                false,
                null,
                (r1, r2) -> true
//                        Math.abs(r2.start.getTime() - r1.start.getTime()) < minDistance &&
//                                r1.scope.equals(r2.scope)
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
