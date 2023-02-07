package cz.cvut.kbss.amaplas.controller;

import cz.cvut.kbss.amaplas.model.Client;
import cz.cvut.kbss.amaplas.persistence.dao.ClientDao;
import cz.cvut.kbss.amaplas.services.IdentifierService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clients")
public class ClientController extends BaseControllerImplementation<Client, ClientDao> {
    public ClientController(IdentifierService identifierService, ClientDao entityDao) {
        super(identifierService, entityDao);
    }
}
