package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.config.props.ConfigProperties;
import cz.cvut.kbss.amaplas.config.props.Repository;
import cz.cvut.kbss.amaplas.io.SparqlDataReader;
import cz.cvut.kbss.amaplas.io.SparqlDataReaderRDF4J;
import cz.cvut.kbss.amaplas.model.Result;
import cz.cvut.kbss.amaplas.model.TaskType;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskTypeService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskTypeService.class);

    private final ConfigProperties config;
    private final Repository repoConfig;

    public TaskTypeService(ConfigProperties config) {
        this.config = config;
        this.repoConfig = config.getRepository();
    }

    @PostConstruct
    public void init(){
        // Load taskTypeDefinitions
        List<TaskType> taskTypeDefinitions = SparqlDataReaderRDF4J.__loadTCDefinitions(
                repoConfig.getUrl(), repoConfig.getTaskDefinitionsGraph(),
                repoConfig.getUsername(), repoConfig.getPassword());
        TaskType.setTaskTypeDefinitions(taskTypeDefinitions);
        // TODO - load task card types from data repository
        LOG.debug("Initializing the TaskTypeService done.");
        // load taskType to TaskType definition mappings
        loadTaskMappings();
    }

    /**
     * Loads mappings from the repository into memory cache.
     */
    public void loadTaskMappings(){
        analyzeTaskTypeDefinitionDuplicates();

        Map<String, TaskType> defs = new HashMap<>();
        TaskType.getTaskTypeDefinitions().stream()
                .filter(t -> t.getTCOrMPDCode() != null)
                .forEach(t -> defs.put(t.getEntityURI().toString(), t));

        ValueFactory f = SimpleValueFactory.getInstance();
        Map<String, Value> bindings = new HashMap();
        bindings.put("taskTypeDefinitionGraph", f.createIRI(repoConfig.getTaskDefinitionsGraph()));
        bindings.put("taskCardMappingGraph", f.createIRI(repoConfig.getTaskMappingGraph()));

        List<Pair<String, String>> taskTypeDefinitionMappings = SparqlDataReaderRDF4J.executeNamedQuery(
                SparqlDataReader.TASK_CARD_MAPPINGS,
                bindings,
                repoConfig.getUrl(), repoConfig.getUsername(), repoConfig.getPassword(), SparqlDataReaderRDF4J::convertToPair);

        Map<String, List<TaskType>> map = taskTypeDefinitionMappings.stream()
                .map(p -> Pair.of(p.getLeft(), defs.get(p.getRight())))
                .collect(Collectors.groupingBy(p -> p.getLeft(), Collectors.mapping(p -> p.getRight(), Collectors.toList())));

        // normalize the list of mapped definitions, e.i. remove duplicates and sort them correctly using ad-hock approach
        // with the method TaskType.findMatchingTCDef
        map.entrySet().forEach(e ->
                e.setValue(
                        // NOTE - the method searches for duplicates in the second argument which is redundant as the
                        // mapping is already calculated. Calling to correctly sort the list of definitions.
                        TaskType.findMatchingTCDef(e.getKey(),
                            e.getValue().stream().distinct().collect(Collectors.toList())
                        )
                )
        );
        TaskType.setTaskTCCode2TCDefinitionMap(map);
    }

    /**
     * Check if different task type definitions use the same code
     */
    public void analyzeTaskTypeDefinitionDuplicates(){
        List<Map.Entry<String, List<TaskType>>> codeMap = TaskType.getTaskTypeDefinitions().stream()
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
    public void updateTaskTypeMappingInMemory(List<Result> sessions){
        LOG.info("update task type mapping to task definitions");
        // read task type definitions initialize a map from session task type code to task types definition codes.
        List<TaskType> taskTypeDefinitions = SparqlDataReaderRDF4J.__loadTCDefinitions(
                repoConfig.getUrl(), repoConfig.getTaskDefinitionsGraph(),
                repoConfig.getUsername(), repoConfig.getPassword());
        TaskType.setTaskTypeDefinitions(taskTypeDefinitions);
        LOG.debug("Map task type definitions to session log task types");
        TaskType.initTC2TCDefMap(sessions);
    }

    /**
     * Calculates the mappings from the data in the repository and writes the new mappings in the repository and
     * refreshes the mapping memory cache.
     */
    public void updateTaskTypeMapping(List<Result> sessions){
        updateTaskTypeMappingInMemory(sessions);
        Map<String, List<TaskType>> map = TaskType.getTaskTCCode2TCDefinitionMap();
        SparqlDataReaderRDF4J.persistStatements(
                SparqlDataReaderRDF4J.convertTaskCardAsStatement(map),
                repoConfig.getTaskMappingGraph(),
                repoConfig.getUrl(), repoConfig.getUsername(), repoConfig.getPassword()
        );
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

    public List<TaskType> getTaskTypes(){
        if(TaskType.taskTypeMap == null)
            return null;
        return new ArrayList<>(TaskType.taskTypeMap.values());
    }
}
