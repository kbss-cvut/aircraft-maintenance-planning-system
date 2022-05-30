package cz.cvut.kbss.amaplas.environment;

import cz.cvut.kbss.amaplas.config.PersistenceConfigUtils;
import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.EntityManagerImpl;
import cz.cvut.kbss.jopa.sessions.UnitOfWork;
import cz.cvut.kbss.jopa.sessions.UnitOfWorkImpl;
import cz.cvut.kbss.ontodriver.sesame.config.SesameOntoDriverProperties;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;

import static cz.cvut.kbss.jopa.model.JOPAPersistenceProperties.*;

@Configuration
@EnableConfigurationProperties(cz.cvut.kbss.amaplas.config.ConfigProperties.class)
@Profile("test")
public class TestPersistenceFactory {

    private final cz.cvut.kbss.amaplas.config.ConfigProperties config;

    private EntityManagerFactory emf;

    @Autowired
    public TestPersistenceFactory(cz.cvut.kbss.amaplas.config.ConfigProperties config) {
        this.config = config;
    }

    @Bean
    @Primary
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    @PostConstruct
    private void init() {
        final Map<String, String> properties = PersistenceConfigUtils.createEntityManagerConfiguration(
                config.getRepository().getUrl(),
                null, null,
                config.getPersistence().getDriver()
        );
        properties.put(SesameOntoDriverProperties.SESAME_USE_VOLATILE_STORAGE, Boolean.TRUE.toString());
        properties.put(LANG, config.getPersistence().getLanguage());
        properties.put(PREFER_MULTILINGUAL_STRING, Boolean.TRUE.toString());
        this.emf = Persistence.createEntityManagerFactory("planningTestPU", properties);
//        EntityManager em = emf.createEntityManager();
//        em.getTransaction().begin();
//        Repository r = ((EntityManagerImpl)em).getCurrentPersistenceContext().unwrap(Repository.class);
//        RepositoryConnection c = r.getConnection();
//        em.unwrap(Object.class);
//        em.getTransaction().rollback();
//        em.clear();
//        em.close();
    }

    @PreDestroy
    private void close() {
        if (emf.isOpen()) {
            emf.close();
        }
    }
}
