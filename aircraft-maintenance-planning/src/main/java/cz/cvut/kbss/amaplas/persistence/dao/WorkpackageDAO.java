package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.model.TaskExecution;
import cz.cvut.kbss.amaplas.model.TaskType;
import cz.cvut.kbss.amaplas.model.Workpackage;
import cz.cvut.kbss.amaplas.model.values.DateUtils;
import cz.cvut.kbss.amaplas.persistence.dao.mapper.Bindings;
import cz.cvut.kbss.amaplas.persistence.dao.mapper.EntityRegistry;
import cz.cvut.kbss.amaplas.persistence.dao.mapper.QueryResultMapper;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.query.BindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Repository
public class WorkpackageDAO extends BaseDao<Workpackage>{
    private static final Logger LOG = LoggerFactory.getLogger(WorkpackageDAO.class);

    public static final String WP_TASK_TYPES = "/queries/analysis/wp-task-types.sparql";

    public static final String WP_TASK_EXECUTIONS = "/queries/analysis/task-executions.sparql";

    public static final String TASK_EXECUTION_STATISTICS_FROM_PARTS = "/queries/analysis/task-execution-statistics-from-parts.sparql";
    public static final String WP_SIMILAR_WPS = "/queries/analysis/similar-wps.sparql";
    public static final String WP_SIMILAR_WP_SCOPES = "/queries/analysis/similar-wp-scopes.sparql";

    public static final String WP_HEADER = "/queries/analysis/wp-header.sparql";
    public static final String WP_IDS_ALL = "/queries/analysis/wp-ids-all.sparql";
    public static final String WP_IDS_OPENED = "/queries/analysis/wp-ids-opened.sparql";
    public static final String WP_IDS_CLOSED = "/queries/analysis/wp-ids-closed.sparql";

    protected Map<String,List<BindingSet>> rawWorkpackageTaskTimePropertiesCache = new HashMap<>();

    protected Supplier<QueryResultMapper<Pair<Workpackage, Integer>>> wpHeader = () -> new QueryResultMapper<>(WP_HEADER) {
        @Override
        public Pair<Workpackage, Integer> convert() {
            return Pair.of(new Workpackage(URI.create(manValue("wp")), manValue("wpId")), manValue("ttCount", Integer::parseInt));
        }
    };

    protected Function<String, QueryResultMapper<Workpackage>> wpIds = (queryName) -> new QueryResultMapper<>(queryName) {
        @Override
        public Workpackage convert() {
            return new Workpackage(URI.create(manValue("wp")), manValue("wpId"));
        }
    };

    protected Supplier<QueryResultMapper<Pair<URI, URI>>> wpTaskTypes = () -> new QueryResultMapper<>(WP_TASK_TYPES) {
        @Override
        public Pair<URI, URI> convert() {
            return Pair.of(manValue("wp", URI::create),manValue("taskType", URI::create));
        }
    };

    protected Supplier<QueryResultMapper<Pair<Workpackage, Double>>> similarWPsMapper = () -> new QueryResultMapper<>(WP_SIMILAR_WPS) {
        @Override
        public Pair<Workpackage, Double> convert() {
            return Pair.of(manValue("wpB", s -> {
                Workpackage wp = new Workpackage();
                wp.setEntityURI(URI.create(s));
                return wp;
            }), manValue("similarity", Double::parseDouble));
        }
    };

    protected Supplier<QueryResultMapper<Pair<Workpackage, Double>>> similarWPScopesMapper = () -> new QueryResultMapper<>(WP_SIMILAR_WP_SCOPES) {
        @Override
        public Pair<Workpackage, Double> convert() {
            return Pair.of(manValue("wpB", s -> {
                Workpackage wp = new Workpackage();
                wp.setEntityURI(URI.create(s));
                return wp;
            }), manValue("similarity", Double::parseDouble));
        }
    };

