package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskTypeService {

    public List<TaskType> getTaskTypes(Collection<String> taskTypeCodes){
        return taskTypeCodes.stream().map(this::getTaskType).collect(Collectors.toList());
    }

    public TaskType getTaskType(String taskTypeCode){
        if(TaskType.taskTypeMap == null)
            return null;
        return TaskType.taskTypeMap.get(taskTypeCode);
    }
}
