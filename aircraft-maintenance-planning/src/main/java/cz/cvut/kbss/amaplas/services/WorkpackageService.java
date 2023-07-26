package cz.cvut.kbss.amaplas.services;


import cz.cvut.kbss.amaplas.model.TaskExecution;
import cz.cvut.kbss.amaplas.model.TaskType;
import cz.cvut.kbss.amaplas.model.Workpackage;
import cz.cvut.kbss.amaplas.persistence.dao.WorkSessionDao;
import cz.cvut.kbss.amaplas.persistence.dao.WorkpackageDAO;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

@Service
public class WorkpackageService extends BaseService {
    private static final Logger LOG = LoggerFactory.getLogger(WorkpackageService.class);

    protected final WorkpackageDAO workpackageDAO;
    protected final TaskTypeService taskTypeService;
    protected final WorkSessionDao workSessionDao;
    protected boolean refreshingCache = false;

    protected Map<URI,Workpackage> workpackageTaskTimePropertiesCache;

    public WorkpackageService(WorkpackageDAO workpackageDAO, TaskTypeService taskTypeService, WorkSessionDao workSessionDao) {
        this.workpackageDAO = workpackageDAO;
        this.taskTypeService = taskTypeService;
        this.workSessionDao = workSessionDao;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init(){
        resetCache();
    }

    public void resetCache(){
        setRefreshingCache(true);
        LOG.info("reset caches");
        taskTypeService.resetCache();
        LOG.info("reset caches - reset workpackages with task with time properties caches");
        workpackageDAO.resetCache();
        workpackageTaskTimePropertiesCache = new HashMap<>();
        LOG.info("finished reset caches");
        setRefreshingCache(false);
    }
    public synchronized boolean isRefreshingCache() {
        return refreshingCache;
    }

    public synchronized void setRefreshingCache(boolean refreshingCache) {
        this.refreshingCache = refreshingCache;
    }

    public List<Pair<Workpackage, Integer>> getHeaders(){
        return workpackageDAO.findAllWorkpackageHeaders();
    }

    public List<Workpackage> getAllWorkpackages(){
        return workpackageDAO.findAll();
    }

    public List<Workpackage> getClosedWorkpackages(){
        return workpackageDAO.findAllClosed();
    }

    public List<Workpackage> getOpenedWorkpackages(){
        return workpackageDAO.findAllOpened();
    }

    public Workpackage getWorkpackage(String workpackageId) {
        Workpackage wp = workpackageDAO.findById(workpackageId).orElse(null);
        return wp;
    }

    public Workpackage getWorkpackageWithExecutionsAndSessions(String workpackageId){
        LOG.info("load WP with Id {}", workpackageId);
        Workpackage wp = getWorkpackage(workpackageId);
        if (wp == null) {
            LOG.warn("Could not find WP with id \"{}\" ", workpackageId);
            return null;
        }
        readTaskExecutions(wp);
        if (wp.getTaskExecutions() == null || wp.getTaskExecutions().isEmpty()) {
            LOG.warn("Could not find any sessions or TC executions for WP \"{}\" ", workpackageId);
            return null;
        }
        return wp;
    }

    /**
     * Reads task executions and their sessions for the provided workpackage. Replaces referenced task types so that they
     * contain transient fields.
     */
    public void readTaskExecutions(Workpackage workpackage){
        workpackageDAO.readWorkparckageTasks(workpackage);
        workSessionDao.loadWorkSessions(workpackage);

        Set<TaskExecution> taskExecutions = workpackage.getTaskExecutions();

        taskExecutions.stream().forEach(this::setStartAndEndFromParts);
        Date start = min(taskExecutions.stream().map(e -> e.getStart()).filter(d -> d != null));
        Date end = max(taskExecutions.stream().map(e -> e.getEnd()).filter(d -> d != null));
        workpackage.setStart(start);
        workpackage.setEnd(end);

        // set task types
        setTaskTypesOfTaskExecutions(workpackage);

        // set aircraft area of WO execution based on referenced task execution
        for (TaskExecution te : workpackage.getTaskExecutions()){
            TaskType taskType = te.getTaskType(); //task types are not the task types loaded from
            if(taskType == null || taskType.getTaskcat() == null || !taskType.getTaskcat().contains("work-order"))
                continue;
            if(te.getReferencedTasks() == null || te.getReferencedTasks().isEmpty())
                continue;
            te.getReferencedTasks().stream().limit(1)// select one
                    .forEach(rt -> te.getTaskType().setArea(
                            Optional.ofNullable(rt.getTaskType())
                                    .map(t -> t.getDefinition() != null ? t.getDefinition() : t)
                                    .map(t -> t.getArea()).orElse(null))
                    );
        }
    }

    /**
     * Read URIs of similar workpackages and the similarity score
     * @param workpackage
     * @return
     */
    public List<Pair<Workpackage, Double>> findSimilarWorkpackages(Workpackage workpackage){
        return workpackageDAO.findSimilarWorkpackages(workpackage);
    }

    public List<Pair<Workpackage, Double>> findWorkpackagesWithSimilarScopes(Workpackage workpackage, Set<TaskType> taskTypes){
        return workpackageDAO.findWorkpackagesWithSimilarScopes(workpackage, taskTypes);
    }

    public Workpackage findById(String id){
        Workpackage workpackage = workpackageDAO.findById(id).orElse(null);
        if(workpackage != null){
            readTaskExecutions(workpackage);
        }
        return workpackage;
    }

    /**
     * Replaces task types instances in task executions with task types from cache.
     * @param workpackage
     */
    public void setTaskTypesOfTaskExecutions(Workpackage workpackage){
        workpackage.getTaskExecutions().stream()
                .filter(te -> te.getTaskType() != null && te.getTaskType().getEntityURI() != null)
                .forEach(te -> Optional
                        .ofNullable(taskTypeService.getCahcedTaskType(te.getTaskType().getEntityURI()))
                        .ifPresent(cachedType -> {
                            cachedType.setAverageTime(te.getTaskType().getAverageTime());
                            te.setTaskType(cachedType);
                        }));
    }



    public void setStartAndEndFromParts(TaskExecution taskExecution) {
        if (taskExecution.getWorkSessions() == null)
            return;
        Date start = min(taskExecution.getWorkSessions().stream().map(w -> w.getStart()).filter(d -> d != null));
        Date end = max(taskExecution.getWorkSessions().stream().map(w -> w.getEnd()).filter(d -> d != null));

        if (start != null && end != null) {
            taskExecution.setStart(start);
            taskExecution.setEnd(end);
            taskExecution.setDur(end.getTime() - start.getTime());
            Long workTime = taskExecution.getWorkSessions().stream().filter(s -> s.getDur() != null)
                    .mapToLong(s -> s.getDur()).sum();
            taskExecution.setWorkTime(workTime);
        }
    }

    public Workpackage getWorkpackageWithTemporalProperties(URI uri){
        Workpackage workpackage = workpackageTaskTimePropertiesCache.get(uri);
        if(workpackage == null){
            workpackage = workpackageDAO.getTimePropertiesOfWorkparckageTasks(uri);
            workpackageTaskTimePropertiesCache.put(uri, workpackage);
        }

        return workpackage;
    }

    public Date min(Stream<Date> dateStream){
        return dateStream.filter(d -> d != null)
                .sorted(Comparator.comparing(d -> d.getTime()))
                .findFirst().orElse(null);
    }

    public Date max(Stream<Date> dateStream){
        return dateStream.filter(d -> d != null)
                .sorted(Comparator.comparing((Date d) -> d.getTime()).reversed())
                .findFirst().orElse(null);
    }
}
