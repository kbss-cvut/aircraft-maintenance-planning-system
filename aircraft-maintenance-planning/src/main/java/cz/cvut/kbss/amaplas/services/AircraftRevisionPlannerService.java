package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.ExtractData;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.SequencePattern;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskPlan;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.planners.OriginalPlanner;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.planners.ReuseBasedPlanner;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.planners.TaskTypePlanValidator;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AircraftRevisionPlannerService {

    private final RevisionHistory revisionHistory;
    private final TaskTypeService taskTypeService;

    public AircraftRevisionPlannerService(RevisionHistory revisionHistoiry, TaskTypeService taskTypeService) {
        this.revisionHistory = revisionHistoiry;
        this.taskTypeService = taskTypeService;
    }

    public List<SequencePattern> planTaskTypeCodes(List<String> toPlan){
        List<TaskType> taskTypes = taskTypeService.getTaskTypes(toPlan);
        return planTaskTypes(taskTypes, Collections.EMPTY_LIST);
    }

    public List<TaskPlan> planRevision(String revisionId){
        List<Result> workLog = revisionHistory.getClosedRevisionWorkLog(revisionId);
        if(workLog == null || workLog.isEmpty())
            return null;

        TaskTypePlanValidator validator = new TaskTypePlanValidator();

        List<SequencePattern> currentPlan = OriginalPlanner.planner.plan(workLog);
        validator.validate(currentPlan, workLog);

        List<TaskType> toPlan = workLog.stream().map(r -> r.taskType).distinct().collect(Collectors.toList());
        List<TaskType> orderedTasks = planTaskTypes(toPlan, Arrays.asList(revisionId));
        validator.validate(workLog.stream().map(r -> r.taskType).collect(Collectors.toSet()), new HashSet<>(orderedTasks));
        List<TaskPlan> plannedRevision = asPlan(orderedTasks);
        calculateTimeEstimates(plannedRevision, workLog);
        return plannedRevision;
    }


    /**
     * plans the task
     * @param toPlan
     */
    public List<TaskType> planTaskTypes(List<TaskType> toPlan, List<String> revisionsToIgnore){
        Set<String> revsToIgnoreSet = new HashSet(revisionsToIgnore);
        Map<String, List<Result>> historyPlans = revisionHistory.getStartSessionsOfMainScopeInClosedRevisions();
//        // copy history
//        historyPlans = new HashMap<>( historyPlans);
//        // remove
//        revisionsToIgnore.forEach(historyPlans::remove);
        List<SequencePattern> rawPlan = ReuseBasedPlanner.planner.planConnected(historyPlans, new HashSet<>(toPlan),
                ExtractData::calculateSetSimilarity,
                revisionId -> revsToIgnoreSet.contains(revisionId));
        List<TaskType> orderedTasks = ReuseBasedPlanner.planner.flattenPartialOrderBreadthFirst(rawPlan);

        return orderedTasks;
    }

    public List<TaskPlan> asPlan(List<TaskType> orderedTasks){
        List<TaskPlan> plan = new ArrayList<>();
        for(TaskType tt : orderedTasks) {
            plan.add(new TaskPlan(tt));
        }
        return plan;
    }


    public void calculateTimeEstimates(List<TaskPlan> plan, List<Result> workLog){
        // TODO
        Map<TaskType, List<Result>> tes = workLog.stream().collect(Collectors.groupingBy(r -> r.taskType));

        List<Date> startTimes = tes.values().stream()
                .map(l -> l.stream().min(Comparator.comparing(r -> r.start)).map(r -> r.start).orElse(null))
                .sorted()
                .collect(Collectors.toList());

        Map<TaskType, Double> durations = new HashMap<>();
        Map<TaskType, Double> workTime = new HashMap<>();
        tes.entrySet().forEach(e ->
                durations.put(
                        e.getKey(),
                        Result.mergeOverlaps(e.getValue().stream().sorted(Comparator.comparing(r -> r.start)).collect(Collectors.toList()))
                                .stream().mapToDouble(i -> i.duration()).sum()/1000/3600) // in hours
        );

        tes.entrySet().forEach(e ->
                workTime.put(
                        e.getKey(),
                        e.getValue().stream().mapToDouble(r -> r.dur).sum()/ 1000/3600) // in hours
        );

        int c = 0;
        for(TaskPlan tp : plan){
            List<Result> te = tes.get(tp.taskType);
            if(te == null)
                continue;
            tp.startTime = startTimes.get(c);
            tp.duration = durations.get(tp.taskType);
            tp.workTime = workTime.get(tp.taskType);
        }
    }
}
