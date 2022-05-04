package cz.cvut.kbss.amaplas.config;

import com.github.ledsoft.jopa.spring.transaction.DelegatingEntityManager;
import com.github.ledsoft.jopa.spring.transaction.JopaTransactionManager;
import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProvider;
import cz.cvut.kbss.ontodriver.config.OntoDriverProperties;
import cz.cvut.kbss.ontodriver.sesame.config.SesameOntoDriverProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableSpringConfigured
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class PersistenceConfig {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Bean
    @Primary
    public DelegatingEntityManager entityManager() {
        return new DelegatingEntityManager();
    }


    @Bean(destroyMethod = "close")
    @Primary
    public EntityManagerFactory entityManagerFactory(@Value(value = "${repository.data.url}") String repositoryUrl,
                                                     @Value(value = "${repository.data.username:#{null}}") String repositoryUsername,
                                                     @Value(value = "${repository.data.password:#{null}}") String repositoryPassword,
                                                     @Value(value = "${persistence.driver}") String driver) {
        return createEntityManagerFactory(repositoryUrl, repositoryUsername, repositoryPassword, driver);
    }



    private EntityManagerFactory createEntityManagerFactory(String repositoryUrl, String repositoryUsername,
                                                            String repositoryPassword, String driver) {
        log.info("Using repository: {}", repositoryUrl);
        final Map<String, String> properties = new HashMap<>();
        properties.put(JOPAPersistenceProperties.SCAN_PACKAGE, "cz.cvut.kbss.amaplas");
        properties.put(JOPAPersistenceProperties.JPA_PERSISTENCE_PROVIDER, JOPAPersistenceProvider.class.getName());
        properties.put(JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY, repositoryUrl);
        properties.put(JOPAPersistenceProperties.DATA_SOURCE_CLASS, driver);
//        properties.put(JOPAPersistenceProperties.LANG, "en");
        properties.put(JOPAPersistenceProperties.PREFER_MULTILINGUAL_STRING, Boolean.FALSE.toString());
        properties.put(SesameOntoDriverProperties.SESAME_LOAD_ALL_THRESHOLD, "1");
        properties.put(OntoDriverProperties.USE_TRANSACTIONAL_ONTOLOGY, Boolean.TRUE.toString());
        if (repositoryUsername != null) {
            properties.put(OntoDriverProperties.DATA_SOURCE_USERNAME, repositoryUsername);
            properties.put(OntoDriverProperties.DATA_SOURCE_PASSWORD, repositoryPassword);
        }
        return Persistence.createEntityManagerFactory("emf", properties);
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf,
                                                         DelegatingEntityManager emProxy) {
        return new JopaTransactionManager(emf, emProxy);
    }
}
