package cz.cvut.kbss.amaplas.controller;


import cz.cvut.kbss.amaplas.model.Result;
import cz.cvut.kbss.amaplas.services.RevisionHistory;
import cz.cvut.kbss.amaplas.services.TaskTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/task-types")
public class TaskTypeController {
    private static final Logger LOG = LoggerFactory.getLogger(TaskTypeController.class);

    private final TaskTypeService taskTypeService;
    private final RevisionHistory revisionHistory;

    public TaskTypeController(TaskTypeService taskTypeService, RevisionHistory revisionHistory) {
        this.taskTypeService = taskTypeService;
        this.revisionHistory = revisionHistory;
    }

    @GetMapping(path="update-definition-mappings")
    public void updateTaskTypeMapping(){
        LOG.info("updating taskType to taskTypeDefinition mapping.");
        taskTypeService.updateTaskTypeMapping();
    }
}
