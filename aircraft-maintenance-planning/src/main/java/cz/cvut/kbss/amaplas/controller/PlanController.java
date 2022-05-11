package cz.cvut.kbss.amaplas.controller;

import cz.cvut.kbss.amaplas.controller.dto.EntityReferenceDTO;
import cz.cvut.kbss.amaplas.controller.dto.RelationDTO;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.AbstractPlan;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.RevisionPlan;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskPlan;
import cz.cvut.kbss.amaplas.services.AircraftRevisionPlannerService;
import cz.cvut.kbss.amaplas.services.IdentifierService;
import cz.cvut.kbss.amplas.util.Vocabulary;
import cz.cvut.kbss.jsonld.JsonLd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/plans")
public class PlanController {
    private static final Logger LOG = LoggerFactory.getLogger(PlanController.class);

    private final AircraftRevisionPlannerService plannerService;
    private final IdentifierService identifierService;

    public PlanController(AircraftRevisionPlannerService plannerService, IdentifierService identifierService) {
        this.plannerService = plannerService;
        this.identifierService = identifierService;
    }

    @PostMapping(path = "plan-tasks",consumes = MediaType.APPLICATION_JSON_VALUE)
    public void planTasks(@RequestBody List<String> taskCardCodes){
        LOG.info("planning tasks {}", taskCardCodes);
        plannerService.planTaskTypeCodes(taskCardCodes);
    }

    @PostMapping(path = "plan-tasks",consumes = MediaType.TEXT_PLAIN_VALUE)
    public void planTasks(@RequestBody String taskCardCodes){
        planTasks(Arrays.asList(taskCardCodes.split(",")));
    }


    @GetMapping(path = "revision-plans", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public List<TaskPlan> planRevision(@RequestParam String revisionId) {
        List<TaskPlan> tts = plannerService.planRevision(revisionId);
        return tts;
    }

    @GetMapping(path = "revision-plans-induced-by-revision-execution", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE} )
    public RevisionPlan planRevision2(@RequestParam String revisionId){
        return plannerService.createRevisionPlanScheduleDeducedFromRevisionExecution(revisionId);
    }

    /**
     * Create a new plan based on plan type fragment, no other information is required. Accepted plan type fragments are :
     * workpackage-plan, phase-plan, general-task-plan, task-plan, work-session-plan.
     *
     * @param planTypeFragment
     * @return
     */
    @PostMapping(path="/{plantTypeFragment}", consumes = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public AbstractPlan createPlan(@PathVariable String planTypeFragment){
        URI planTypeURI = URI.create(String.format("%s/%s", Vocabulary.ONTOLOGY_IRI_aircraft_maintenance_planning, planTypeFragment));
        return plannerService.createPlan(planTypeURI);
    }

    /**
     * Create a new plan from the plan object passed in the request body.
     * @param plan
     * @return
     */
    @PostMapping(path="/", consumes = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public AbstractPlan createPlan(@RequestBody AbstractPlan plan){
        return plannerService.createPlan(plan);
    }

    @GetMapping(path="", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public AbstractPlan getPlanUri(@RequestParam URI planUri){
        return plannerService.getPlan(planUri);
    }

    /**
     * Updates basic properties (i.e. properties of primitive type and String or URI/IRI) without the entityUri field
     * for the specified plan.
     * @param plan The updated plan
     */
    @PutMapping(path = "/", consumes = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@RequestBody AbstractPlan plan) {
        plannerService.updatePlanSimpleProperties(plan);
        LOG.debug("Basic properties of plan {} updated.", plan);
    }

    @PostMapping(path = "/planParts", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public void addPlanPart(@RequestBody RelationDTO relationDTO){
        plannerService.addPlanPart(relationDTO);
    }


    @GetMapping(path = "/planParts", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public Collection<? extends AbstractPlan> getPlanParts(@RequestParam URI planUri){
        return plannerService.getPlanParts(planUri);
    }

    @DeleteMapping(path = "/planParts", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public void deletePlanPart(@RequestBody RelationDTO relationDTO){
        plannerService.deletePlanPart(relationDTO);
    }

    @DeleteMapping(path = "/", consumes = {MediaType.APPLICATION_JSON_VALUE} )
    public void deletePlan(@RequestBody EntityReferenceDTO entityReferenceDTO){
        plannerService.deletePlan(entityReferenceDTO);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //// Fragment based api ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GetMapping(path="/{planFragment}", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public AbstractPlan getPlanFragment(@PathVariable  String planFragment){
        URI planUri = expandUri(planFragment);
        return plannerService.getPlan(planUri);
    }

    @GetMapping(path = "/{planFragment}/planParts", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public Collection<? extends AbstractPlan> getPlanParts(@PathVariable String planFragment){
        URI planUri = expandUri(planFragment);
        return plannerService.getPlanParts(planUri);
    }

    @PostMapping(path = "/{planFragment}/planParts/{planPartFragment}")
    public void addPlanPart(@PathVariable String planFragment, @PathVariable String planPartFragment){
        URI planUri = expandUri(planFragment);
        URI planPartUri = expandUri(planPartFragment);
        plannerService.addPlanPart(planUri, planPartUri);
    }


    @DeleteMapping(path = "/{planFragment}/planParts/{planPartFragment}")
    public void deletePlan(@PathVariable String planFragment, @PathVariable String planPartFragment){
        URI planUri = expandUri(planFragment);
        URI planPartUri = expandUri(planPartFragment);
        plannerService.deletePlanPart(planUri, planPartUri);
    }

    @DeleteMapping(path = "/{planFragment}")
    public void deletePlan(@PathVariable String planFragment){
        URI planUri = expandUri(planFragment);
        plannerService.deletePlan(planUri);
    }

    protected URI expandUri(String planFragment){
        return identifierService.composeIdentifier(Vocabulary.s_c_event_plan, planFragment);
    }

}