    protected Supplier<QueryResultMapper<TaskExecution>> taskExecutionMapper = () -> new QueryResultMapper<>(WP_TASK_EXECUTIONS) {
        protected Pattern taskTypeIRIPattern = Pattern.compile("task-type--([^-]+)--(.+)");
        protected HashMap<String, String > taskCategories = new HashMap<>(){
            {
                put("TC", "task-card");
                put("M", "maintenance-work-order");
                put("S", "scheduled-work-order");
            }
        };
        protected EntityRegistry registry;

        @Override
        public List<TaskExecution> convert(Iterable<BindingSet> bindingSets) {
            registry = new EntityRegistry();
            return super.convert(bindingSets);
        }

        @Override
        public TaskExecution convert() {
            String def = "unknown";
            String wp = manValue("wp");
            String defaultTaskType = def;
            String defaultTaskCategory = def;

            // task type
            String tType = manValue("tType");
            URI taskTypeUri = URI.create(tType);
            Matcher m = taskTypeIRIPattern.matcher(tType);
            if(m.find()) {
                defaultTaskCategory = taskCategories.getOrDefault(m.group(1), def);
                defaultTaskType = m.group(2);
            }

            TaskType taskType = new TaskType(
                    optValue("type", defaultTaskType),
                    optValue( "typeLabel", defaultTaskType),
                    optValue( "taskcat", defaultTaskCategory),
                    optValue("acmodel", def)
            );
            taskType.setEntityURI(taskTypeUri);
            taskType.setAverageTime(optValue("findingAverageTime", Double::parseDouble, null));
            if(taskType.getAverageTime() == null)
                taskType.setAverageTime(optValue("averageTime", Double::parseDouble, null));

            String taskExecutionURI = manValue("tt");
            TaskExecution taskExecution = registry.getOrCreate(taskExecutionURI, () -> new TaskExecution(URI.create(taskExecutionURI)), TaskExecution.class);

            if(taskExecution.getTaskType() == null) // task type will be null only if taskExecution was loaded as referencedTask
                taskExecution.setTaskType(taskType);

            String referencedTaskURI = optValue( "referencedTask", null);
            TaskExecution referencedTask = referencedTaskURI == null ?
                    null :
                    registry.getOrCreate(referencedTaskURI, () -> new TaskExecution(URI.create(referencedTaskURI)), TaskExecution.class);

            if(referencedTask != null) {
                Set<TaskExecution> referencedTasks = taskExecution.getReferencedTasks();
                if(referencedTasks == null) {
                    referencedTasks = new HashSet<>();
                    taskExecution.setReferencedTasks(referencedTasks);
                }
                referencedTasks.add(referencedTask);
            }
            taskExecution.setIssueTime(optValue("issueTime", s -> parseLocalDate(s), null));
            taskExecution.setEndTime(optValue("endTime", s -> parseLocalDate(s), null));
            taskExecution.setEstMin(optValue("estMin", Double::parseDouble, null));

            return taskExecution;
        }
    };
    protected Supplier<QueryResultMapper<BindingSet>> workpackagesWithTaskExecutionsWithTimeProperties = () -> new QueryResultMapper<>(TASK_EXECUTION_STATISTICS_FROM_PARTS) {
        @Override
        public BindingSet convert() {
            return bs;
        }
    };

    protected Supplier<QueryResultMapper<TaskExecution>> taskExecutionStatisticsFromPartsMapper = () -> new QueryResultMapper<>(TASK_EXECUTION_STATISTICS_FROM_PARTS) {
        @Override
        public TaskExecution convert() {
            TaskType taskType = manValue("taskType", s -> new TaskType(URI.create(s)));
            taskType.setCode(optValue("taskTypeId", null));
            TaskExecution taskExecution = manValue("task", s -> new TaskExecution(URI.create(s)));
            taskExecution.setTaskType(taskType);

            taskExecution.setIssueTime(optValue("issueTime", s -> parseLocalDate(s), null));
            taskExecution.setEndTime(optValue("endTime", s -> parseLocalDate(s), null));
            taskExecution.setStart(manValue("derivedStart", s -> parseDate(s)));
            taskExecution.setEnd(manValue("derivedEnd", s -> parseDate(s)));
            taskExecution.setWorkTime(manValue("derivedWorkTime", Long::parseLong));
            taskExecution.setDur(manValue("derivedDuration", Long::parseLong));

            return taskExecution;
        }
    };

    public void resetCache(){
        rawWorkpackageTaskTimePropertiesCache = getTimePropertiesOfWorkparckageTasks();
    }

    public List<TaskExecution> convertToTaskExecution(Iterable<BindingSet> bindingSets){
        return taskExecutionStatisticsFromPartsMapper.get().convert(bindingSets);
    }

