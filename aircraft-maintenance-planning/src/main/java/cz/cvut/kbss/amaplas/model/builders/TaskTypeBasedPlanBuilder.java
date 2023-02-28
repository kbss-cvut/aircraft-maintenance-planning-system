package cz.cvut.kbss.amaplas.model.builders;

import cz.cvut.kbss.amaplas.model.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class TaskTypeBasedPlanBuilder extends AbstractPlanBuilder<List<TaskType>>{

    private static final Logger LOG = LoggerFactory.getLogger(TaskTypeBasedPlanBuilder.class);
    protected Aircraft aircraft;

    public TaskTypeBasedPlanBuilder() {
    }

    public TaskTypeBasedPlanBuilder(AbstractPlanBuilder planBuilder) {
        super(planBuilder);
    }

    public RevisionPlan addMissingTaskPlansToRevision(PlanBuilderInput<List<TaskType>> input, RevisionPlan revisionPlan){
        Set<TaskType> plannedTaskTypes = revisionPlan.streamPlanParts()
                .filter(p -> p instanceof TaskPlan)
                .map(p -> ((TaskPlan)p).getTaskType())
                .collect(Collectors.toSet());

        Set<TaskType> taskTypes = new HashSet<>(input.getInput());
        taskTypes.removeAll(plannedTaskTypes);

        for(TaskType tt : taskTypes) {
            addPlanParts(revisionPlan, tt);
        }
        return revisionPlan;
    }

    public RevisionPlan createRevision(PlanBuilderInput<List<TaskType>> input){
        List<TaskType> taskTypes = input.getInput();

        String revisionIdString = input.workpackage.getId();

        aircraft = input.getWorkpackage().getAircraft();
        if(aircraft == null)
            aircraft = deriveAircraft(taskTypes, revisionIdString);

        // create bottom part of hierarchical plan -  create to task plans
        RevisionPlan revisionPlan = createRevision(aircraft, revisionIdString);

        for(TaskType tt : taskTypes) {
            addPlanParts(revisionPlan, tt);
        }
        return revisionPlan;
    }

    public RevisionPlan createRevision(Aircraft aircraft, String revisionCode){
        // create bottom part of hierarchical plan -  create to task plans
        RevisionPlan revisionPlan = new RevisionPlan();

        Long revisionId = revisionCode != null ? (long)revisionCode.hashCode() : null;
        revisionPlan.setTitle(revisionCode);
        revisionPlan.setId(revisionId);
        revisionPlan.setEntityURI(modelFactory.createURI(revisionId + ""));

        revisionPlan.setResource(aircraft);
        return revisionPlan;
    }

    protected void addPlanParts(RevisionPlan revisionPlan, TaskType tt){
        PhasePlan phasePlan = getPhasePlan(tt, (Aircraft)revisionPlan.getResource());
        revisionPlan.getPlanParts().add(phasePlan);

        AircraftArea area = getAircraftArea(optionalTaskType(tt));
        GeneralTaskPlan generalTaskPlan = getGeneralTaskPlanInCtx(optionalTaskType(tt), phasePlan, area);
        phasePlan.getPlanParts().add(generalTaskPlan);

        TaskPlan taskPlan = getTaskPlan(tt, generalTaskPlan, area.getTitle());
        generalTaskPlan.getPlanParts().add(taskPlan);
    }

    public PhasePlan getPhasePlan(TaskType tt, final Aircraft aircraft) {
        String phaseLabel = getPhaseLabel(optionalTaskType(tt));
        PhasePlan phasePlan = getEntity(phaseLabel, "phase", () -> {
            PhasePlan p = modelFactory.newPhasePlan(phaseLabel);
            p.setResource(aircraft);
            return p;
        });
        return phasePlan;
    }

    protected Aircraft deriveAircraft(List<TaskType> taskTypes, String revisionId){
        List<Aircraft> aircraftList = taskTypes.stream()
                .filter(tt -> tt != null)
                .map(tt -> getAircraft(optionalTaskType(tt)))
                .distinct()
                .collect(Collectors.toList());
        if(aircraftList.isEmpty()) {
            warn(String.format("WP \"%s\", the task list does not contain a task with specified aircraft.", revisionId));
            return null;
        }

        if(aircraftList.size() > 1)
            warn(String.format("WP \"%s\", the task list contains tasks for multiple aircraft %s.", revisionId, aircraftList));
            LOG.warn("Task list for this work package");
        return aircraftList.get(0);
    }


}
