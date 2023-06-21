package cz.cvut.kbss.amaplas.persistence.dao;


import cz.cvut.kbss.amaplas.persistence.dao.mapper.EntityRegistry;
import cz.cvut.kbss.amaplas.model.MaintenanceGroup;
import cz.cvut.kbss.amaplas.model.TaskType;
import cz.cvut.kbss.amaplas.model.Workpackage;
import cz.cvut.kbss.amaplas.persistence.dao.mapper.Bindings;
import cz.cvut.kbss.amaplas.persistence.dao.mapper.QueryResultMapper;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Repository
public class TaskTypeDao extends BaseDao<TaskType>{
    private static final Logger LOG = LoggerFactory.getLogger(TaskTypeDao.class);
    private static final IRI hasSpecificTaskWithId = SimpleValueFactory.getInstance().createIRI(Vocabulary.s_p_has_specific_task_with_id);
    private static final IRI HAS_TASK_TYPE_DEFINITION = SimpleValueFactory.getInstance().createIRI(
            Vocabulary.ONTOLOGY_IRI_aircraft_maintenance_planning + "/" + "has-task-type-definition"
    );


    public static final String TASK_TYPES = "/queries/analysis/task-type.sparql";
    public static final String WP_TASK_TYPES = "/queries/analysis/wp-task-types.sparql";
    public static final String TASK_TYPES_SESSION_TITLES = "/queries/analysis/task-type-session-titles.sparql";
    public static final String TASK_TYPES_SESSION_SCOPES = "/queries/analysis/task-type-session-scopes.sparql";
    public static final String TASK_TYPES_DEFINITIONS = "/queries/analysis/task-types-definitions.sparql";
    public static final String TASK_TYPE_DEFINITION_IDS = "/queries/analysis/task-type-definition-ids.sparql";
    public static final String TASK_TYPES_DEFINITIONS_MAP = "/queries/analysis/task-card-mappings.sparql";


    /**
     * Converts the input map to statements ?key amp:has-specific-task-with-id ?valListItem.
     *
     * @param map
     * @return
     */
    public static List<Statement> taskCardCode2DefinitionAsStatements(Map<String, List<TaskType>> map){
        ValueFactory f = SimpleValueFactory.getInstance();

        return map.entrySet().stream().
                filter(e -> !e.getValue().isEmpty()).
                map(e -> f.createStatement(
                                f.createIRI(e.getValue().get(0).getEntityURI().toString()),
                                hasSpecificTaskWithId,
                                f.createLiteral(e.getKey())
                        )
                ).collect(Collectors.toList());
    }

    protected Supplier<QueryResultMapper<TaskType>> taskTypeBasicDescriptionMapper = () -> new QueryResultMapper<>(TASK_TYPES) {
        @Override
        public TaskType convert() {
            TaskType taskType = new TaskType(
                    optValue("taskTypeId", null),
                    null,
                    optValue("superTaskTypeLabel", null),
                    null
            );
            mandatory("taskType", URI::create, taskType::setEntityURI);
            return taskType;
        }
    };

    protected Supplier<QueryResultMapper<Pair<URI,TaskType>>> wpsTaskTypeBasicDescriptionMapper = () -> new QueryResultMapper<>(WP_TASK_TYPES) {
        @Override
        public Pair<URI, TaskType> convert() {
            TaskType taskType = new TaskType(
                    optValue("taskTypeId", null),
                    null,
                    optValue("superTaskTypeLabel", null),
                    null
            );
            mandatory("taskType", URI::create, taskType::setEntityURI);
            return Pair.of(manValue("task", URI::create), taskType);
        }
    };


    protected Supplier<QueryResultMapper<Pair<String, String>>> taskTypeLabelsMapper = () -> new QueryResultMapper<>(TASK_TYPES_SESSION_TITLES) {
        @Override
        public Pair<String, String> convert() { // ?taskType ?sessionLabel
            return Pair.of(manValue("taskType"), manValue("sessionLabel"));
        }
    };

