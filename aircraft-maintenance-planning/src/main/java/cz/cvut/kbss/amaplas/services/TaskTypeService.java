package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.config.ConfigProperties;
import cz.cvut.kbss.amaplas.exp.dataanalysis.inputs.AnalyzeTCCodes;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskTypeService {

    private final RevisionHistory revisionHistory;
    private final ConfigProperties config;
    private ConfigProperties.Repository repoConfig;

    public TaskTypeService(RevisionHistory revisionHistory, ConfigProperties config) {
        this.revisionHistory = revisionHistory;
        this.config = config;
        this.repoConfig = config.getRepository();
    }

    @PostConstruct
    public void init(){
        // read task type definitions initialize a map from session task type code to task types definition codes.
        Map<String, List<Result>> revisions = revisionHistory.getAllClosedRevisionsWorkLog(false);
        List<Result> sessions = revisions.values().stream().flatMap(l -> l.stream()).collect(Collectors.toList());
        List<TaskType> taskTypeDefinitions = AnalyzeTCCodes.__loadTCDefinitions(
                repoConfig.getUrl(), repoConfig.getTaskDefinitionsGraph(),
                repoConfig.getUsername(), repoConfig.getPassword());
        TaskType.setTaskTypeDefinitions(taskTypeDefinitions);
        TaskType.initTC2TCDefMap(sessions);
    }
//
    public TaskType getMatchingTaskTypeDefinition(TaskType taskType){
        return TaskType.getTaskTypeDefinition(taskType);
    }

    public List<TaskType> getTaskTypes(Collection<String> taskTypeCodes){
        return taskTypeCodes.stream().map(this::getTaskType).collect(Collectors.toList());
    }

    public TaskType getTaskType(String taskTypeCode){
        if(TaskType.taskTypeMap == null)
            return null;
        return TaskType.taskTypeMap.get(taskTypeCode);
    }
}
