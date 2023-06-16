package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.model.Aircraft;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class AircraftDao extends BaseDao<Aircraft> {

    public AircraftDao(EntityManager em, Rdf4JDao rdf4JDao) {
        super(Aircraft.class, em, rdf4JDao);
    }
}
