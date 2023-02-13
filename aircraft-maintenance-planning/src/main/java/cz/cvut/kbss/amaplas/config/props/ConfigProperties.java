package cz.cvut.kbss.amaplas.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConfigurationProperties("planning")
@Primary
public class ConfigProperties {
    private String prop;
    private Repository repository;
    private Persistence persistence;

    public String getProp() {
        return prop;
    }

    public void setProp(String prop) {
        this.prop = prop;
    }

    public Repository getRepository() {
        return repository;
    }

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    public Persistence getPersistence() {
        return persistence;
    }

    public void setPersistence(Persistence persistence) {
        this.persistence = persistence;
    }

}