    protected Supplier<QueryResultMapper<Triple<URI, MaintenanceGroup, Integer>>> taskTypeSessionScopes = () -> new QueryResultMapper<>(TASK_TYPES_SESSION_SCOPES) {

        protected EntityRegistry taskTypeURIRegistry;
        protected EntityRegistry maintenanceGroupRegistry;
        @Override
        public List<Triple<URI, MaintenanceGroup, Integer>> convert(Iterable<BindingSet> bindingSets) {
            taskTypeURIRegistry = new EntityRegistry();
            maintenanceGroupRegistry = new EntityRegistry();

            return super.convert(bindingSets);
        }

        @Override
        public Triple<URI, MaintenanceGroup, Integer> convert() { // ?taskType ?scopeGroup ?scopeAbbreviation (COUNT(?session) as ?participationCount) {
            MaintenanceGroup scope = getMaintenanceGroup(
                    manValue("scopeGroup"),
                    manValue("scopeAbbreviation")
            );
            URI taskTypeURI = getTaskTypeURI(manValue("taskType"));
            return Triple.of(
                    taskTypeURI,
                    scope,
                    manValue("participationCount", Integer::parseInt)
            );
        }

        private MaintenanceGroup getMaintenanceGroup(String scopeGroupUri, String scopeAbbreviation){
            return maintenanceGroupRegistry.getOrCreate(scopeGroupUri,() -> {
                        MaintenanceGroup mg = new MaintenanceGroup();
                        mg.setEntityURI(URI.create(scopeGroupUri));
                        mg.setAbbreviation(scopeAbbreviation);
                        return mg;
                }
                ,MaintenanceGroup.class);
        }

        private URI getTaskTypeURI(String taskTypeUri){
            return taskTypeURIRegistry.getOrCreate(taskTypeUri,() -> URI.create(taskTypeUri)
                    ,URI.class);
        }
    };

    protected Supplier<QueryResultMapper<TaskType>> taskTypeDefinitionIdsMapper = () -> new QueryResultMapper<>(TASK_TYPE_DEFINITION_IDS) {
        @Override
        public TaskType convert() {
            TaskType taskType = new TaskType(
                    optValue("taskCardCode", null),
                    optValue( "title", null),
                    "task_card",
                    optValue("aircraftModel", null)
            );
            mandatory("taskTypeDefinition", URI::create, taskType::setEntityURI);
            optional("MPDTASK", taskType::setMpdtask);
            return taskType;
        }
    };

    protected Supplier<QueryResultMapper<TaskType>> taskTypeDefinitionsMapper = () -> new QueryResultMapper<>(TASK_TYPES_DEFINITIONS) {
        @Override
        public TaskType convert() {
            TaskType taskType = new TaskType(
                    optValue("taskCardCode", null),
                    optValue( "title", null),
                    "task_card",
                    optValue("aircraftModel", null)
            );

            if(bs.hasBinding("team")) {
                MaintenanceGroup maintenanceGroup = new MaintenanceGroup();
                optional("team", maintenanceGroup::setAbbreviation);
                optional("teamUri", URI::create, maintenanceGroup::setEntityURI);
                taskType.setScope(maintenanceGroup);
            }

            mandatory("taskTypeDefinition", URI::create, taskType::setEntityURI);
            optional("MPDTASK", taskType::setMpdtask);
            optional("phase", taskType::setPhase);
            optional("taskType", taskType::setTaskType);
            optional("area", taskType::setArea);
            optional("elPower", taskType::setElPowerRestrictions);//?elPower ?hydPower ?jacks
            optional("hydPower", taskType::setHydPowerRestrictions);
            optional("jacks", taskType::setJackRestrictions);
            return taskType;
        }
    };

    protected Supplier<QueryResultMapper<Pair<URI, URI>>> taskTypeDefinitionsMapMapper = () -> new QueryResultMapper<>(TASK_TYPES_DEFINITIONS_MAP) {
        @Override
        public Pair<URI, URI> convert() { // ?taskCard ?definition
            return Pair.of(manValue("taskCard", URI::create), manValue("definition", URI::create));
        }
    };


    public TaskTypeDao(EntityManager em, Rdf4JDao rdf4JDao) {
        super(TaskType.class, em, rdf4JDao);
    }

    /**
     *
     * @return list of all task types reading all their relevant properties, URI, code, label derived from sessions,
     * all session scopes and main scope derived from sessions
     */
    public List<TaskType> listTaskTypes(){
        List<TaskType> taskTypes = listTaskTypeBasicDescriptions();
        Map<URI, TaskType> taskTypeMap = new HashMap<>();
        taskTypes.forEach(t -> taskTypeMap.put(t.getEntityURI(), t));

        // set labels
        setupTaskTypeTitles(taskTypeMap);
        // set scopes
        setupScopes(taskTypeMap);

        return taskTypes;
    }

