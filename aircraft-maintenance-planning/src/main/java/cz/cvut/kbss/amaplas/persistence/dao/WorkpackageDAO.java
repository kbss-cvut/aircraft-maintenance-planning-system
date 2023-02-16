package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.model.Workpackage;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Repository
public class WorkpackageDAO extends BaseDao<Workpackage>{


    protected static final URI p_workpackage_end_time = URI.create(Vocabulary.s_p_workpackage_end_time);
    public WorkpackageDAO(EntityManager em) {
        super(Workpackage.class, em);
    }

    public List<Workpackage> findAllClosed() {
        return getEm().createNativeQuery(
              "SELECT ?w WHERE {\n" +
                            "?w a ?type; \n" +
                            "?endTimeProperty ?closeDate. \n" +
                            "FILTER(?closeDate < ?now)\n" +
                        "}" ,
                        Workpackage.class
                )
                .setParameter("type", getTypeUri())
                .setParameter("endTimeProperty", p_workpackage_end_time)
                .setParameter("now", LocalDate.now()).getResultList();
    }

    public List<Workpackage> findAllOpened() {
        return getEm().createNativeQuery(
                        "SELECT ?w WHERE {\n" +
                                "?w a ?type; \n" +
                                "?endTimeProperty ?closeDate. \n" +
                                "FILTER(?closeDate > ?now)\n" +
                                "}" ,
                        Workpackage.class
                )
                .setParameter("type", getTypeUri())
                .setParameter("endTimeProperty", p_workpackage_end_time)
                .setParameter("now", LocalDate.now()).getResultList();
    }
}
