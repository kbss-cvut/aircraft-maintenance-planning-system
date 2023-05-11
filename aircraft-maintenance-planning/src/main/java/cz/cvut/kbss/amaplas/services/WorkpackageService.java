package cz.cvut.kbss.amaplas.services;


import cz.cvut.kbss.amaplas.model.TaskExecution;
import cz.cvut.kbss.amaplas.model.TaskType;
import cz.cvut.kbss.amaplas.model.Workpackage;
import cz.cvut.kbss.amaplas.persistence.dao.WorkpackageDAO;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkpackageService extends BaseService {
    protected WorkpackageDAO workpackageDAO;
    protected TaskTypeService taskTypeService;

    /**
     * Reads task executions and their sessions for the provided workpackage. Replaces referenced task types so that they
     * contain transient fields.
     */
    public void readTaskExecutions(Workpackage workpackage){
        Map<URI,TaskType> taskTypeMap = new HashMap<>();
        taskTypeService.getTaskTypes().forEach(tt -> taskTypeMap.put(tt.getEntityURI(), tt));
        workpackageDAO.readWorkparckageTasks(workpackage);
        workpackage.getTaskExecutions().stream()
                .filter(te -> te.getTaskType() == null || te.getTaskType().getEntityURI() == null)
                .map(te -> Pair.of(te, taskTypeMap.get(te.getTaskType().getEntityURI())))
                .filter(p -> p.getValue() != null)
                .forEach(p -> p.getKey().setTaskType(p.getValue()));

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
}
