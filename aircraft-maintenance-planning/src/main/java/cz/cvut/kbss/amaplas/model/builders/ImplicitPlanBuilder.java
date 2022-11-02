package cz.cvut.kbss.amaplas.model.builders;

import cz.cvut.kbss.amaplas.model.*;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ImplicitPlanBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(ImplicitPlanBuilder.class);

    private final ModelFactory modelFactory = new ModelFactory();
    protected Map<Object, Map<String, AbstractEntity>> entityMaps = new HashMap<>();
    protected Defaults defaults = new Defaults();

    /**
     * Add implicit plan restrictions to the provided revisionPlan.
     *
     * For each TaskPlan and its corresponding TaskType
     * definitions a 0 to 3 task restrictionPlans are constructed. The restrictionPlans' schedule corresponds to the
     * schedule of the TaskPlan and restrictions and their subject corresponds to those defined in the TaskType
     * definition.
     *
     * Each set of restriction plans with the same subject (i.e. El. power, Hyd. power and Jack) is reduced by
     * merging adjacent plans with identical restrictions, e.g. OFF and OFF. Finally, the reduced sets are added to the
     * revisionPlan.
     *
     * @param revisionPlan
     */
    public void addRestrictionPlans(RevisionPlan revisionPlan){
        LOG.debug("addRestrictionPlans to \"{}\"", revisionPlan.getTitle());
        // create atomic restriction types
        List<RestrictionPlan> restrictionPlans = revisionPlan.streamPlanParts()
                .filter(p -> p instanceof TaskPlan)
                .map(p -> (TaskPlan)p)
                .filter(p -> p.getTaskType() != null && p.getTaskType().getDefinition() != null)
                .flatMap(
                        p -> getRestrictionPlans(p, p.getTaskType().getDefinition()).stream()
                ).collect(Collectors.toList());

        // group restriction plans according to subject
        Map<URI, List<RestrictionPlan>> map = restrictionPlans.stream().collect(Collectors.groupingBy(p -> p.getRestrictions().iterator().next().getSubject()));
        // sort each of the lists in the map according to
        map.entrySet().forEach(e -> e.getValue().sort(
                Comparator
                        .comparing((RestrictionPlan p) -> p.getStartTime().getTime())
                        .thenComparing((RestrictionPlan p) -> p.getEndTime().getTime())
        ));
        // simplify restriction plans
        map.values().stream().flatMap(p -> {
            List<RestrictionPlan> l = simplifyPlans(p);
            System.out.println(String.format("simplify restriction plans - # non simplified %d, # simplified %d ", p.size(), l.size()));
            return l.stream();
        }).forEach(revisionPlan::addPlanPart);
    }

    /**
     * This method reduces a list of plan restrictions by merging adjacent plans with identical restrictions.
     *
     * This method assumes that originalPlans list is ordered according to startTime and then to endTime.
     * @param originalPlans
     * @return
     */
    protected List<RestrictionPlan> simplifyPlans(List<RestrictionPlan> originalPlans){
        LOG.debug("merge list of restriction plans");
        List<RestrictionPlan> plans = new ArrayList<>();
            int i = 0, j = 0;
            RestrictionPlan p1, p2 = null;
            for(; i < originalPlans.size(); i = j){
                p1 = originalPlans.get(i);
                for(j = i + 1; j < originalPlans.size(); j ++) {
                    p2 = originalPlans.get(j);
                    // check if plans can be merged
                    boolean propositionMatches = p1.getTitle().equals(p2.getTitle());
                    if(!propositionMatches)
                        break;
                    if(p1.getEndTime().getTime() < p2.getEndTime().getTime()) {
                        // merge plans
                        p1.getRequiringPlans().addAll(p2.getRequiringPlans());
                        p1.setEndTime(p2.getEndTime());
                    }
                }
                plans.add(p1);
            }
            if(originalPlans.size() > j && p2 != null)
                plans.add(p2);
        return plans;
    }

    /**
     * Constructs a list of restrictionPlans for the given taskPlan and its corresponding taskType. The list is filled
     * with restriction plan for each available subject restriction in the TaskType.
     *
     * @param taskPlan
     * @param taskType
     * @return
     */
    public List<RestrictionPlan> getRestrictionPlans(TaskPlan taskPlan, TaskType taskType){
        return Stream.<Pair<String, Function<TaskType, String>>>of(
                Pair.of(Vocabulary.s_c_el_dot__power, TaskType::getElPowerRestrictions),
                Pair.of(Vocabulary.s_c_hyd_dot__power, TaskType::getHydPowerRestrictions),
                Pair.of(Vocabulary.s_c_jack, TaskType::getJackRestrictions))
                .filter(p -> p.getValue().apply(taskType) != null && !p.getValue().apply(taskType).isBlank() && !p.getValue().apply(taskType).trim().equals("/"))
                .map(
                        p -> getRestrictionPlan(taskPlan, taskType, p.getValue().apply(taskType), URI.create(p.getKey()))
                )
                .collect(Collectors.toList());
    }

    /**
     * Construct restrictionPlan for the given arguments
     * @param taskPlan
     * @param taskType
     * @param restrictionProposition
     * @param restrictionSubject
     * @return
     */
    public RestrictionPlan getRestrictionPlan(TaskPlan taskPlan, TaskType taskType, String restrictionProposition, URI restrictionSubject){
        Restriction restriction = getRestriction(restrictionProposition, restrictionSubject);
        RestrictionPlan restrictionPlan = new RestrictionPlan();
        restrictionPlan.setId(modelFactory.generateId());
        restrictionPlan.setTitle(getRestrictionSubjectLabel(restrictionSubject) + " " + restrictionProposition);
        restrictionPlan.setRestrictions(new HashSet<>());
        restrictionPlan.getRestrictions().add(restriction);
        restrictionPlan.setRequiringPlans(new HashSet<>());
        restrictionPlan.getRequiringPlans().add(taskPlan);
        applyTemporalValues(taskPlan, restrictionPlan);

        return restrictionPlan;
    }

    /**
     * Set temporal fields from <code>from</code>  to <code>to</code>
     * @param from
     * @param to
     */
    public void applyTemporalValues(AbstractPlan from, AbstractPlan to){
        to.setStartTime(from.getStartTime());
        to.setEndTime(from.getEndTime());
        to.setDuration(from.getDuration());
        to.setPlannedStartTime(from.getPlannedStartTime());
        to.setPlannedEndTime(from.getPlannedEndTime());
        to.setPlannedDuration(from.getPlannedDuration());
    }

    /**
     * Construct a restriction from the given restrictionProposition and restrictionSubject
     * @param restrictionProposition
     * @param restrictionSubject
     * @return
     */
    public Restriction getRestriction(String restrictionProposition, URI restrictionSubject){
        String restrictionId = DigestUtils.md5Hex(restrictionSubject.toString() + restrictionProposition);
        return getEntity(restrictionId, "restriction", () -> {
            Restriction restriction = new Restriction();
            restriction.setId(restrictionId);
            restriction.setSubject(restrictionSubject);
            restriction.setTitle(restrictionProposition);
            return restriction;
        });
    }

    protected String getRestrictionSubjectLabel(URI restrictionSubject){
        switch (restrictionSubject.toString()){
            case Vocabulary.s_c_el_dot__power: return "el. power";
            case Vocabulary.s_c_hyd_dot__power: return "hyd. power";
            case Vocabulary.s_c_jack: return "jack";
            default : return null;
        }
    }

    public PlanningResult createRevision(List<Result> results){
        Aircraft aircraft = results.stream().map(r -> getAircraft(r)).filter(a -> !defaults.isDefault(a)).findFirst().orElse(null);
        if(aircraft == null)
            aircraft = results.stream().map(r -> getAircraft(r)).findFirst().orElse(null);
        // create bottom part of hierarchical plan -  create to task plans
        RevisionPlan revisionPlan = new RevisionPlan();
        String revisionCode = results.stream()
                .map(r -> r.wp)
                .filter(s -> s != null && !s.isEmpty())
                .findFirst().orElse(null);

        Long revisionId = revisionCode != null ? (long)revisionCode.hashCode() : null;
        revisionPlan.setTitle(revisionCode);
        revisionPlan.setId(revisionId);

        revisionPlan.setResource(aircraft);

        PlanningResult result = new PlanningResult(revisionPlan);

        for(Result r : results) {
            PhasePlan phasePlan = getPhasePlan(r, aircraft);
            revisionPlan.getPlanParts().add(phasePlan);

            AircraftArea area = getAircraftArea(r);
            Pair generalTaskPlanContext = Pair.of(phasePlan, area);
            GeneralTaskPlan generalTaskPlan = getGeneralTaskPlanInCtx(r, generalTaskPlanContext);
            phasePlan.getPlanParts().add(generalTaskPlan);

            TaskPlan taskPlan = getTaskPlan(r, generalTaskPlan);
            generalTaskPlan.getPlanParts().add(taskPlan);

            SessionPlan sessionPlan = getSessionPlan(r);
            taskPlan.getPlanParts().add(sessionPlan);

        }
        return result;
    }

    public SessionPlan getSessionPlan(Result r){
        SessionPlan sessionPlan = modelFactory.newSessionPlan(r.start, r.end);
        Mechanic mechanic = getMechanic(r);
        sessionPlan.setResource(mechanic);
        return sessionPlan;
    }

    public Mechanic getMechanic(Result r){
        Mechanic m = r.getMechanic();
        LOG.debug("is mechanic null {}", m == null);
        return m == null ?
                null :
                getEntity(
                    (String)m.getId(),
                    "mechanic",
                    () -> m
                );
    }

    public MaintenanceGroup getMaintenanceGroup(Result r){
        String maintenanceGroupLabel = getMaintenanceGroupLabel(r);
        return getEntity(maintenanceGroupLabel, "maintenance-group", () -> modelFactory.newMaintenanceGroup(maintenanceGroupLabel));
    }

    public MaintenanceGroup getMaintenanceGroupInCtx(Result r, AircraftArea area){
        String maintenanceGroupLabel = getMaintenanceGroupLabel(r);
        Pair pair = Pair.of("group", area);
        MaintenanceGroup maintenanceGroup = getEntity(maintenanceGroupLabel, pair, () -> modelFactory.newMaintenanceGroup(maintenanceGroupLabel));
        return maintenanceGroup;
    }

    public Mechanic getMechanicInCtx(Result r){
        try {

            String mechanicTitle = getMechanicLabel(r);
            String area = getAreaLabel(r);
            return getEntity(
                    mechanicTitle,
                    area,
                    () -> modelFactory.newMechanic(mechanicTitle)
            );
        }catch (Exception e){
            LOG.info("Error getting/creating mechanic", e);
        }
        return null;
    }

    public AircraftArea getAircraftArea(Result r){
        String area = getAreaLabel(r);
        return getEntity(area, "aircraft-area", () -> modelFactory.newAircraftArea(area));
    }

    public AircraftArea getAircraftAreaInCtx(Result r, PhasePlan phasePlan){
        String area = getAreaLabel(r);
        return getEntity(area, phasePlan, () -> modelFactory.newAircraftArea(area));
    }

    public Aircraft getAircraft(Result r){
        String aircraftModel = getAircraftModelLabel(r);
        return getEntity(aircraftModel, "aircraft", () -> modelFactory.newEntity(() -> new Aircraft(), aircraftModel));
    }

    public TaskPlan getTaskPlan(final Result r, GeneralTaskPlan gp){
        TaskType taskType = getTaskType(r).orElse(null);

        final AircraftArea area = getAircraftArea(r);
        TaskPlan taskPlan = null;
        Object context = gp;
        if (taskType != null){
            String taskTypeCode = taskType.getCode();
            taskPlan = getEntity(taskTypeCode, context, () -> {
                TaskPlan p = modelFactory.newTaskPlan(taskType);
                MaintenanceGroup group = getMaintenanceGroupInCtx(r, area);
                p.setResource(group);
                return p;
            });
        }else {
            LOG.warn("TaskType is null!!!");
        }
        return  taskPlan;
    }

    public GeneralTaskPlan getGeneralTaskPlan(Result r){
        String generalTaskType = getGeneralTaskTypeLabel(r);
        GeneralTaskPlan generalTaskPlan = getEntity(
                generalTaskType,
                "general-task-type",
                () -> createGeneralTasPlan(r, generalTaskType)
        );
        return generalTaskPlan;
    }

    public GeneralTaskPlan getGeneralTaskPlanInCtx(Result r, Object context){
        // general task plan <=> different session.type.type per session.type.area
        String generalTaskType = getGeneralTaskTypeLabel(r);
        GeneralTaskPlan generalTaskPlan = getEntity(
                generalTaskType,
                context,
                () -> createGeneralTasPlan(r, generalTaskType));
        return generalTaskPlan;
    }

    private GeneralTaskPlan createGeneralTasPlan(Result r, String generalTaskType){
        GeneralTaskPlan p = modelFactory.newGeneralTaskPlan(generalTaskType);
        AircraftArea area = getAircraftArea(r);
        p.setResource(area);
        return p;
    }

    public PhasePlan getPhasePlan(Result r, final Aircraft aircraft){
        String phaseLabel = getPhaseLabel(r);
        PhasePlan phasePlan = getEntity(phaseLabel, "phase", () -> {
            PhasePlan p = modelFactory.newPhasePlan(phaseLabel);
            Aircraft ac = getAircraft(r);
            p.setResource(defaults.isDefault(ac) ? aircraft : ac);
            return p;
        });
        return phasePlan;
    }



    // this is a simplification
    public String getMechanicLabel(Result r){
        return Optional.ofNullable(r.getMechanic()).map(Mechanic::getTitle)
                .map(this::mapNull).orElse(modelFactory.generateId() + "");
    }

    public String getAircraftModelLabel(Result r){
        return getTaskTypeDefinition(r).map(TaskType::getAcmodel)
                .map(this::mapNull).orElse(defaults.aircraftModelLabel);
    }

    public String getAreaLabel(Result r){
        return getTaskTypeDefinition(r).map(TaskType::getArea)
                .map(this::mapNull).orElse(defaults.areaLabel);
    }

    public String getMaintenanceGroupLabel(Result r){
        return getTaskTypeDefinition(r).map(TaskType::getScope)
                .map(this::mapNull).orElse(defaults.maintenanceGroupLabel);
    }

    public String getGeneralTaskTypeLabel(Result r){
        return getTaskTypeDefinition(r).map(TaskType::getTaskType)
                .map(this::mapNull).orElse(defaults.generalTaskTypeLabel);
    }

    public String getPhaseLabel(Result r){
        return getTaskTypeDefinition(r).map(TaskType::getPhase)
                .map(this::mapNull).orElse(defaults.phaseLabel);
    }

    public Optional<TaskType> getTaskType(Result r){
        return Optional.ofNullable(r.taskType);
    }

    public Optional<TaskType> getTaskTypeDefinition(Result r){
        return getTaskType(r).map(tt -> tt.getDefinition());
    }


    /**
     *
     * @param id - the id of the object to be fetched
     * @param context - equals and hash should be implemented to follow that two different instances with the same
     *               attribute are interpreted as equal and have the same hash code
     * @param generator
     * @param <T>
     * @return
     */
    public <T extends AbstractEntity> T getEntity(String id, Object context, Supplier<T> generator){
        Map<String, AbstractEntity> entityMap = entityMaps.get(context);
        if(entityMap == null) {
            entityMap = new HashMap<>();
            entityMaps.put(context, entityMap);
        }
        AbstractEntity r = entityMap.get(id);
        if(r == null) {
            r = generator.get();
            entityMap.put(id, r);
        }
        return (T)r;
    }

    public static class PlanningResult{
        protected RevisionPlan revisionPlan;
        protected Map<Result, SessionPlan> sessionPlans = new HashMap<>();

        public PlanningResult(RevisionPlan revisionPlan) {
            this.revisionPlan = revisionPlan;
        }

        public RevisionPlan getRevisionPlan() {
            return revisionPlan;
        }

        public void setRevisionPlan(RevisionPlan revisionPlan) {
            this.revisionPlan = revisionPlan;
        }

        public Map<Result, SessionPlan> getSessionPlans() {
            return sessionPlans;
        }

        public void setSessionPlans(Map<Result, SessionPlan> sessionPlans) {
            this.sessionPlans = sessionPlans;
        }
    }

    private String mapNull(String s){
        return isEmpty(s) ? null : s;
    }

    private boolean isEmpty(String s){
        return s == null || s.trim().isEmpty();
    }

    public static String DEFAULT = "unknown";
    public static class Defaults{
        public String aircraftModelLabel = DEFAULT;
        public String phaseLabel = DEFAULT;
        public String areaLabel = DEFAULT;
        public String maintenanceGroupLabel = DEFAULT;
        public String generalTaskTypeLabel = DEFAULT;
        public String taskTypeCode = DEFAULT;
        public String mechanicLabel = DEFAULT;
        public boolean isDefault(Aircraft ac){
            return aircraftModelLabel.equals(ac.getTitle());
        }

        public boolean isDefault(AircraftArea aa){
            return areaLabel.equals(aa.getTitle());
        }

        public boolean isDefault(MaintenanceGroup mg){
            return maintenanceGroupLabel.equals(mg.getTitle());
        }
    }

//    public static void main(String[] args) {
////        List<Pair<Integer, Integer>> l = Stream.of(
////                Pair.of(2,4),
////                Pair.of(2,3),
////                Pair.of(1,2)
////                ).collect(Collectors.toList());
////
////        l.sort(Comparator
////                .comparing((Pair<Integer, Integer> p) -> p.getLeft())
////                .thenComparing((Pair<Integer, Integer> p) -> p.getRight())
////        );
////        System.out.println(l);
//
//        for(int i = 0; i < 10; i++) {
//            for (int j = i + 1; j < 10; j++) {
//                System.out.println(String.format("%d, %d", i, j));
//            }
//            System.out.println(String.format("%d, %d", i, j));
//        }
//    }
}
