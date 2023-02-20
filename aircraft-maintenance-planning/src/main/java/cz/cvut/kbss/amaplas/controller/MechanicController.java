package cz.cvut.kbss.amaplas.controller;

import cz.cvut.kbss.amaplas.model.Mechanic;
import cz.cvut.kbss.amaplas.persistence.dao.MechanicDao;
import cz.cvut.kbss.amaplas.services.IdentifierService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mechanics")
public class MechanicController extends BaseControllerImplementation<Mechanic, MechanicDao> {
    public MechanicController(IdentifierService identifierService, MechanicDao entityDao) {
        super(identifierService, entityDao);
    }
}