    protected Date parseDate(String dateString){
        return DateUtils.parseDate(DateUtils.df.get(), dateString);
    }

    protected LocalDate parseLocalDate(String lacalDateString){
        return LocalDate.parse(lacalDateString);
    }



    public WorkpackageDAO(EntityManager em, Rdf4JDao rdf4JDao) {
        super(Workpackage.class, em, rdf4JDao);
    }

    public List<Workpackage> findAllClosed() {
        return load(wpIds.apply(WP_IDS_CLOSED), null);
    }

    public List<Workpackage> findAllOpened() {
        return load(wpIds.apply(WP_IDS_OPENED), null);
    }

    public List<Workpackage> findAll() {
        return load(wpIds.apply(WP_IDS_ALL), null);
    }

    public Map<URI, List<URI>> readTaskTypeUsage(){
        return load(wpTaskTypes.get(), null).stream().collect(Collectors.groupingBy(
                p -> p.getLeft(),
                Collectors.mapping(p -> p.getRight(),Collectors.toList()))
        );
    }

    public List<Pair<Workpackage, Integer>> findAllWorkpackageHeaders(){
        return load(wpHeader.get(), null);
    }

    /**
     * Low level method, reads task executions and their work sessions and sets it in the Workpackage.taskExecutions field.
     * This method:
     * - does not set up start and end times of task executions and workpackage start and end time from work sessions
     * - only loads task types of task executions without reference to their task definitions
     * - set aircraft area of WO task executions based on referenced task executions.
     *
     * Use WorkpackageService.readTaskExecutions
     * @param workpackage
     * @see
     */
    public void readWorkparckageTasks(final Workpackage workpackage){
        Bindings bindings = new Bindings();
        bindings.add("wp", workpackage.getEntityURI());
        List<TaskExecution> taskExecutions = load(taskExecutionMapper.get(), bindings);
        workpackage.setTaskExecutions(new HashSet<>(taskExecutions));
        taskExecutions.forEach(te -> te.setWorkpackage(workpackage));
    }
    /**
     * Reads temporal properties of task executions in all workpackages. Temporal properties derived from the task's parts
     * (work sessions) are start, end, workTime and duration. Data are represented as binding sets and are grouped by wp
     * uri string. No conversion to model entities is performed.
     *
     * Limitations - this method does not read other relevant properties of task cards.
     */
    protected Map<String, List<BindingSet>> getTimePropertiesOfWorkparckageTasks(){
        List<BindingSet> bindingSets = load(workpackagesWithTaskExecutionsWithTimeProperties.get(), null);
        return bindingSets.stream().collect(Collectors.groupingBy(b -> b.getValue("wp").stringValue()));
    }

    /**
     * Creates a Workpackage and populates it with task executions and theri temporal properties. The task executions
     * are constructed from binding set cache.
     *
     * Limitations - this method does not read other relevant properties of task cards.
     * @param uri
     */
    public Workpackage getTimePropertiesOfWorkparckageTasks(final URI uri){
        List<BindingSet> taskExecutionBindings = rawWorkpackageTaskTimePropertiesCache.remove(uri.toString());

        if(taskExecutionBindings == null)
            return null;

        Workpackage workpackage = new Workpackage(uri);
        List<TaskExecution> taskExecutions = convertToTaskExecution(taskExecutionBindings);

        workpackage.setTaskExecutions(new HashSet<>(taskExecutions));
        taskExecutions.forEach(te -> te.setWorkpackage(workpackage));

        return workpackage;
    }

    public List<Pair<Workpackage, Double>> findSimilarWorkpackages(Workpackage workpackage){
        Bindings bindings = new Bindings();
        bindings.add("wpA", workpackage.getEntityURI());
        return load(similarWPsMapper.get(), bindings);
    }

    public List<Pair<Workpackage, Double>> findWorkpackagesWithSimilarScopes(Workpackage workpackage, Set<TaskType> taskTypes){
        Bindings bindings = new Bindings();
        bindings.add("wpA", workpackage.getEntityURI());
        List<Bindings> values = taskTypes.stream()
                .map(t -> Bindings.newBindings().add("taskType", t.getEntityURI()))
                .collect(Collectors.toList());
        return load(similarWPScopesMapper.get(), values, bindings);
    }
}
