package cz.cvut.kbss.amaplas.controller;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.RevisionPlan;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskPlan;
import cz.cvut.kbss.amaplas.services.AircraftRevisionPlannerService;
import cz.cvut.kbss.jsonld.JsonLd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
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
}
