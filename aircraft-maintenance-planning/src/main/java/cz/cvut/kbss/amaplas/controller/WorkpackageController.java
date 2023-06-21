package cz.cvut.kbss.amaplas.controller;


import cz.cvut.kbss.amaplas.model.Workpackage;
import cz.cvut.kbss.amaplas.persistence.dao.WorkpackageDAO;
import cz.cvut.kbss.amaplas.services.IdentifierService;
import cz.cvut.kbss.amaplas.services.WorkpackageService;
import cz.cvut.kbss.jsonld.JsonLd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/workpackage")
public class WorkpackageController extends BaseControllerImplementation<Workpackage, WorkpackageDAO> {
    private static final Logger LOG = LoggerFactory.getLogger(WorkpackageController.class);

    private final WorkpackageService workpackageService;
    public WorkpackageController(IdentifierService identifierService, WorkpackageDAO entityDao, WorkpackageService workpackageService) {
        super(identifierService, entityDao);
        this.workpackageService = workpackageService;
    }

    @GetMapping(path = "/closed", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public List<Workpackage> listClosed(){
        return getEntityDao().findAllClosed();
    }

    @GetMapping(path = "/opened", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public List<Workpackage> listOpened(){
        return getEntityDao().findAllOpened();
    }

    @GetMapping(path = "header", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> listAllHeaders(){
        LOG.info("Retrieving all revisions");
        return workpackageService.getHeaders().stream()
                .map(p -> String.format("%s, %d", p.getLeft().getId(), p.getRight()))
                .collect(Collectors.toList());
    }

    @GetMapping(params = {"id"}, produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    @Override
    public Workpackage getEntityById(@RequestParam String revisionId){
        Workpackage workpackage = workpackageService.findById(revisionId);
        return workpackage;
    }
}
