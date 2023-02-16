package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.config.props.ConfigProperties;
import cz.cvut.kbss.amaplas.config.props.Repository;
import cz.cvut.kbss.amaplas.io.SparqlDataReader;
import cz.cvut.kbss.amaplas.io.SparqlDataReaderRDF4J;
import cz.cvut.kbss.amaplas.model.Result;
import cz.cvut.kbss.amaplas.model.Workpackage;
import cz.cvut.kbss.amaplas.persistence.dao.WorkpackageDAO;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RevisionHistory {
    private static final Logger LOG = LoggerFactory.getLogger(RevisionHistory.class);

    protected final ConfigProperties config;
    protected final TaskTypeService taskTypeService;

    protected final WorkpackageDAO workpackageDAO;
    protected Repository repoConfig;

    private Map<String, List<Result>> historyCache;

    public RevisionHistory(ConfigProperties config, TaskTypeService taskTypeService, WorkpackageDAO workpackageDAO) {
        this.config = config;
        this.repoConfig = config.getRepository();
        this.taskTypeService = taskTypeService;
        this.workpackageDAO = workpackageDAO;
    }

    public List<String> getAllRevisions(){
        return new SparqlDataReaderRDF4J().readRowsAsStrings(SparqlDataReader.WP_HEADER, repoConfig.getUrl(), repoConfig.getUsername(), repoConfig.getPassword());
    }

    public List<String> getRevisionIds(){
        return new SparqlDataReaderRDF4J().readRowsAsStrings(SparqlDataReader.WP_IDS, repoConfig.getUrl(), repoConfig.getUsername(), repoConfig.getPassword());
    }
    /**
     *
     * @param refreshCash - if true cache is refreshed
     * @return
     */
    public Map<String, List<Result>> getAllClosedRevisionsWorkLog(boolean refreshCash){
        LOG.info("fetching revision work log from {}", repoConfig.getUrl());
        if(refreshCash || historyCache == null){
            historyCache = loadAllClosedRevisionsWorkLog();
        }
        return historyCache;
    }

    /**
     * Loads closed revisions in map.
     * - The key is the id of the revision and
     * - the value is the list of work sessions of the revision ordered by the work session start timestamp.
     * @return
     */
    private Map<String, List<Result>> loadAllClosedRevisionsWorkLog(){
        // load task card definitions
        LOG.info("fetching revision work log from {}", repoConfig.getUrl());
        List<String> revisionIds = getRevisionIds();
        ValueFactory vf = SimpleValueFactory.getInstance();
        Map<String, List<Result>> closedRevisions = new HashMap<>();
        for(String wpId : revisionIds) {
            Map<String, Value> bindings = new HashMap<>();
            bindings.put("wp", vf.createLiteral(wpId));
            List<Result> results = new SparqlDataReaderRDF4J().readSessionLogsWithNamedQuery(SparqlDataReader.DA_TASK_SO_WITH_WP_SCOPE,
                    bindings,
                    repoConfig.getUrl(), repoConfig.getUsername(), repoConfig.getPassword());
            closedRevisions.put(wpId, results);
        }

        List<Result> workSessions = closedRevisions.values().stream().flatMap(l -> l.stream()).collect(Collectors.toList());
        // normalize and fix referenced task types
        Result.normalizeTaskTypes(workSessions);
        workSessions.stream().filter(r -> r.taskType != null && "task-card".equals(r.taskType.getTaskcat())).forEach(r -> r.taskType.setDefinition(taskTypeService.getMatchingTaskTypeDefinition(r.taskType)));
        // add task labels if missing
        taskTypeService.getTaskTypes().stream()
                .filter(t -> t.getCode() == t.getTitle()
                        && t.getDefinition() != null
                        && t.getDefinition().getTitle() != null
                        && !t.getDefinition().getTitle().trim().isEmpty())
                .forEach(t -> {
                    t.setTitle(t.getDefinition().getTitle());
                    t.setViewLabel(t.getCode() + "\n" + t.getTitle());
                });

        LOG.debug("sorting fetched revisions");
        // make sure the work sessions are ordered by start time
        closedRevisions.entrySet().forEach(e -> e.getValue().sort(Comparator.comparing(r -> r.start != null ? r.start.getTime() : -1L)));
        return closedRevisions;
    }

    public List<Result> getClosedRevisionWorkLog(String revisionId){
        Map<String, List<Result>> historyCache = getAllClosedRevisionsWorkLog(false);
        return historyCache.get(revisionId);
    }

    /**
     * Returns the sessions of main scopes ordered by their start time.
     * @return
     */
    public Map<String, List<Result>> getMainScopeSessionsByRevisionId(){
        Map<String, List<Result>> workLog =  getAllClosedRevisionsWorkLog(false);
        Map<String, List<Result>> filteredPlans = new HashMap<>();
        workLog.entrySet().forEach(e ->
                filteredPlans.put(e.getKey(), e.getValue().stream()
                        .filter(Result.isMainScopeSession)
                        .collect(Collectors.toList()))
        );

        Map<String, List<Result>> taskStartPlans = toRevisionPlans(filteredPlans, Result.startTimeMilSec, null);
        return taskStartPlans;
    }


    /**
     * Returns the start sessions or main scopes ordered by their start time. A start session is a session that starts a specific task
     * execution. The result contains only work sessions of closed revisions.
     * @return
     */
    public Map<String, List<Result>> getStartSessionsOfMainScopeInClosedRevisions(){
        Map<String, List<Result>> workLog =  getAllClosedRevisionsWorkLog(false);
        Map<String, List<Result>> filteredPlans = new HashMap<>();
        workLog.entrySet().forEach(e ->
                filteredPlans.put(e.getKey(), e.getValue().stream()
                        .filter(Result.isMainScopeSession)
                        .collect(Collectors.toList()))
        );
        Map<String, List<Result>> taskStartPlans = toRevisionPlans(filteredPlans, Result.startTimeMilSec, Result.key_TaskTypeCode_ScopeCode);
        return taskStartPlans;
    }

    /**
     * Create a task plan ordered by the order comparator taking the minimum element(s) of each groupId
     * @param plans
     * @param orderBy
     * @param groupId
     */
    public Map<String, List<Result>> toRevisionPlans(Map<String, List<Result>> plans, Function<Result, Long> orderBy, Function<Result, Object> groupId){
        Map<String, List<Result>> planHistory = new HashMap<>();
        plans.entrySet().stream()
                // leave only starts of different tasks types executed by different scope groups in each plan
                .map(e -> Pair.of(e.getKey(), toRevisionPlan(e.getValue(), orderBy, groupId)))
                .forEach(p -> planHistory.put(p.getKey(), p.getRight()));
        return planHistory;
    }

    public List<Result> toRevisionPlan(Collection<Result> plan, Function<Result, Long> orderBy, Function<Result, Object> groupId){
        Comparator<Result> order = Comparator.comparing(orderBy);
        Stream<Result> sequence = plan.stream().filter(r -> r.start != null).sorted(order);
        if(groupId != null)
            sequence = sequence
                    .collect(Collectors.groupingBy(groupId, LinkedHashMap::new, Collectors.toList()))
                    .entrySet().stream()
                    .map(pe -> pe.getValue().get(0));

        return sequence.collect(Collectors.toList());
    }

    public Workpackage getWorkpackage(String workpackageId){
        Workpackage wp = workpackageDAO.findById(workpackageId).orElse(null);
        return wp;
    }

    public List<Workpackage> getWorkpackages(){
        return workpackageDAO.findAll();
    }

    public List<Workpackage> getClosedWorkpackages(){
        return workpackageDAO.findAllClosed();
    }

    public List<Workpackage> getOpenedWorkpackages(){
        return workpackageDAO.findAllOpened();
    }
}
