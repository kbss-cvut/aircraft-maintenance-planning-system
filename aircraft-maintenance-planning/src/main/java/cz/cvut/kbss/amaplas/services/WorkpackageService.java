package cz.cvut.kbss.amaplas.services;


import cz.cvut.kbss.amaplas.model.TaskExecution;
import cz.cvut.kbss.amaplas.model.TaskType;
import cz.cvut.kbss.amaplas.model.Workpackage;
import cz.cvut.kbss.amaplas.persistence.dao.WorkSessionDao;
import cz.cvut.kbss.amaplas.persistence.dao.WorkpackageDAO;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

@Service
public class WorkpackageService extends BaseService {
    protected final WorkpackageDAO workpackageDAO;
    protected final TaskTypeService taskTypeService;
    protected final WorkSessionDao workSessionDao;

    protected Map<URI,Workpackage> workpackageTaskTimePropertiesCache;

    public WorkpackageService(WorkpackageDAO workpackageDAO, TaskTypeService taskTypeService, WorkSessionDao workSessionDao) {
        this.workpackageDAO = workpackageDAO;
        this.taskTypeService = taskTypeService;
        this.workSessionDao = workSessionDao;
    }

    public void init(){

    }

    public void resetCache(){}

    /**
     * Reads task executions and their sessions for the provided workpackage. Replaces referenced task types so that they
     * contain transient fields.
     */
    public void readTaskExecutions(Workpackage workpackage){
        workpackageDAO.readWorkparckageTasks(workpackage);
        workSessionDao.loadWorkSessions(workpackage);

        Set<TaskExecution> taskExecutions = workpackage.getTaskExecutions();

        taskExecutions.forEach(this::setStartAndEndFromParts);
        Date start = min(taskExecutions.stream().map(e -> e.getStart()).filter(d -> d != null));
        Date end = max(taskExecutions.stream().map(e -> e.getEnd()).filter(d -> d != null));
        workpackage.setStart(start);
        workpackage.setEnd(end);

        // set task types
        setTaskTypesOfTaskExecutions(workpackage);

        // set aircraft area of WO execution based on referenced task execution
        for (TaskExecution te : workpackage.getTaskExecutions()){
            TaskType taskType = te.getTaskType(); //task types are not the task types loaded from
            if(taskType == null || taskType.getTaskcat() == null || !taskType.getTaskcat().contains("work-order"))
                continue;
            if(te.getReferencedTasks() == null || te.getReferencedTasks().isEmpty())
                continue;
            te.getReferencedTasks().stream().limit(1)// select one
                    .forEach(rt -> te.getTaskType().setArea(
                            Optional.ofNullable(rt.getTaskType()).map(t -> t.getArea()).orElse(null))
                    );
        }
    }

    /**
     * Read URIs of similar workpackages and the similarity score
     * @param workpackage
     * @return
     */
    public List<Pair<Workpackage, Double>> findSimilarWorkpackages(Workpackage workpackage){
        return workpackageDAO.findSimilarWorkpackages(workpackage);
    }

    /**
     * Replaces reads task executions and their time properties of the input workpackage and stores them in its
     * taskExections field.
     * @param workpackage
     */
    public void setTaskExecutionsWithPropertiesTimeProperties(Workpackage workpackage, Workpackage workpackageA){
        workpackageDAO.readTimePropertiesOfWorkparckageTasks(workpackage, workpackageA);
        setTaskTypesOfTaskExecutions(workpackage);
    }

    /**
     * Replaces task types instances in task executions with task types from cache.
     * @param workpackage
     */
    public void setTaskTypesOfTaskExecutions(Workpackage workpackage){
        workpackage.getTaskExecutions().stream()
                .filter(te -> te.getTaskType() != null && te.getTaskType().getEntityURI() != null)
                .forEach(te -> Optional
                        .ofNullable(taskTypeService.getCahcedTaskType(te.getTaskType().getEntityURI()))
                        .ifPresent(te::setTaskType));
    }



    public void setStartAndEndFromParts(TaskExecution taskExecution){
        Date start = min(taskExecution.getWorkSessions().stream().map(w -> w.getStart()).filter(d -> d != null));
        Date end = max(taskExecution.getWorkSessions().stream().map(w -> w.getEnd()).filter(d -> d != null));
        taskExecution.setStart(start);
        taskExecution.setEnd(end);
    }

    public Date min(Stream<Date> dateStream){
        return dateStream.filter(d -> d != null)
                .sorted(Comparator.comparing(d -> d.getTime()))
                .findFirst().orElse(null);
    }

    public Date max(Stream<Date> dateStream){
        return dateStream.filter(d -> d != null)
                .sorted(Comparator.comparing((Date d) -> d.getTime()).reversed())
                .findFirst().orElse(null);
    }
}
