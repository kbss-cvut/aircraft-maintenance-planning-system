package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.exceptions.PersistenceException;
import cz.cvut.kbss.amaplas.model.AbstractPlan;
import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public class GenericPlanDao extends BaseDao<AbstractPlan>{

    public GenericPlanDao(EntityManager em, Rdf4JDao rdf4JDao) {
        super(AbstractPlan.class, em, rdf4JDao);
    }

    public <P extends AbstractPlan> Stream<P> stream(Class<P> type) {
        try {
            return selectAll(type).getResultStream();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    public <P extends AbstractPlan> List<P> findAll(Class<P> type) {
        try {
            return selectAll(type).getResultList();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    public <P extends AbstractPlan> Optional<P> find(URI id, Class<P> type) {
        return (Optional<P>)super.find(id);
    }

    public <P extends AbstractPlan> Optional<P> getReference(URI id, Class<P> type) {
        return (Optional<P>)getReference(id);
    }

    @Transactional
    public <P extends AbstractPlan> P update(AbstractPlan plan, Class<P> type) {
        return (P)super.update(plan);
    }


    @Override
    public boolean exists(URI id) {
        try {
            Objects.requireNonNull(id);
            return getEm().createNativeQuery("ASK { ?x a/<http://www.w3.org/2000/01/rdf-schema#subClassOf>* ?type . }", Boolean.class)
                    .setParameter("x", id)
                    .setParameter("type", getTypeUri())
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new PersistenceException(e);
        }
    }
}
