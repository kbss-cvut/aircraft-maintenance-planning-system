package cz.cvut.kbss.amaplas.controller;


import cz.cvut.kbss.amaplas.model.Workpackage;
import cz.cvut.kbss.amaplas.persistence.dao.WorkpackageDAO;
import cz.cvut.kbss.amaplas.services.IdentifierService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/workpackages")
public class WorkpackageController extends BaseControllerImplementation<Workpackage, WorkpackageDAO> {
    public WorkpackageController(IdentifierService identifierService, WorkpackageDAO entityDao) {
        super(identifierService, entityDao);
    }
}
