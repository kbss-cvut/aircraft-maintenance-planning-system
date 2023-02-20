package cz.cvut.kbss.amaplas.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties("repository")
public class Repository {
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
