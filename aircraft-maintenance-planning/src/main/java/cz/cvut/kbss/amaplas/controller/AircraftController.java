package cz.cvut.kbss.amaplas.controller;

import cz.cvut.kbss.amaplas.model.AbstractPlan;
import cz.cvut.kbss.amaplas.model.Aircraft;
import cz.cvut.kbss.amaplas.persistence.dao.AircraftDao;
import cz.cvut.kbss.amaplas.services.IdentifierService;
import cz.cvut.kbss.jsonld.JsonLd;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/aircrafts")
public class AircraftController extends BaseControllerImplementation<Aircraft, AircraftDao>{

//    protected final AircraftDao aircraftDao;

    public AircraftController(IdentifierService identifierService, AircraftDao aircraftDao) {
        super(identifierService, aircraftDao);
    }

//    @GetMapping(path = "/", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
//    public List<Aircraft> listAircraft(){
//        return getEntityDao().findAll();
//    }

//    @GetMapping(path= "/{planFragment}", produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
//    public Aircraft getAircraft(@PathVariable String planFragment,
//                                @RequestParam(required = false) Optional<String> ns){
//        URI aircraftUri = expandFragment(planFragment, ns);
//        return getEntityDao().find(aircraftUri).orElse(null);
//    }
//
//    @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE, JsonLd.MEDIA_TYPE})
//    public Aircraft getAircraftById(@RequestParam String revisionId){
//        return getEntityDao().findById(revisionId).orElse(null);
//    }
}
