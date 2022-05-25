package cz.cvut.kbss.amaplas.planners;


import cz.cvut.kbss.amaplas.model.Result;
import cz.cvut.kbss.amaplas.model.SequencePattern;
import cz.cvut.kbss.amaplas.model.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskTypePlanValidator {
    private static final Logger LOG = LoggerFactory.getLogger(TaskTypePlanValidator.class);

    public void validate(List<SequencePattern> plan, List<Result> revisionHistory){
        Set<TaskType> expectedTaskTypes = revisionHistory.stream().map(r -> r.taskType).collect(Collectors.toSet());
        Set<TaskType> plannedTaskTypes = plan.stream().flatMap(p -> p.pattern.stream()).collect(Collectors.toSet());

        validate(expectedTaskTypes, plannedTaskTypes);
    }
    
    public void validate(Set<TaskType> expectedTaskTypes, Set<TaskType> plannedTaskTypes){
        if(!expectedTaskTypes.containsAll(plannedTaskTypes)){
            LOG.warn("The plan contains task types which are not part of the revision history. List of task types - {}",
                    plannedTaskTypes.stream()
                            .filter(t -> !expectedTaskTypes.contains(t))
                            .map(TaskType::toString)
                            .collect(Collectors.joining(", "))
            );
        }

        if(!plannedTaskTypes.containsAll(expectedTaskTypes)){
            LOG.warn("The plan does not schedule all task types which are not part of the revision history. " +
                            "List of task types - {}",
                    plannedTaskTypes.stream()
                            .filter(t -> !expectedTaskTypes.contains(t))
                            .map(TaskType::toString)
                            .collect(Collectors.joining(", "))
            );
        }
    }
}
