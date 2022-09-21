package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.config.ConfigProperties;
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
        List<TaskType> taskTypeDefinitions = SparqlDataReaderRDF4J.__loadTCDefinitions(
                repoConfig.getUrl(), repoConfig.getTaskDefinitionsGraph(),
                repoConfig.getUsername(), repoConfig.getPassword());
        TaskType.setTaskTypeDefinitions(taskTypeDefinitions);
        // TODO load mapping for task type definitions
        LOG.debug("Initializing the TaskTypeService done.");
        loadTaskMappings();
    }

    /**
     * Loads mappings from the repository into memory cache.
     */
    public void loadTaskMappings(){
        ValueFactory f = SimpleValueFactory.getInstance();
        Map<String, Value> bindings = new HashMap();
        bindings.put("taskTypeDefinitionGraph", f.createIRI(repoConfig.getTaskDefinitionsGraph()));
        bindings.put("taskCardMappingGraph", f.createIRI(repoConfig.getTaskMappingGraph()));
        List<Pair<String, String>> taskTypeDefinitions = SparqlDataReaderRDF4J.executeNamedQuery(
                SparqlDataReader.TASK_CARD_MAPPINGS,
                bindings,
                repoConfig.getUrl(), repoConfig.getUsername(), repoConfig.getPassword(), SparqlDataReaderRDF4J::convertToPair);

        Map<String, List<TaskType>> map = new HashMap<>();
        Map<String, List<TaskType>> defs = TaskType.getTaskTypeDefinitions().stream()
                .collect(Collectors.groupingBy(t -> t.getCode()));
        analyzeTaskTypeDefinitionDuplicates();

        // init the lists containing mapped task card definitions
        taskTypeDefinitions.stream().map(p -> p.getKey()).distinct().forEach(s -> map.put(s , new ArrayList<>()));
        // add the task card definitions
        taskTypeDefinitions.forEach(p -> map.get(p.getKey()).addAll(defs.get(p.getValue())));

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
                .collect(Collectors.groupingBy(t -> t.getCode()))
                .entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getValue().size())).collect(Collectors.toList());
        for(Map.Entry<String, List<TaskType>> codeMappings : codeMap){
            if(codeMappings.getValue().size() > 1){
                LOG.warn("There are multiple definitions with the same code {} , {}", codeMappings.getKey(), codeMappings.getValue());
            }
        }
    }

    /**
     * Calculates the mappings from the data in the repository and writes the new mappings in the repository and
     * refreshes the mapping memory cache.
     */
    public void updateTaskTypeMapping(){
        LOG.info("update task type mapping to task definitions");
        // read task type definitions initialize a map from session task type code to task types definition codes.
        Map<String, List<Result>> revisions = revisionHistory.getAllClosedRevisionsWorkLog(true);
        List<Result> sessions = revisions.values().stream().flatMap(l -> l.stream()).collect(Collectors.toList());
        List<TaskType> taskTypeDefinitions = SparqlDataReaderRDF4J.__loadTCDefinitions(
                repoConfig.getUrl(), repoConfig.getTaskDefinitionsGraph(),
                repoConfig.getUsername(), repoConfig.getPassword());
        TaskType.setTaskTypeDefinitions(taskTypeDefinitions);
        LOG.debug("Map task type definitions to session log task types");
        TaskType.initTC2TCDefMap(sessions);
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
}
