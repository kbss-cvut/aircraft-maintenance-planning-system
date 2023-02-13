package cz.cvut.kbss.amaplas;


import cz.cvut.kbss.amaplas.config.props.ConfigProperties;
import cz.cvut.kbss.amaplas.config.props.Persistence;
import cz.cvut.kbss.amaplas.config.props.Repository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ConfigProperties.class, Persistence.class, Repository.class})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


}
