package cz.cvut.kbss.amaplas.environment.config;

import cz.cvut.kbss.amaplas.config.ConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;

@EnableSpringConfigured
@EnableConfigurationProperties({ConfigProperties.class})
public class TestConfig {

    @Autowired
    private ConfigProperties configuration;

//    @Bean
//    @Primary
    public ConfigProperties configuration() {
        return configuration;
    }
}
