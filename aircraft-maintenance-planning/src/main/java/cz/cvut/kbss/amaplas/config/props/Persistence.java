package cz.cvut.kbss.amaplas.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Configuration
@ConfigurationProperties(prefix = "persistence")
public class Persistence {
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
