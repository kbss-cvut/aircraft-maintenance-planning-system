package cz.cvut.kbss.amaplas.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.validation.constraints.NotNull;

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

    @Configuration
    @ConfigurationProperties(prefix = "persistence")
    public static class Persistence {
        /**
         * OntoDriver class for the repository.
         */
        @NotNull
        String driver;
        /**
         * Language used to store strings in the repository (persistence unit language).
         */
//        @NotNull
        String language;

        public String getDriver() {
            return driver;
        }

        public void setDriver(String driver) {
            this.driver = driver;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }
    }

    @Configuration
    @ConfigurationProperties("repository")
    public static class Repository {
        @NotNull
        String url;
        String username;
        String password;
        String taskDefinitionsGraph;
        String taskMappingGraph;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getTaskDefinitionsGraph() {
            return taskDefinitionsGraph;
        }

        public void setTaskDefinitionsGraph(String taskDefinitionsGraph) {
            this.taskDefinitionsGraph = taskDefinitionsGraph;
        }

        public String getTaskMappingGraph() {
            return taskMappingGraph;
        }

        public void setTaskMappingGraph(String taskMappingGraph) {
            this.taskMappingGraph = taskMappingGraph;
        }
    }
}
