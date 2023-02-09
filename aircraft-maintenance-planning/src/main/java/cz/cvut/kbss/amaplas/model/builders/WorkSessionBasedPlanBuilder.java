package cz.cvut.kbss.amaplas.model.builders;

import cz.cvut.kbss.amaplas.model.*;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorkSessionBasedPlanBuilder extends AbstractPlanBuilder<List<Result>> {

    private static final Logger LOG = LoggerFactory.getLogger(WorkSessionBasedPlanBuilder.class);


    public WorkSessionBasedPlanBuilder() {
    }

    public WorkSessionBasedPlanBuilder(AbstractPlanBuilder planBuilder) {
        super(planBuilder);
    }

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
                .filter(p -> p.getTaskType() != null && p.getTaskType().getDefinition() != null
                        && p.getStartTime() != null && p.getEndTime() != null)
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
                        .thenComparing((RestrictionPlan p) -> p.getRestrictions().iterator().next().getTitle())
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
        for(; i < originalPlans.size(); i = j) {
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
        return taskType.getRestrictions().stream()
                .filter(p -> !p.getValue().trim().equals("/"))
                .map(p -> getRestrictionPlan(taskPlan, taskType, p.getValue(), p.getKey()))
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
    public RestrictionPlan getRestrictionPlan(TaskPlan taskPlan, TaskType taskType, String restrictionProposition, String restrictionSubject){
        Restriction restriction = getRestriction(restrictionProposition, restrictionSubject);
        RestrictionPlan restrictionPlan = new RestrictionPlan();
        restrictionPlan.setId(modelFactory.generateId());
        restrictionPlan.setEntityURI(modelFactory.createURI(restrictionPlan.getId()));
        restrictionPlan.setTitle(getRestrictionSubjectLabel(restrictionSubject) + " " + restrictionProposition);
        restrictionPlan.setRestrictions(new HashSet<>());
        restrictionPlan.getRestrictions().add(restriction);
        restrictionPlan.setRequiringPlans(new HashSet<>());
        restrictionPlan.getRequiringPlans().add(taskPlan);
        applyTemporalValues(taskPlan, restrictionPlan);

        // add the restrictionSubject as a resource in the restriction Plan
        Resource restrictedResource = getEntity(restrictionSubject,"restriction-subject", () -> {
            Resource r = new Resource();
            r.setId(modelFactory.generateId());
            r.setEntityURI(URI.create(restrictionSubject));
            r.setTitle(getRestrictionSubjectLabel(restrictionSubject));
            return r;
        });
        restrictionPlan.setResource(restrictedResource);
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
    public Restriction getRestriction(String restrictionProposition, String restrictionSubject){
        String restrictionId = DigestUtils.md5Hex(restrictionSubject + restrictionProposition);
        return getEntity(restrictionId, "restriction", () -> {
            Restriction restriction = new Restriction();
            restriction.setId(restrictionId);
            restriction.setEntityURI(modelFactory.createURI(restrictionId));
            restriction.setSubject(URI.create(restrictionSubject));
            restriction.setTitle(restrictionProposition);
            return restriction;
        });
    }

    protected String getRestrictionSubjectLabel(String restrictionSubject){
        switch (restrictionSubject.toString()){
            case Vocabulary.s_c_el_dot__power: return "el. power";
            case Vocabulary.s_c_hyd_dot__power: return "hyd. power";
            case Vocabulary.s_c_jack: return "jack";
            default : return null;
        }
    }

    public RevisionPlan createRevision(PlanBuilderInput<List<Result>> input){
        List<Result> results = input.getInput();
        Aircraft aircraft = input.workpackage.getAircraft();
        if(aircraft == null)
            aircraft = results.stream().map(r -> getAircraft(r)).filter(a -> !defaults.isDefault(a)).findFirst().orElse(null);

        setUpAircraft(aircraft);
        // create bottom part of hierarchical plan -  create to task plans
        RevisionPlan revisionPlan = new RevisionPlan();
        String revisionCode = results.stream()
                .map(r -> r.wp)
                .filter(s -> s != null && !s.isEmpty())
                .findFirst().orElse(null);

        Long revisionId = revisionCode != null ? (long)revisionCode.hashCode() : null;
        revisionPlan.setTitle(revisionCode);
        revisionPlan.setId(revisionId);
        revisionPlan.setEntityURI(modelFactory.createURI(revisionId + ""));

        revisionPlan.setResource(aircraft);

        for(Result r : results) {
            PhasePlan phasePlan = getPhasePlan(r, aircraft);
            revisionPlan.getPlanParts().add(phasePlan);

            AircraftArea area = getAircraftArea(r);
            Pair generalTaskPlanContext = Pair.of(phasePlan, area);
            GeneralTaskPlan generalTaskPlan = getGeneralTaskPlanInCtx(r, generalTaskPlanContext);
            phasePlan.getPlanParts().add(generalTaskPlan);

            TaskPlan taskPlan = getTaskPlan(r, generalTaskPlan);
            generalTaskPlan.getPlanParts().add(taskPlan);

            SessionPlan sessionPlan = getSessionPlan(r, taskPlan.getResource());
            taskPlan.getPlanParts().add(sessionPlan);

        }
        return revisionPlan;
    }

    public void setUpAircraft(Aircraft aircraft){
        if(aircraft == null)
            return;
        String model = aircraft.getModel();
        String registration = aircraft.getRegistration();
        String age = aircraft.getAge();
        String title =  Stream.of(model, registration, age).filter(s -> s != null).collect(Collectors.joining(" - "));
        aircraft.setTitle(title);
    }

    public SessionPlan getSessionPlan(Result r, Resource groupInArea) {
        SessionPlan sessionPlan = modelFactory.newSessionPlan(r.start, r.end);
        Mechanic mechanic = getMechanic(r, groupInArea);
        sessionPlan.setResource(mechanic);
        return sessionPlan;
    }

    public Mechanic getMechanic(Result r, Resource groupInArea) {
        Mechanic m = r.getMechanic();
        return m == null ?
                null :
                getEntity(
                        m.getId(),
                        groupInArea,
                        () -> {
                            Mechanic mech = new Mechanic();
                            mech.setId(m.getId());
                            mech.setEntityURI(modelFactory.createURI("mechanic", Objects.toString(m.getId()), groupInArea));
                            mech.setTitle(Objects.toString(m.getTitle() == null ? m.getId() : m.getTitle()));
                            return mech;
                        }
                );
    }

    public MaintenanceGroup getMaintenanceGroupInCtx(Result r, String area){
        String maintenanceGroupLabel = getMaintenanceGroupLabel(r);
        return getMaintenanceGroupInCtx(maintenanceGroupLabel, area);
    }

    public AircraftArea getAircraftArea(Result r){
        String area = getAreaLabel(r);
        return getAircraftArea(area);
    }

    public Aircraft getAircraft(Result r){
        String aircraftModel = getAircraftModelLabel(r);
        return getAircraft(aircraftModel);
    }

    public TaskPlan getTaskPlan(final Result r, GeneralTaskPlan gp){
        TaskType taskType = getTaskType(r).orElse(null);
        return getTaskPlan(taskType, gp);
    }

    public GeneralTaskPlan getGeneralTaskPlanInCtx(Result r, Object context){
        // general task plan <=> different session.type.type per session.type.area
        return getGeneralTaskPlanInCtx(getTaskTypeDefinition(r), context);
    }

    public PhasePlan getPhasePlan(Result r, final Aircraft aircraft){
        String phaseLabel = getPhaseLabel(r);
        PhasePlan phasePlan = getEntity(phaseLabel, "phase", () -> {
            PhasePlan p = modelFactory.newPhasePlan(phaseLabel);
            Aircraft ac = aircraft == null ? getAircraft(r) : aircraft;
            p.setResource(defaults.isDefault(ac) ? aircraft : ac);
            return p;
        });
        return phasePlan;
    }

    public String getAircraftModelLabel(Result r){
        return getAircraftModelLabel(getTaskTypeDefinition(r));
    }

    public String getAreaLabel(Result r){
        return getAircraftAreaLabel(getTaskTypeDefinition(r));
    }

    public String getMaintenanceGroupLabel(Result r){
        return (r.scope != null)
                ? r.scope
                : getMaintenanceGroupLabel(getTaskTypeDefinition(r));
    }

    public String getGeneralTaskTypeLabel(Result r){
        return getGeneralTaskTypeLabel(getTaskTypeDefinition(r));
    }

    public String getPhaseLabel(Result r){
        return getPhaseLabel(getTaskTypeDefinition(r));
    }

    public Optional<TaskType> getTaskType(Result r){
        return Optional.ofNullable(r.taskType);
    }

    public Optional<TaskType> getTaskTypeDefinition(Result r){
        return getTaskType(r).map(tt -> tt.getDefinition());
    }
}

