package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.model.FailureAnnotation;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.query.TypedQuery;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class FailureAnnotationDao extends BaseDao<FailureAnnotation>{

    protected static final URI ID = URI.create(Vocabulary.s_p_id);
    protected static final URI PART_OF_WORKPACKAGE = URI.create(Vocabulary.s_p_is_part_of_workpackage);
    protected static final URI HAS_STEP_PART = URI.create(Vocabulary.s_p_has_work_order_step);


    public FailureAnnotationDao(EntityManager em) {
        super(FailureAnnotation.class, em);
    }

    public List<FailureAnnotation> listInWorkpackageId(String wpId){
        return getFindQuery("?wp ?id ?wpId .")
                .setParameter("wpId", wpId)
                .getResultList();
    }

    public List<FailureAnnotation> listInWorkpackageURI(URI workpackageURI){
        return getFindQuery()
                .setParameter("wp", workpackageURI)
                .getResultList();
    }

    protected TypedQuery<FailureAnnotation> getFindQuery(String ... triples){
        return getEm().createNativeQuery("SELECT DISTINCT ?failureAnnotation WHERE { " +
                        Arrays.stream(triples).collect(Collectors.joining("/n")) +
                        "?task ?partOfWp ?wp . \n" +
                        "?task ?hasStepPart ?failureAnnotation . \n" +
                        "?failureAnnotation a ?type . \n" +
                        "}", getType())
                .setParameter("partOfWp", PART_OF_WORKPACKAGE)
                .setParameter("hasStepPart", HAS_STEP_PART)
                .setParameter("type", getTypeUri());
    }
}
