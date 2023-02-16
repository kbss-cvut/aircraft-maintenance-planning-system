package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.model.Workpackage;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class WorkpackageDAO extends BaseDao<Workpackage>{

    public WorkpackageDAO(EntityManager em) {
        super(Workpackage.class, em);
    }
}