    /**
     * Reads the task types their details and task definitions for task executions of the input workpackage.
     * @param workpackage
     * @return
     */
    public List<Pair<URI, TaskType>> readTaskTypes(Workpackage workpackage){
        Bindings bindings = new Bindings();
        bindings.add("wp", workpackage.getEntityURI());
        return load(wpsTaskTypeBasicDescriptionMapper.get(), bindings);
    }

    protected void setupTaskTypeTitles(Map<URI, TaskType> taskTypeMap){

        Function<Collection<String>, String> selectLabel = c ->
                c.stream().max(Comparator.comparing((String s) -> s.length())).orElse(null);
        List<Pair<String,String>> uriLabelPairs = listTaskTypeLabels();

        uriLabelPairs.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getLeft(),
                        Collectors.mapping(p -> p.getRight(), Collectors.toSet()))
                ).entrySet()
                .forEach(e -> Optional.ofNullable(taskTypeMap.get(URI.create(e.getKey())))
                        .ifPresent(t -> t.setTitle(selectLabel.apply(e.getValue()))));
    }

    /**
     * Set the scope of  each Result (work session) to the main scope of the task card using the heuristic that the main
     * scope is the one which is used the most.
     */
    public void setupScopes(Map<URI, TaskType> taskTypeMap){

        Map<URI, List<Pair<MaintenanceGroup, Integer>>> taskTypeScopeMap = listTaskTypeScopes().stream()
                .collect(Collectors.groupingBy(
                        t -> t.getLeft(),
                        Collectors.mapping(t -> Pair.of(t.getMiddle(),t.getRight()), Collectors.toList()))
                );
        // set main scope
        Function<Collection<Pair<MaintenanceGroup,Integer>>, MaintenanceGroup> selectMainScope = c ->
                c.stream().max(Comparator.comparing(p -> p.getRight())).map(p -> p.getLeft()).orElse(null);

        taskTypeScopeMap.entrySet()
                .forEach(e -> Optional.ofNullable(taskTypeMap.get(e.getKey()))
                        .ifPresent(t -> {
                            // set all scopes
                            t.setScopes(e.getValue().stream().map(p -> p.getLeft()).collect(Collectors.toSet()));
                            // set main scope
                            t.setScope(selectMainScope.apply(e.getValue()));
                        }));
    }



    /**
     *
     * @return list of all task types with uri, code and category. The rest of the properties have to be loaded separately
     */
    public List<TaskType> listTaskTypeBasicDescriptions(){
        return load(taskTypeBasicDescriptionMapper.get(), null);
    }

    /**
     *
     * @return list a map of task type uri and scopes
     */
    public List<Pair<String, String>> listTaskTypeLabels(){
        return load(taskTypeLabelsMapper.get(), null);
    }

    /**
     *
     * @return list a map of task type uri and scopes
     */
    public List<Triple<URI, MaintenanceGroup, Integer>> listTaskTypeScopes(){
        return load(taskTypeSessionScopes.get(), null);
    }


    public List<TaskType> listTaskTypeDefinitions(){
        return load(taskTypeDefinitionsMapper.get(), null);
    }

    public List<TaskType> listTaskTypeDefinitionIds(){
        return load(taskTypeDefinitionIdsMapper.get(), null);
    }

    public List<Pair<URI, URI>> readTaskTypeDefinitionsMap(){
        return load(taskTypeDefinitionsMapMapper.get(), null);
    }

    public void persistTaskCardCode2DefinitionMap(Map<String, List<TaskType>> map, String graphIRI) {
        persistStatements(taskCardCode2DefinitionAsStatements(map),graphIRI);
    }

    public void persistTaskCardCode2DefinitionMap(List<TaskType> taskTypes, String graphIRI) {
        ValueFactory f = SimpleValueFactory.getInstance();
        persistStatements(
                taskTypes.stream().filter(t -> t.getDefinition() != null).map(t -> f.createStatement(
                        f.createIRI(t.getEntityURI().toString()),
                        HAS_TASK_TYPE_DEFINITION,
                        f.createIRI(t.getDefinition().getEntityURI().toString())
                )).collect(Collectors.toList()),
                graphIRI);
    }
}
