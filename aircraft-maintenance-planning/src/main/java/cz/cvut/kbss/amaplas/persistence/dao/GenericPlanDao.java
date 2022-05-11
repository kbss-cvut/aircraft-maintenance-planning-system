package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.exceptions.DaoException;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.AbstractPlan;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public class GenericPlanDao extends BaseDao<AbstractPlan>{

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public GenericPlanDao(EntityManager em) {
        super(AbstractPlan.class, em);
    }

    public <P extends AbstractPlan> Stream<P> stream(Class<P> type) {
        try {
            return selectAll(type).getResultStream();
        } catch (RuntimeException e) {
            getLogger().error("Exception occurred", e);
            throw new DaoException(e);
        }
    }

    public <P extends AbstractPlan> List<P> findAll(Class<P> type) {
        try {
            return selectAll(type).getResultList();
        } catch (RuntimeException e) {
            getLogger().error("Exception occurred", e);
            throw new DaoException(e);
        }
    }

    public <P extends AbstractPlan> Optional<P> find(URI id, Class<P> type) {
        return (Optional<P>)super.find(id);
    }

    public <P extends AbstractPlan> Optional<P> getReference(URI id, Class<P> type) {
        return (Optional<P>)getReference(id);
    }

    @Transactional
    public <P extends AbstractPlan> P update(T plan, Class<P> type) {
        return (P)super.update(plan);
    }


    @Override
    public Logger getLogger() {
        return log;
    }
}
