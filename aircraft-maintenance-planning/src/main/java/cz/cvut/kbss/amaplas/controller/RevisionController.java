package cz.cvut.kbss.amaplas.controller;

import cz.cvut.kbss.amaplas.config.props.ConfigProperties;
import cz.cvut.kbss.amaplas.model.Workpackage;
import cz.cvut.kbss.amaplas.services.RevisionHistory;
import cz.cvut.kbss.jsonld.JsonLd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RevisionController {
    private static final Logger LOG = LoggerFactory.getLogger(RevisionController.class);

    private final ConfigProperties props;
    private final RevisionHistory revisionHistoryService;

    @Autowired
    public RevisionController(ConfigProperties props, RevisionHistory revisionHistoryService) {
        this.props = props;
        this.revisionHistoryService = revisionHistoryService;
    }

    @GetMapping(path = "revisions/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getAllRevisions(){
        LOG.info("Retrieving all revisions");
        return revisionHistoryService.getAllRevisions();
    }

    @GetMapping(path = "revisions", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public Workpackage getWorkpackageById(@RequestParam String revisionId){
        Workpackage workpackage = revisionHistoryService.getWorkpackage(revisionId);
        return workpackage;
    }
}
