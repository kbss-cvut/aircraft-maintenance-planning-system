package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.io.EntityRegistry;
import cz.cvut.kbss.amaplas.model.*;
import cz.cvut.kbss.amaplas.model.values.DateParserSerializer;
import cz.cvut.kbss.amaplas.persistence.dao.mapper.Bindings;
import cz.cvut.kbss.amaplas.persistence.dao.mapper.QueryResultMapper;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.query.BindingSet;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Repository
public class WorkpackageDAO extends BaseDao<Workpackage>{

    protected static final URI p_workpackage_end_time = URI.create(Vocabulary.s_p_workpackage_end_time);

    public static final String WP_TASK_TYPES = "/queries/analysis/wp-task-types.sparql";

    public static final String WP_TASK_EXECUTIONS = "/queries/analysis/task-executions.sparql";

    public static final String TASK_EXECUTION_STATISTICS_FROM_PARTS = "/queries/analysis/task-execution-statistics-from-parts.sparql";
    public static final String WP_SIMILAR_WPS = "/queries/analysis/similar-wps.sparql";


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

    protected Supplier<QueryResultMapper<TaskExecution>> taskExecutionAndSessionMapper = () -> new QueryResultMapper<>(WP_TASK_EXECUTIONS) {
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
            String tType = optValue("tType", null);
            URI taskTypeUri = null;
            String defaultTaskType = def;
            String defaultTaskCategory = def;
            Double averageTime = null;

            if(tType != null){
                taskTypeUri = URI.create(tType);
                Matcher m = taskTypeIRIPattern.matcher(tType);
                if(m.find()) {
                    defaultTaskCategory = taskCategories.getOrDefault(m.group(1), def);
                    defaultTaskType = m.group(2);
                }
                averageTime = optValue("averageTime", Double::parseDouble, null);
            }
            TaskType taskType = new TaskType(
                    optValue("type", defaultTaskType),
                    optValue( "typeLabel", defaultTaskType),
                    optValue( "taskcat", defaultTaskCategory),
                    optValue("acmodel", def)
            );
            taskType.setEntityURI(taskTypeUri);
            taskType.setAverageTime(averageTime);

            TaskExecution taskExecution = null;
            String taskExecutionURI = optValue("tt", null);
            if(taskExecutionURI != null) {
                taskExecution = registry.getOrCreate(taskExecutionURI, TaskExecution::new, TaskExecution.class);
                taskExecution.setTaskType(taskType);
                taskExecution.setEntityURI(URI.create(taskExecutionURI));
                String referencedTaskURI = optValue( "referencedTask", null);
                TaskExecution referencedTask = referencedTaskURI == null ? null : registry.getOrCreate(referencedTaskURI, TaskExecution::new, TaskExecution.class);
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
                taskExecution.setStart(manValue("start", s -> parseDate(s)));
                taskExecution.setEnd(manValue("end", s -> parseDate(s)));
                taskExecution.setWorkTime(manValue("workTime", Long::parseLong));
                taskExecution.setDur(manValue("dur", Long::parseLong));
            }
            return taskExecution;
        }
    };

    protected Supplier<QueryResultMapper<TaskExecution>> taskExecutionMapper = () -> new QueryResultMapper<>(TASK_EXECUTION_STATISTICS_FROM_PARTS) {
        @Override
        public TaskExecution convert() {
            TaskType taskType = manValue("taskType", s -> new TaskType(URI.create(s)));
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
    protected Date parseDate(String dateString){
        return DateParserSerializer.parseDate(DateParserSerializer.df.get(), dateString);
    }

    protected LocalDate parseLocalDate(String lacalDateString){
        return LocalDate.parse(lacalDateString);
    }



    public WorkpackageDAO(EntityManager em, Rdf4JDao rdf4JDao) {
        super(Workpackage.class, em, rdf4JDao);
    }

    public List<Workpackage> findAllClosed() {
        return getEm().createNativeQuery(
              "SELECT ?w WHERE {\n" +
                            "?w a ?type; \n" +
                            "?endTimeProperty ?closeDate. \n" +
                            "FILTER(xsd:dateTime(?closeDate) < ?now)\n" +
                        "}" ,
                        URI.class
                )
                .setParameter("type", getTypeUri())
                .setParameter("endTimeProperty", p_workpackage_end_time)
                .setParameter("now", LocalDate.now())
                .getResultList().stream()
                .map(u -> new Workpackage(u))
                .collect(Collectors.toList());
    }


    //TODO - optimize, create a QueryResultMapper, use it with load() to read WPs with uri and id
    public List<Workpackage> findAllOpened() {
        return getEm().createNativeQuery("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
                                "SELECT ?w WHERE { \n" +
                                "?w a ?type; \n" +
                                "OPTIONAL { \n" +
                                "   ?w ?endTimeProperty ?_closeDate. \n" +
                                "BIND(xsd:dateTime(?_closeDate) as ?closeDate) \n" +
                                "} \n" +
                                "FILTER(!BOUND(?closeDate) || ?closeDate > ?now) \n" +
                                "}" ,
                        URI.class
                )
                .setParameter("type", getTypeUri())
                .setParameter("endTimeProperty", p_workpackage_end_time)
                .setParameter("now", LocalDate.now())
                .getResultList().stream()
                .map(u -> new Workpackage(u))
                .collect(Collectors.toList());
    }

    public Map<URI, List<URI>> readTaskTypeUsage(){
        return load(wpTaskTypes.get(), null).stream().collect(Collectors.groupingBy(
                p -> p.getLeft(),
                Collectors.mapping(p -> p.getRight(),Collectors.toList()))
        );
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
        List<TaskExecution> taskExecutions = load(taskExecutionAndSessionMapper.get(), bindings);
        workpackage.setTaskExecutions(new HashSet<>(taskExecutions));
        taskExecutions.forEach(te -> te.setWorkpackage(workpackage));
    }

    /**
     * Reads temporal properties of task executions in the input workpackage. Temporal properties derived from the task's parts
     * (work sessions) are start, end, workTime and duration.
     *
     * Limitations - this method does not read other relevant properties of task cards.
     * @param workpackage
     */
    public void readTimePropertiesOfWorkparckageTasks(final Workpackage workpackage, final Workpackage workpackageA){
        Bindings bindings = new Bindings();
        bindings.add("wp", workpackage.getEntityURI());
        List<TaskExecution> taskExecutions = load(taskExecutionMapper.get(), bindings);
        workpackage.setTaskExecutions(new HashSet<>(taskExecutions));
        taskExecutions.forEach(te -> te.setWorkpackage(workpackage));
    }

    public void readTimePropertiesOfWorkparckageTasks(final Collection<Workpackage> workpackages, Workpackage workpackageA) {
        for(Workpackage wp : workpackages){
            readTimePropertiesOfWorkparckageTasks(wp, workpackageA);
        }
    }

    public List<Pair<Workpackage, Double>> findSimilarWorkpackages(Workpackage workpackage){
        Bindings bindings = new Bindings();
        bindings.add("wpA", workpackage.getEntityURI());
        return load(similarWPsMapper.get(), bindings);
    }

    //  TODO - delete
//    // DONE TODO - set aircraft area of WO execution based on referenced task execution.
//    public static void setAreasInWOsFromReferenceTask(List<WorkSession> workSessions){
//        Map<URI, List<WorkSession>> map = workSessions.stream().filter(s -> s.getTaskExecution() != null)
//                .collect(Collectors.groupingBy(s -> s.getTaskExecution().getEntityURI()));
//        for(List<WorkSession> sessions: map.values()) {
//            String taskCategory = sessions.stream().map(s -> s.getTaskExecution().getTaskType().getTaskcat()).filter(c -> c != null).findFirst().orElse(null);
//            if(taskCategory != null && !taskCategory.contains("work-order"))
//                continue;
//
//            List<String> referencedTasks = sessions.stream()
//                    .map(s -> s.referencedTasks).filter(ts -> ts != null).flatMap(rt -> rt.stream())
//                    .distinct().collect(Collectors.toList());
//            if(referencedTasks.isEmpty())
//                continue;
//            List<Result> referencedTaskSessions = map.get(referencedTasks.get(0));
//            if(referencedTaskSessions == null || referencedTaskSessions.isEmpty())
//                continue;
//
//            String area = referencedTaskSessions.stream().map(r -> r.taskType)
//                    .filter(t -> t != null && t.getDefinition() != null && t.getDefinition().getArea() != null)
//                    .map(t -> t.getDefinition().getArea()).findFirst()
//                    .orElse(null);
//            if(area != null)
//                sessions.forEach(s -> s.taskType.setArea(area));
//        }
//    }

}
