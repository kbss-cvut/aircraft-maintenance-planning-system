package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.model.Mechanic;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class MechanicDao extends BaseDao<Mechanic> {
    public MechanicDao(EntityManager em) {
        super(Mechanic.class, em);
    }
}
