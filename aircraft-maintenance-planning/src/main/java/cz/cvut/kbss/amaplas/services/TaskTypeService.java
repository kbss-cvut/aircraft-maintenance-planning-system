package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.config.props.ConfigProperties;
import cz.cvut.kbss.amaplas.config.props.Repository;
import cz.cvut.kbss.amaplas.model.TaskType;
import cz.cvut.kbss.amaplas.persistence.dao.TaskTypeDao;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

    public TaskTypeService(ConfigProperties config, TaskTypeDao taskTypeDao) {
        this.config = config;
        this.repoConfig = config.getRepository();
        this.taskTypeDao = taskTypeDao;
    }


    // TODO - refactoring DAO layer - check which methods replace the init and loadTaskMappings methods and check if the result looks ok
    @PostConstruct
    public void init(){
//        loadTaskTypes();
        resetCache();

//        // Load taskTypeDefinitions
//        List<TaskType> taskTypeDefinitions = taskTypeDao.listTaskTypeDefinitions();
////                repoConfig.getUrl(), repoConfig.getTaskDefinitionsGraph(),
////                repoConfig.getUsername(), repoConfig.getPassword());
//        TaskType.setTaskTypeDefinitions(taskTypeDefinitions);
//        // TODO - load task card types from data repository
//        LOG.debug("Initializing the TaskTypeService done.");
//        // load taskType to TaskType definition mappings
//        loadTaskMappings();
    }

    public void resetCache(){
        taskTypesCache = taskTypeDao.listTaskTypes();
        taskTypeDefinitionCache = taskTypeDao.listTaskTypeDefinitions();
        setupDefinitionFieldOfTaskCards(taskTypesCache, taskTypeDefinitionCache);
//        loadTaskTypes();
    }

    // TODO - move to service layer
