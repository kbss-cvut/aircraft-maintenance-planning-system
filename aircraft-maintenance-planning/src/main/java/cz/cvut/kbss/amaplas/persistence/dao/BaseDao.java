package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.exceptions.PersistenceException;
import cz.cvut.kbss.amaplas.model.AbstractEntity;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.exceptions.NoResultException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.metamodel.Metamodel;
import cz.cvut.kbss.jopa.model.query.TypedQuery;
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

    protected static final URI ID = URI.create(Vocabulary.s_p_id);

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
            throw new PersistenceException(e);
        }
    }

    @Override
    public List<T> findAll() {
        try {
            return selectAll(type, typeUri).getResultList();
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    protected <E extends AbstractEntity> TypedQuery<E> selectAll(Class<E> type){
        return selectAll(type, URI.create(EntityToOwlClassMapper.getOwlClassForEntity(type)));
    }

    protected <E extends AbstractEntity> TypedQuery<E> selectAll(Class<E> type, URI typeUri){
        return em.createNativeQuery("SELECT DISTINCT ?x WHERE { ?x a ?type . }", type)
                .setParameter("type", typeUri);
    }

    @Override
    public Optional<T> find(URI id) {
        Objects.requireNonNull(id);
        try {
            return Optional.ofNullable(em.find(type, id));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public Optional<T> getReference(URI id) {
        Objects.requireNonNull(id);
        try {
            return Optional.ofNullable(em.getReference(type, id));
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    @Transactional
    public void persist(T entity) {
        Objects.requireNonNull(entity);
        try {
            em.persist(entity);
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    @Transactional
    public void persist(Collection<T> entities) {
        Objects.requireNonNull(entities);
        try {
            entities.forEach(em::persist);
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    @Transactional
    public T update(T entity) {
        Objects.requireNonNull(entity);
        try {
            return em.merge(entity);
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    @Transactional
    public void remove(T entity) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(entity.getEntityURI());
        try {
            final Optional<T> reference = getReference(entity.getEntityURI());
            reference.ifPresent(em::remove);
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    @Transactional
    public void removeById(URI id) {
        try {
            final Optional<T> reference = getReference(id);
            reference.ifPresent(em::remove);
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
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
            throw new PersistenceException(e);
        }
    }

    @Transactional
    public Optional<T> findById(Object id){
        Objects.requireNonNull(id);
        try {
            T entity = em.createNativeQuery("SELECT ?t {?t ?p ?id}", type)
                    .setParameter("p", ID)
                    .setParameter("id",id)
                    .getSingleResult();
//            T e1 = em.find(type, entity.getEntityURI());
            return Optional.of(entity);
        } catch (RuntimeException e) {
            throw new PersistenceException(e);
        }
    }

    @Transactional
    public boolean exists(Object id){
        try {
            Objects.requireNonNull(id);
            return em.createNativeQuery("ASK { ?x a ?type; ?p ?id. }", Boolean.class)
                    .setParameter("type", typeUri)
                    .setParameter("p", ID)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new PersistenceException(e);
        }
    }

    public Metamodel getMetamodel(){
        return em.getMetamodel();
    }


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
