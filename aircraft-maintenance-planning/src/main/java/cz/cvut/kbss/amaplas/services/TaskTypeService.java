package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.config.props.ConfigProperties;
import cz.cvut.kbss.amaplas.config.props.Repository;
import cz.cvut.kbss.amaplas.model.TaskExecution;
import cz.cvut.kbss.amaplas.model.TaskType;
import cz.cvut.kbss.amaplas.model.Workpackage;
import cz.cvut.kbss.amaplas.persistence.dao.TaskTypeDao;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class TaskTypeService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskTypeService.class);

    private final ConfigProperties config;
    private final Repository repoConfig;
    private final TaskTypeDao taskTypeDao;

    protected List<TaskType> taskTypesCache;
    protected List<TaskType> taskTypeDefinitionCache;
    protected Map<URI,TaskType> taskTypeMap;
    protected Map<URI,TaskType> taskTypeDefinitionMap;

    public TaskTypeService(ConfigProperties config, TaskTypeDao taskTypeDao) {
        this.config = config;
        this.repoConfig = config.getRepository();
        this.taskTypeDao = taskTypeDao;
    }

    public void resetCache(){
        LOG.info("reset caches - task type and task type definition caches");
        taskTypesCache = taskTypeDao.listTaskTypes();
        taskTypeMap = new HashMap<>();
        taskTypesCache.forEach(tt -> taskTypeMap.put(tt.getEntityURI(), tt));

        taskTypeDefinitionCache = taskTypeDao.listTaskTypeDefinitions();
        taskTypeDefinitionMap = new HashMap<>();
        taskTypeDefinitionCache.forEach(tt -> taskTypeDefinitionMap.put(tt.getEntityURI(), tt));

        setupDefinitionFieldOfTaskCards(taskTypesCache, taskTypeDefinitionCache);
        LOG.info("finished reset caches");
    }

    /**
     * Set the definition field of task types in the taskTypes list to a task definition with a matching code.
     * @param taskTypes
     * @param taskTypeDefinitions
     */
    public void setupDefinitionFieldOfTaskCards(List<TaskType> taskTypes,
                                                List<TaskType> taskTypeDefinitions) {
        Map<URI, TaskType> taskURIMap = new HashMap<>();
        taskTypes.forEach(t -> taskURIMap.put(t.getEntityURI(), t));

        Map<URI, TaskType> taskTypeDefinitionUriMap = new HashMap<>();
        taskTypeDefinitions.forEach(t -> taskTypeDefinitionUriMap.put(t.getEntityURI(), t));

        taskTypeDao.readTaskTypeDefinitionsMap()
                .stream()
                .map(p -> Pair.of(taskURIMap.get(p.getKey()), taskTypeDefinitionUriMap.get(p.getValue())))
                .filter(p -> p.getKey() != null)
                .forEach(p -> p.getKey().setDefinition(p.getValue()));
    }

    /**
     * Updates the value of the <code>TaskType.definition</code> property by finding matches between task card and
     * definition codes.
     *
     * @param taskTypes required fields:<br>
     * - {@link TaskType#getCode()}<br>
     * - {@link TaskType#getEntityURI()}
     * @param taskTypeDefinitions required fields:<br>
     * - {@link TaskType#getCode()}<br>
     * - {@link TaskType#getMpdtask()}<br>
     * - {@link TaskType#getEntityURI()}
     */
    public void calculateTaskCards2DefinitionsMap(Collection<TaskType> taskTypes, Collection<TaskType> taskTypeDefinitions) {
        // clear definition field of task cards and group them by code
        taskTypes.forEach(t -> t.setDefinition(null));
        Map<String, List<TaskType>> taskTypeCodeMap = taskTypes.stream()
                .filter(t -> t.getCode() != null && "task-card".equals(t.getTaskcat()))
                .collect(Collectors.groupingBy(t -> t.getCode()));
        // for each task card code find a task definition with a matching code
        // and set it as the definition field of the task card.
        taskTypeCodeMap.entrySet().stream()
                .forEach(p -> findTaskCardDefinitionWithMatchingCode(p.getKey(), taskTypeDefinitions).stream()
                        .limit(1)
                        .forEach(td -> p.getValue().forEach(t -> t.setDefinition(td)))
                );
    }

    protected List<TaskType> findTaskCardDefinitionWithMatchingCode(String code, Collection<TaskType> taskTypeDefinitions){
        List<Pair<TaskType, Integer>> matchResults = findTaskCardDefinitionWithMatchingCode(code, TaskType::getCode, taskTypeDefinitions);

        if(matchResults.isEmpty()) {
            matchResults = findTaskCardDefinitionWithMatchingCode(code, TaskType::getMpdtask, taskTypeDefinitions);
        }
        List<TaskType> matches = matchResults.stream()
                .sorted(Comparator.comparing((Pair<TaskType, Integer> p) -> p.getRight()).reversed())
                .map(p -> p.getKey())
                .collect(Collectors.toList());

        return matches;
    }

    protected List<Pair<TaskType, Integer>> findTaskCardDefinitionWithMatchingCode(String tcCode, Function<TaskType, String> idfunc, Collection<TaskType> taskTypeDefinitions){
        return taskTypeDefinitions.stream()
                .map(t -> Pair.of(t, TaskType.is_TCCode_Match_v3(idfunc.apply(t), tcCode)))
                .filter(p -> p.getRight() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Check if different task type definitions use the same code
     */
    public void analyzeTaskTypeDefinitionDuplicates(){
        List<Map.Entry<String, List<TaskType>>> codeMap = getAllTaskTypeDefinitions().stream()
                .collect(Collectors.groupingBy(t -> t.getTCOrMPDCode()))
                .entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getValue().size())).collect(Collectors.toList());
        for(Map.Entry<String, List<TaskType>> codeMappings : codeMap){
            if(codeMappings.getValue().size() > 1){
                LOG.warn("There are multiple definitions with the same code {} , {}", codeMappings.getKey(), codeMappings.getValue());
            }
        }
    }

    /**
     * Calculates the mappings from the data in the repository and refreshes the mapping memory cache.
     */
    public void updateTaskTypeMappingInMemory(){
        LOG.info("update task type mapping to task definitions");
        setupDefinitionFieldOfTaskCards(taskTypesCache, taskTypeDefinitionCache);
    }

    /**
     * Calculates the mappings from the data in the repository and writes the new mappings in the repository and
     * refreshes the mapping memory cache.
     */
    public void updateTaskTypeMapping(){
        List<TaskType> taskTypes = taskTypeDao.listTaskTypeBasicDescriptions();
        List<TaskType> taskTypeDefinitions = taskTypeDao.listTaskTypeDefinitionIds();
        calculateTaskCards2DefinitionsMap(taskTypes, taskTypeDefinitions);
        taskTypeDao.persistTaskCardCode2DefinitionMap(taskTypes, repoConfig.getTaskMappingGraph());
        resetCache();
    }

    public List<TaskType> getAllTaskTypeDefinitions(){
        return taskTypeDao.listTaskTypeDefinitions();
    }

    public TaskType getCahcedTaskType(URI taskTypeUri){
        return taskTypeMap.get(taskTypeUri);
    }

    public TaskType getCachedTaskTypeDefinition(URI taskTypeUri){
        return taskTypeDefinitionMap.get(taskTypeUri);
    }

    public List<TaskType> getTaskTypes(){
        return taskTypesCache;
    }

    public List<TaskType> getTaskTypeDefinitions(){
        return taskTypeDefinitionCache;
    }
}
