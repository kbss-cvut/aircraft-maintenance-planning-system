package cz.cvut.kbss.amaplas.controller;

import cz.cvut.kbss.amaplas.model.AbstractEntity;
import cz.cvut.kbss.amaplas.persistence.dao.BaseDao;
import cz.cvut.kbss.amaplas.services.IdentifierService;
import cz.cvut.kbss.jsonld.JsonLd;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;
import java.util.List;
import java.util.Optional;

public class BaseControllerImplementation<E extends AbstractEntity,D extends BaseDao> extends BaseController<E>{

    private final D entityDao;

    public BaseControllerImplementation(IdentifierService identifierService, D entityDao) {
        super(identifierService, entityDao.getTypeUri().toString());
        this.entityDao = entityDao;
    }

    public D getEntityDao() {
        return entityDao;
    }

    @GetMapping(path = "", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public List<E> listEntities(){
        return entityDao.findAll();
    }

    @GetMapping(path= "/{entityFragment}", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public E getEntity(@PathVariable String entityFragment,
                       @RequestParam(required = false) Optional<String> ns){
        URI entityUri = expandFragment(entityFragment, ns);
        return (E)getEntityDao().find(entityUri).orElse(null);
    }

    @GetMapping(params = {"id"}, produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
    public E getEntityById(@RequestParam String id){
        return (E)getEntityDao().findById(id).orElse(null);
    }
}
