package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.model.Client;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class ClientDao extends BaseDao<Client> {
    public ClientDao(EntityManager em) {
        super(Client.class, em);
    }
}
