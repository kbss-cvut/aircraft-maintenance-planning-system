package cz.cvut.kbss.amaplas.controller;

import cz.cvut.kbss.amaplas.controller.dto.EntityReferenceDTO;
import cz.cvut.kbss.amaplas.controller.dto.RelationDTO;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.AbstractPlan;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.RevisionPlan;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskPlan;
import cz.cvut.kbss.amaplas.services.AircraftRevisionPlannerService;
import cz.cvut.kbss.amplas.util.Vocabulary;
import cz.cvut.kbss.jsonld.JsonLd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/plans")
public class PlanController {
    private static final Logger LOG = LoggerFactory.getLogger(PlanController.class);

    private final AircraftRevisionPlannerService plannerService;

    public PlanController(AircraftRevisionPlannerService plannerService) {
        this.plannerService = plannerService;
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

    @GetMapping(path="/", consumes = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public AbstractPlan getPlan(@RequestBody EntityReferenceDTO planReference){
        return plannerService.getPlan(planReference);
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

    @PostMapping(path = "/part", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public void addPlanPart(@RequestBody RelationDTO relationDTO){
        plannerService.addPlanPart(relationDTO);
    }

}
