package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.config.ConfigProperties;
import cz.cvut.kbss.amaplas.io.SparqlDataReaderRDF4J;
import cz.cvut.kbss.amaplas.model.Result;
import cz.cvut.kbss.amaplas.model.TaskType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TaskTypeService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskTypeService.class);

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
        LOG.debug("Initializing the TaskTypeService");
        // read task type definitions initialize a map from session task type code to task types definition codes.
        Map<String, List<Result>> revisions = revisionHistory.getAllClosedRevisionsWorkLog(false);
        List<Result> sessions = revisions.values().stream().flatMap(l -> l.stream()).collect(Collectors.toList());
        List<TaskType> taskTypeDefinitions = SparqlDataReaderRDF4J.__loadTCDefinitions(
                repoConfig.getUrl(), repoConfig.getTaskDefinitionsGraph(),
                repoConfig.getUsername(), repoConfig.getPassword());
        TaskType.setTaskTypeDefinitions(taskTypeDefinitions);
        LOG.debug("Map task type definitions to session log task types");
        TaskType.initTC2TCDefMap(sessions);
        LOG.debug("Initializing the TaskTypeService done.");
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
