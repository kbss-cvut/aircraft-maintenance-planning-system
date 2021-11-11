package cz.cvut.kbss.amaplas.controller;

import cz.cvut.kbss.amaplas.services.RevisionHistory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RevisionController {
    private final RevisionHistory revisionHistoryService;

    public RevisionController(RevisionHistory revisionHistoryService) {
        this.revisionHistoryService = revisionHistoryService;
    }

    @GetMapping(path = "/api/revisions/", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getAllRevisions(){
        return revisionHistoryService.getAllRevisions();
    }
}
