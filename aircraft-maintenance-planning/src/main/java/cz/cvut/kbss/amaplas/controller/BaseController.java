package cz.cvut.kbss.amaplas.controller;

import cz.cvut.kbss.amaplas.model.AbstractEntity;
import cz.cvut.kbss.amaplas.model.Aircraft;
import cz.cvut.kbss.amaplas.persistence.dao.BaseDao;
import cz.cvut.kbss.amaplas.services.IdentifierService;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jsonld.JsonLd;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public class BaseController<E extends AbstractEntity> {

    private final IdentifierService identifierService;


    protected String defaultNamesapce;
    public BaseController(IdentifierService identifierService, String defaultNamesapce) {
        this.identifierService = identifierService;
        this.defaultNamesapce = defaultNamesapce;
    }

    public IdentifierService getIdentifierService() {
        return identifierService;
    }


    protected URI expandFragment(String planFragment, Optional<String> namespace){
        String ns = namespace.orElse(defaultNamesapce);
        return identifierService.composeIdentifier(ns, planFragment);
    }



}
