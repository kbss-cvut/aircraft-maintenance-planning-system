package cz.cvut.kbss.amaplas.controller;

import cz.cvut.kbss.amaplas.config.ConfigProperties;
import cz.cvut.kbss.amaplas.services.RevisionHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
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
}
