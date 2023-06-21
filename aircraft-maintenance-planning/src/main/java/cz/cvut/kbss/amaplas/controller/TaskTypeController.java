package cz.cvut.kbss.amaplas.controller;


import cz.cvut.kbss.amaplas.services.TaskTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task-types")
public class TaskTypeController {
    private static final Logger LOG = LoggerFactory.getLogger(TaskTypeController.class);

    private final TaskTypeService taskTypeService;

    public TaskTypeController(TaskTypeService taskTypeService) {
        this.taskTypeService = taskTypeService;
    }

    @GetMapping(path="update-definition-mappings")
    public void updateTaskTypeMapping(){
        LOG.info("updating taskType to taskTypeDefinition mapping.");
        taskTypeService.updateTaskTypeMapping();
    }
}
