package cz.cvut.kbss.amaplas.controller;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.Result;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskPlan;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskType;
import cz.cvut.kbss.amaplas.services.AircraftRevisionPlannerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController()
public class PlanController {
    private static final Logger LOG = LoggerFactory.getLogger(PlanController.class);

    private final AircraftRevisionPlannerService plannerService;

    public PlanController(AircraftRevisionPlannerService plannerService) {
        this.plannerService = plannerService;
    }

    @PostMapping(path = "/api/plan-tasks",consumes = MediaType.APPLICATION_JSON_VALUE)
    public void planTasks(@RequestBody List<String> taskCardCodes){
        LOG.info("planning tasks {}", taskCardCodes);
        plannerService.planTaskTypeCodes(taskCardCodes);
    }

    @PostMapping(path = "/api/plan-tasks",consumes = MediaType.TEXT_PLAIN_VALUE)
    public void planTasks(@RequestBody String taskCardCodes){
        planTasks(Arrays.asList(taskCardCodes.split(",")));
    }


    @GetMapping(path = "/api/plan-revision", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TaskPlan> planRevision(@RequestParam String revisionId) {
        List<TaskPlan> tts = plannerService.planRevision(revisionId);
        return tts;
//        // transform to dto
//        if(tts == null)
//            return Collections.EMPTY_LIST;
//        List<String> ret = new ArrayList<>();
//        for(TaskType tt: tts){
//            ret.add(tt.toString());
//        }
//        return ret;
    }

    @GetMapping(path = "/api/tmp", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Result> tmpController() {
        Function<Integer, Result> factory = i -> {
                Result r = new Result();
                r.wp = "wp" + 1;
                r.taskType = new TaskType("" + i, "task-type-name-" + i);

                r.start = new Date();
                r.dur = System.currentTimeMillis();
                return r;
        };
//        return IntStream.range(0,10).mapToObj(i -> new Tmp("name-"+i, ""+i)).collect(Collectors.toList());
        return IntStream.range(0, 10).mapToObj(i -> factory.apply(i)).collect(Collectors.toList());
    }

    public static class Tmp{
        protected String name;
        protected String code;


        public Tmp(String name, String code) {
            this.name = name;
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
