package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.config.ConfigProperties;
import cz.cvut.kbss.amaplas.environment.config.TestPersistenceConfig;
import cz.cvut.kbss.amaplas.environment.config.TestServiceConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableTransactionManagement
@ExtendWith(SpringExtension.class)
@EnableSpringConfigured
@EnableConfigurationProperties(ConfigProperties.class)
@ContextConfiguration(classes = {
        TestPersistenceConfig.class,
        TestServiceConfig.class}, initializers = {ConfigDataApplicationContextInitializer.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class BaseServiceTestRunner {
}
