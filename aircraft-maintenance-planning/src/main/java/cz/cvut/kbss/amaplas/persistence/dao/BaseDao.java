package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.exceptions.DaoException;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.AbstractEntity;
import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.query.TypedQuery;
import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Base implementation of the generic DAO API.
 */
public abstract class BaseDao<T extends AbstractEntity> implements GenericDao<T> {

    private final Class<T> type;
    private final URI typeUri;
    private final EntityManager em;

    protected BaseDao(Class<T> type, EntityManager em) {
        this.type = type;
        this.typeUri = URI.create(EntityToOwlClassMapper.getOwlClassForEntity(type));
        this.em = em;
    }

    @Override
    public Stream<T> stream() {
        try {
            return selectAll(type, typeUri).getResultStream();
        } catch (RuntimeException e) {
            getLogger().error("Exception occurred", e);
            throw new DaoException(e);
        }
    }

    @Override
    public List<T> findAll() {
        try {
            return selectAll(type, typeUri).getResultList();
        } catch (RuntimeException e) {
            getLogger().error("Exception occurred", e);
            throw new DaoException(e);
        }
    }

    protected <E extends AbstractEntity> TypedQuery<E> selectAll(Class<E> type){
        return selectAll(type, URI.create(EntityToOwlClassMapper.getOwlClassForEntity(type)));

    }

    protected <E extends AbstractEntity> TypedQuery<E> selectAll(Class<E> type, URI typeUri){
        return em.createNativeQuery("SELECT DISTINCT ?x WHERE { ?x a ?type . }", type)
                .setParameter("type", EntityToOwlClassMapper.getOwlClassForEntity(type));
    }

    @Override
    public Optional<T> find(URI id) {
        Objects.requireNonNull(id);
        return Optional.ofNullable(em.find(type, id));
    }

    @Override
    public Optional<T> getReference(URI id) {
        Objects.requireNonNull(id);
        return Optional.ofNullable(em.getReference(type, id));
    }

    @Override
    @Transactional
    public void persist(T entity) {
        Objects.requireNonNull(entity);
        em.persist(entity);
    }

    @Override
    @Transactional
    public void persist(Collection<T> entities) {
        Objects.requireNonNull(entities);
        entities.forEach(em::persist);
    }

    @Override
    @Transactional
    public T update(T entity) {
        Objects.requireNonNull(entity);
        return em.merge(entity);
    }

    @Override
    @Transactional
    public void remove(T entity) {
        final Optional<T> reference = getReference(entity.getEntityURI());
        reference.ifPresent(em::remove);
    }

    @Override
    @Transactional
    public void removeById(URI id) {
        final Optional<T> reference = getReference(id);
        reference.ifPresent(em::remove);
    }

    @Override
    public boolean exists(URI id) {
        try {
            Objects.requireNonNull(id);
            return em.createNativeQuery("ASK { ?x a ?type . }", Boolean.class)
                    .setParameter("x", id)
                    .setParameter("type", typeUri)
                    .getSingleResult();
        } catch (NoResultException e) {
            return false;
        }
    }

    public abstract Logger getLogger();

    public Class<T> getType() {
        return type;
    }

    public URI getTypeUri() {
        return typeUri;
    }

    public EntityManager getEm() {
        return em;
    }
}
