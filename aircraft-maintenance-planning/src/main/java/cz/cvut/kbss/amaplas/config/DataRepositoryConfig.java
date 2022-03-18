package cz.cvut.kbss.amaplas.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("repository.data")
@Data
public class DataRepositoryConfig {
    private String url;
    private String username;
    private String password;
    private String taskDefinitionsGraph;
}
