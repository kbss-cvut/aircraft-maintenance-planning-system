package cz.cvut.kbss.amaplas.controller;

import cz.cvut.kbss.amaplas.model.AbstractPlan;
import cz.cvut.kbss.amaplas.model.RevisionPlan;
import cz.cvut.kbss.amaplas.model.values.DateUtils;
import cz.cvut.kbss.amaplas.services.AircraftRevisionPlannerService;
import cz.cvut.kbss.amaplas.services.IdentifierService;
import cz.cvut.kbss.amaplas.services.WorkpackageService;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jsonld.JsonLd;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/plans")
public class PlanController extends BaseController{
    private static final Logger LOG = LoggerFactory.getLogger(PlanController.class);

    private final AircraftRevisionPlannerService plannerService;
    private final WorkpackageService workpackageService;


    public PlanController(AircraftRevisionPlannerService plannerService, IdentifierService identifierService, WorkpackageService workpackageService) {
        super(identifierService, Vocabulary.s_c_event_plan);
        this.plannerService = plannerService;
        this.workpackageService = workpackageService;
    }

    @GetMapping(path = "revision-plans-induced-by-revision-execution", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE} )
    public RevisionPlan planRevision2(@RequestParam String revisionId){
        return plannerService.createRevisionPlanScheduleDeducedFromRevisionExecution(revisionId);
    }
    @GetMapping(path = "plan-from-similar-revisions", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE} )
    public ResponseEntity<RevisionPlan> planFromSimilarRevisions(@RequestParam String revisionId){
        if(workpackageService.isRefreshingCache())
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(null);
        return ResponseEntity.status(HttpStatus.OK).body(
                plannerService.createRevisionPlanScheduleDeducedFromSimilarRevisions(revisionId, true)
        );
    }

    @GetMapping(path = "export-plan")
    public void exportPlan(@RequestParam String revisionId, HttpServletRequest request, HttpServletResponse response){
        byte[] output = plannerService.exportPlan(revisionId);
        try {
            response.setContentType("application/zip");
            response.setContentLength(output.length);

            String fileName = String.format("%s--planned-on-%s.zip",
                    revisionId.replaceAll("[^\\w\\d-_ ]", "--"),
                    DateUtils.formatDateTime(new Date()).replace(":","-")
            );

            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                    .filename(fileName, StandardCharsets.UTF_8)
                    .build()
                    .toString());
            IOUtils.write(output, response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
