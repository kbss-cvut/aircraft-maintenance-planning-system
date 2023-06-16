package cz.cvut.kbss.amaplas.persistence.dao;

import com.github.ledsoft.jopa.spring.transaction.DelegatingEntityManager;
import cz.cvut.kbss.amaplas.persistence.dao.mapper.Bindings;
import cz.cvut.kbss.amaplas.persistence.dao.mapper.QueryResultMapper;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class Rdf4JDao {

    protected final EntityManager em;

    public Rdf4JDao(EntityManager em) {
        this.em = em;
    }

    protected <T> List<T> load(QueryResultMapper<T> mapper, Bindings bindings){
        TupleQueryResult rawResults = executeSelect(mapper.getQuery(), bindings);
        return mapper.convert(rawResults);
    }

    protected TupleQueryResult executeSelect(String query, Bindings bindings){
        RepositoryConnection c = getRepositoryConnection();
        TupleQuery tupleQuery = c.prepareTupleQuery(query);
        if(bindings != null)
            bindings.stream().forEach(b -> tupleQuery.setBinding(b.getKey(), b.getValue()));

        return tupleQuery.evaluate();
    }

    protected RepositoryConnection getRepositoryConnection(){
        EntityManager entityManager = em;
        if(entityManager instanceof DelegatingEntityManager){
            entityManager = ((EntityManager)entityManager.getDelegate());
        }

        org.eclipse.rdf4j.repository.Repository repo = entityManager.unwrap(org.eclipse.rdf4j.repository.Repository.class);

        return Optional.ofNullable(repo)
                .map(r -> r.getConnection()).orElse(null);
    }

}
