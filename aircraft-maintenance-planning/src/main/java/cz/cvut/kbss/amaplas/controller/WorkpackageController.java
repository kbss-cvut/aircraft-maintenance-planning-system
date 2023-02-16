package cz.cvut.kbss.amaplas.controller;


import cz.cvut.kbss.amaplas.model.Workpackage;
import cz.cvut.kbss.amaplas.persistence.dao.WorkpackageDAO;
import cz.cvut.kbss.amaplas.services.IdentifierService;
import cz.cvut.kbss.jsonld.JsonLd;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/workpackages")
public class WorkpackageController extends BaseControllerImplementation<Workpackage, WorkpackageDAO> {
    public WorkpackageController(IdentifierService identifierService, WorkpackageDAO entityDao) {
        super(identifierService, entityDao);
    }

    @GetMapping(path = "/closed", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public List<Workpackage> listClosed(){
        return getEntityDao().findAllClosed();
    }

    @GetMapping(path = "/opened", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public List<Workpackage> listOpened(){
        return getEntityDao().findAllOpened();
    }
}
