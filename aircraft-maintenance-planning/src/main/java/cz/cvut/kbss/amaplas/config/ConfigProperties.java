package cz.cvut.kbss.amaplas.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
@Data
public class ConfigProperties {
    private String prop;
    private DataRepositoryConfig repository;

    @Autowired
    public ConfigProperties(DataRepositoryConfig repository) {
        this.repository = repository;
    }
}
