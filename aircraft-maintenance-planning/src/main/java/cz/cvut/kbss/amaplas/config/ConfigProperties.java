package cz.cvut.kbss.amaplas.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class ConfigProperties {
    private String prop;
    private DataRepositoryConfig repository;

    @Autowired
    public ConfigProperties(DataRepositoryConfig repository) {
        this.repository = repository;
    }

    public String getProp() {
        return prop;
    }

    public void setProp(String prop) {
        this.prop = prop;
    }

    public DataRepositoryConfig getRepository() {
        return repository;
    }

    public void setRepository(DataRepositoryConfig repository) {
        this.repository = repository;
    }
}
