package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.config.ConfigProperties;
import cz.cvut.kbss.amaplas.io.SparqlDataReader;
import cz.cvut.kbss.amaplas.io.SparqlDataReaderRDF4J;
import cz.cvut.kbss.amaplas.model.Result;
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

@Service
public class RevisionHistory {
    private static final Logger LOG = LoggerFactory.getLogger(RevisionHistory.class);

    protected final ConfigProperties config;
    protected ConfigProperties.Repository repoConfig;

    private Map<String, List<Result>> historyCache;

    public RevisionHistory(ConfigProperties config) {
        this.config = config;
        this.repoConfig = config.getRepository();
    }

    public List<String> getAllRevisions(){
        return new SparqlDataReaderRDF4J().readRowsAsStrings(SparqlDataReader.WP, repoConfig.getUrl(), repoConfig.getUsername(), repoConfig.getPassword());
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
        return plan.stream()
                .collect(Collectors.groupingBy(groupId))
                .entrySet().stream()
                .map(pe -> pe.getValue().stream()
                        .collect(Collectors.minBy(order)).orElse(null))
                .sorted(order)
                .collect(Collectors.toList());
    }
}
