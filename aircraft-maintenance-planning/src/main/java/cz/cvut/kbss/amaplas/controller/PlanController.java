package cz.cvut.kbss.amaplas.controller;

import cz.cvut.kbss.amaplas.model.AbstractPlan;
import cz.cvut.kbss.amaplas.model.RevisionPlan;
import cz.cvut.kbss.amaplas.services.AircraftRevisionPlannerService;
import cz.cvut.kbss.amaplas.services.IdentifierService;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jsonld.JsonLd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/plans")
public class PlanController extends BaseController{
    private static final Logger LOG = LoggerFactory.getLogger(PlanController.class);

    private final AircraftRevisionPlannerService plannerService;

    public PlanController(AircraftRevisionPlannerService plannerService, IdentifierService identifierService) {
        super(identifierService, Vocabulary.s_c_event_plan);
        this.plannerService = plannerService;
    }

    @GetMapping(path = "revision-plans-induced-by-revision-execution", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE} )
    public RevisionPlan planRevision2(@RequestParam String revisionId){
        return plannerService.createRevisionPlanScheduleDeducedFromRevisionExecution(revisionId);
    }
    @GetMapping(path = "plan-from-similar-revisions", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE} )
    public RevisionPlan planFromSimilarRevisions(@RequestParam String revisionId){
        return plannerService.createRevisionPlanScheduleDeducedFromSimilarRevisions(revisionId);
    }

    /**
     * Create a new plan based on plan type fragment, no other information is required. Accepted plan type fragments are :
     * workpackage-plan, phase-plan, general-task-plan, task-plan, work-session-plan.
     *
     * @param planTypeFragment
     * @return
     */
    @PostMapping(path="/{plantTypeFragment}", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public AbstractPlan createPlan(@PathVariable String planTypeFragment, @RequestParam(required = false) Optional<String> ns){
        URI planTypeURI = expandFragment(planTypeFragment, ns);
        return plannerService.createPlan(planTypeURI);
    }

    /**
     * Create a new plan from the plan object passed in the request body.
     * @param plan
     * @return
     */
    @PostMapping(path="/", consumes = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE}, produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public AbstractPlan createPlan(@RequestBody AbstractPlan plan){
        return plannerService.createPlan(plan);
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //// Fragment based api ////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @GetMapping(path= "/{planFragment}", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public AbstractPlan getPlan(@PathVariable  String planFragment,
                                        @RequestParam(required = false) Optional<String> ns){
        URI planUri = expandFragment(planFragment, ns);
        return plannerService.getPlan(planUri);
    }

    @GetMapping(path = "/{planFragment}/planParts", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public Collection<? extends AbstractPlan> getPlanParts(@PathVariable String planFragment,
                                                           @RequestParam(required = false) Optional<String> ns){
        URI planUri = expandFragment(planFragment, ns);
        return plannerService.getPlanParts(planUri);
    }

    @PostMapping(path = "/{planFragment}/planParts/{planPartFragment}")
    public void addPlanPart(@PathVariable String planFragment, @PathVariable String planPartFragment,
                            @RequestParam(required = false) Optional<String> ns1,
                            @RequestParam(required = false) Optional<String> ns2,
                            @RequestParam(required = false) Optional<String> ns){
        URI planUri = expandFragment(planFragment, ns1.isPresent() ? ns1 : ns);
        URI planPartUri = expandFragment(planPartFragment, ns2.isPresent() ? ns2 : ns);
        plannerService.addPlanPart(planUri, planPartUri);
    }


    @DeleteMapping(path = "/{planFragment}/planParts/{planPartFragment}")
    public void deletePlanPart(@PathVariable String planFragment, @PathVariable String planPartFragment,
                               @RequestParam(required = false) Optional<String> ns1,
                               @RequestParam(required = false) Optional<String> ns2,
                               @RequestParam(required = false) Optional<String> ns){
        URI planUri = expandFragment(planFragment, ns1.isPresent() ? ns1 : ns);
        URI planPartUri = expandFragment(planPartFragment, ns2.isPresent() ? ns2 : ns);
        plannerService.deletePlanPart(planUri, planPartUri);
    }

    @DeleteMapping(path = "/{planFragment}")
    public void deletePlan(@PathVariable String planFragment,
                           @RequestParam(required = false) Optional<String> ns){
        URI planUri = expandFragment(planFragment, ns);
        plannerService.deletePlan(planUri);
    }
}