//    public List<TaskType> loadTaskTypes(){
//        LOG.info("loadAndReconstructTaskTypeCache ...");
//        long time = System.currentTimeMillis();
//        // load task types
//        // for each task type pick one session title as the label of task type
//        // for each task type load its scopes and select the main scope
//        // load task type to task definition map
//
//        // create task type map
////        Map<URI, TaskType> taskTypeMap = new HashMap<>();
////        for(TaskType taskType : taskTypesCache) {
////            if(taskTypeMap.containsKey(taskType.getEntityURI()))
////                LOG.warn("There are multiple task types with the same IRI <{}> .", taskType.getEntityURI());
////            taskTypeMap.put(taskType.getEntityURI(), taskType);
////        }
//
//        // setup mapping between task types and task type definitions
//        setupDefinitionsOfTaskCards(taskTypesCache, taskTypeDefinitionCache);
//        LOG.info("loadAndReconstructTaskTypeCache DONE in {}", (System.currentTimeMillis() - time)/1000.);
//
//        return taskTypesCache;
//    }

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
     * @param taskTypes
     * @param taskTypeDefinitions
     */
    public void calculateTaskCards2DefinitionsMap(Collection<TaskType> taskTypes, List<TaskType> taskTypeDefinitions) {
        // clear definition field of task cards and group them by code
        taskTypes.forEach(t -> t.setDefinition(null));
        Map<String, List<TaskType>> taskTypeCodeMap = taskTypes.stream()
                .filter(t -> t.getCode() != null && "task-card".equals(t.getTaskcat()))
                .collect(Collectors.groupingBy(t -> t.getCode()));
        // for each task card code find a task definition with a matching code
        // and set it as the definition field of the task card.
        taskTypeCodeMap.entrySet().stream()
                .forEach(p -> p.getValue()
                        .forEach(
                                t -> findTaskCardDefinitionWithMatchingCode(p.getKey(), taskTypeDefinitions).stream()
                                        .limit(1)
                                        .forEach(t::setDefinition)
                        )
                );
    }

    protected List<TaskType> findTaskCardDefinitionWithMatchingCode(String code, List<TaskType> taskTypeDefinitions){
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

    protected List<Pair<TaskType, Integer>> findTaskCardDefinitionWithMatchingCode(String tcCode, Function<TaskType, String> idfunc, List<TaskType> taskTypeDefinitions){
        return taskTypeDefinitions.stream()
                .map(t -> Pair.of(t, TaskType.is_TCCode_Match_v3(idfunc.apply(t), tcCode)))
                .filter(p -> p.getRight() > 0)
                .collect(Collectors.toList());
    }


//    /**
//     * Loads mappings from the repository into memory cache.
//     */
//    public void loadTaskMappings(){
//        analyzeTaskTypeDefinitionDuplicates();
//
//        Map<String, TaskType> defs = new HashMap<>();
//        TaskType.getTaskTypeDefinitions().stream()
//                .filter(t -> t.getTCOrMPDCode() != null)
//                .forEach(t -> defs.put(t.getEntityURI().toString(), t));
//
//        ValueFactory f = SimpleValueFactory.getInstance();
//        Map<String, Value> bindings = new HashMap();
//        bindings.put("taskTypeDefinitionGraph", f.createIRI(repoConfig.getTaskDefinitionsGraph()));
//        bindings.put("taskCardMappingGraph", f.createIRI(repoConfig.getTaskMappingGraph()));
//
//        List<Pair<String, String>> taskTypeDefinitionMappings = SparqlDataReaderRDF4J.executeNamedQuery(
//                SparqlDataReader.TASK_CARD_MAPPINGS,
//                bindings,
//                repoConfig.getUrl(), repoConfig.getUsername(), repoConfig.getPassword(), SparqlDataReaderRDF4J::convertToPair);
//
//        Map<String, List<TaskType>> map = taskTypeDefinitionMappings.stream()
//                .map(p -> Pair.of(p.getLeft(), defs.get(p.getRight())))
//                .collect(Collectors.groupingBy(p -> p.getLeft(), Collectors.mapping(p -> p.getRight(), Collectors.toList())));
//
//        // normalize the list of mapped definitions, e.i. remove duplicates and sort them correctly using ad-hock approach
//        // with the method TaskType.findMatchingTCDef
//        map.entrySet().forEach(e ->
//                e.setValue(
//                        // NOTE - the method searches for duplicates in the second argument which is redundant as the
//                        // mapping is already calculated. Calling to correctly sort the list of definitions.
//                        TaskType.findMatchingTCDef(e.getKey(),
//                            e.getValue().stream().distinct().collect(Collectors.toList())
//                        )
//                )
//        );
//        TaskType.setTaskTCCode2TCDefinitionMap(map);
//    }

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
        resetCache();
        // TODO - move persistStatements method to DAO, get rid of references to SparqlDataReaderRDF4J and config
        calculateTaskCards2DefinitionsMap(taskTypesCache, taskTypeDefinitionCache);
        taskTypeDao.persistTaskCardCode2DefinitionMap(taskTypesCache, repoConfig.getTaskMappingGraph());
    }

    public List<TaskType> getAllTaskTypeDefinitions(){
        return taskTypeDao.listTaskTypeDefinitions();
    }
//
//    public TaskType getMatchingTaskTypeDefinition(TaskType taskType){
////        return taskTypeDao.
//        return TaskType.getTaskTypeDefinition(taskType); // TODO - use DAO layer instead
//    }

    public List<TaskType> getTaskTypes(Collection<String> taskTypeCodes){
        return taskTypeCodes.stream().map(this::getTaskType).collect(Collectors.toList());
    }

    public TaskType getTaskType(String taskTypeCode){
        if(TaskType.taskTypeMap == null)
            return null;
        return TaskType.taskTypeMap.get(taskTypeCode);
    }

    public List<TaskType> getTaskTypes(){
        return taskTypesCache;
    }

    public List<TaskType> getTaskTypeDefinitions(){
        return taskTypeDefinitionCache;
    }
}
