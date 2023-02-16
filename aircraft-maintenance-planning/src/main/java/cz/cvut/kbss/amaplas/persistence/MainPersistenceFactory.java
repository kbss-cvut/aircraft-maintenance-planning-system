package cz.cvut.kbss.amaplas.persistence;

import cz.cvut.kbss.amaplas.config.props.ConfigProperties;
import cz.cvut.kbss.amaplas.config.PersistenceConfigUtils;
import cz.cvut.kbss.jopa.Persistence;
import cz.cvut.kbss.jopa.model.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.util.Map;

@Configuration
@Profile("!test")
public class MainPersistenceFactory {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final ConfigProperties config;
    private EntityManagerFactory emf;

    public MainPersistenceFactory(ConfigProperties config) {
        this.config = config;
    }

    @PostConstruct
    public void init(){
        String repositoryUrl = config.getRepository().getUrl();
        String repositoryUsername = config.getRepository().getUsername();
        String repositoryPassword = config.getRepository().getPassword();
        String driver = config.getPersistence().getDriver();
        emf = createEntityManagerFactory(repositoryUrl, repositoryUsername, repositoryPassword, driver);
        log.info("EntityManagerFactory created with repository url <{}> .", repositoryUrl);
    }

    @Bean(destroyMethod = "close")
    @Primary
    public EntityManagerFactory entityManagerFactory() {
        return emf;
    }



    private EntityManagerFactory createEntityManagerFactory(String repositoryUrl, String repositoryUsername,
                                                            String repositoryPassword, String driver) {
        log.info("Using repository: {}", repositoryUrl);
        final Map<String, String> properties = PersistenceConfigUtils.createEntityManagerConfiguration(repositoryUrl, repositoryUsername, repositoryPassword, driver);
        return Persistence.createEntityManagerFactory("emf", properties);
    }
}
