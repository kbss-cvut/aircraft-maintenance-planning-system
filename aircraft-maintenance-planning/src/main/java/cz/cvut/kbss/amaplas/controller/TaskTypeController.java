package cz.cvut.kbss.amaplas.controller;


import cz.cvut.kbss.amaplas.services.TaskTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/task-types")
public class TaskTypeController {

    private final TaskTypeService taskTypeService;

    public TaskTypeController(TaskTypeService taskTypeService) {
        this.taskTypeService = taskTypeService;
    }

    @GetMapping(path="update")
    public void updateTaskTypeMapping(){
        taskTypeService.updateTaskTypeMapping();
    }
}
