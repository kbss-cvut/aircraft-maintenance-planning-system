package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.io.EntityRegistry;
import cz.cvut.kbss.amaplas.model.*;
import cz.cvut.kbss.amaplas.model.values.DateParserSerializer;
import cz.cvut.kbss.amaplas.persistence.dao.mapper.Bindings;
import cz.cvut.kbss.amaplas.persistence.dao.mapper.QueryResultMapper;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Repository
public class WorkpackageDAO extends BaseDao<Workpackage>{

    protected static final URI p_workpackage_end_time = URI.create(Vocabulary.s_p_workpackage_end_time);

    public static final String WP_TASK_TYPES = "/queries/analysis/wp-task-types.sparql";

    public static final String WP_TASK_EXECUTIONS = "/queries/analysis/task-executions.sparql";


    protected QueryResultMapper<Pair<URI, URI>> wpTaskTypes = new QueryResultMapper<>(WP_TASK_TYPES) {
        @Override
        public Pair<URI, URI> convert() {
            return Pair.of(manValue("wp", URI::create),manValue("taskType", URI::create));
        }
    };

    protected QueryResultMapper<TaskExecution> taskExecutionMapper = new QueryResultMapper<>(WP_TASK_EXECUTIONS) {
        protected Pattern taskTypeIRIPattern = Pattern.compile("task-type--([^-]+)--(.+)");
        protected Pattern mechanicIRI_IDPattern = Pattern.compile("^.+/mechanic--(.+)$");
        protected HashMap<String, String > taskCategories = new HashMap<>(){
            {
                put("TC", "task-card");
                put("M", "maintenance-work-order");
                put("S", "scheduled-work-order");
            }
        };
        protected EntityRegistry registry;

        @Override
        public List<TaskExecution> convert(TupleQueryResult tupleQueryResult) {
            registry = new EntityRegistry();
            return super.convert(tupleQueryResult);
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
                taskExecution.setEstMin(optValueNonString("estMin", v -> ((Literal)v).longValue(), null));
            }
            // create mechanic
            String mechanicIRI = optValue( "w", null);
            String mechanicID = optValue( "wId", null);
            String mechanicLabel = optValue( "wLabel", null);
            Mechanic mechanic = null;
            if(mechanicIRI != null){
                mechanic = registry.getOrCreate(mechanicIRI, Mechanic::new, Mechanic.class);
                mechanic.setEntityURI(URI.create(mechanicIRI));
                if(mechanicID == null){
                    Matcher m = mechanicIRI_IDPattern.matcher(mechanicIRI);
                    if(m.matches()){
                        mechanicID = m.group(1);
                    }
                }
                mechanic.setId(mechanicID);
                mechanic.setTitle(mechanicLabel != null ? mechanicLabel : mechanicID);
            }

            // create a work session record
            String sessionURI = optValue("t", null);

            WorkSession workSession = sessionURI == null ? new WorkSession() : registry.getOrCreate(sessionURI, WorkSession::new, WorkSession.class);
            workSession.setEntityURI(URI.create(sessionURI));
            workSession.setTaskExecution(taskExecution);
            workSession.setMechanic(mechanic);

            String scopeAbbreviation = optValue("scope", def);
            MaintenanceGroup scope = scopeAbbreviation == null ? null : registry.getOrCreate(scopeAbbreviation, MaintenanceGroup::new, MaintenanceGroup.class);
            if(scope != null) {
                scope.setEntityURI(optValue("scopeGroup", URI::create, null));
                scope.setAbbreviation(scopeAbbreviation);
                scope.setTitle(optValue("scopeLabel", null));

            }

            workSession.setScope(scope);
//            workSession.sedate = optValue( "date", def);

            String start = optValue( "start", null);
            String end = optValue( "end", null);
            if(start != null )
                workSession.setStart(DateParserSerializer.parseDate(DateParserSerializer.df.get(), start));
            if(end != null)
                workSession.setEnd(DateParserSerializer.parseDate(DateParserSerializer.df.get(), end));

            workSession.setDur(optValueNonString("dur", v -> ((Literal)v).longValue(), null));
            return taskExecution;
        }
    };


    public WorkpackageDAO(EntityManager em) {
        super(Workpackage.class, em);
    }

    public List<Workpackage> findAllClosed() {
        return getEm().createNativeQuery(
              "SELECT ?w WHERE {\n" +
                            "?w a ?type; \n" +
                            "?endTimeProperty ?closeDate. \n" +
                            "FILTER(?closeDate < ?now)\n" +
                        "}" ,
                        Workpackage.class
                )
                .setParameter("type", getTypeUri())
                .setParameter("endTimeProperty", p_workpackage_end_time)
                .setParameter("now", LocalDate.now()).getResultList();
    }

    public List<Workpackage> findAllOpened() {
        return getEm().createNativeQuery(
                        "SELECT ?w WHERE {\n" +
                                "?w a ?type; \n" +
                                "?endTimeProperty ?closeDate. \n" +
                                "FILTER(?closeDate > ?now)\n" +
                                "}" ,
                        Workpackage.class
                )
                .setParameter("type", getTypeUri())
                .setParameter("endTimeProperty", p_workpackage_end_time)
                .setParameter("now", LocalDate.now()).getResultList();
    }

    public Map<URI, List<URI>> readTaskTypeUsage(){
        return load(wpTaskTypes, null).stream().collect(Collectors.groupingBy(
                p -> p.getLeft(),
                Collectors.mapping(p -> p.getRight(),Collectors.toList()))
        );
    }

    public void readWorkparckageTasks(Workpackage workpackage){
        Bindings bindings = new Bindings();
        bindings.add("wp", workpackage.getEntityURI());
        List<TaskExecution> taskExecutions = load(taskExecutionMapper, bindings);

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
