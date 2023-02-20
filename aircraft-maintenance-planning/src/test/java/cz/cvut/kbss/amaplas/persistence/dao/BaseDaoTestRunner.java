package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.config.props.ConfigProperties;
import cz.cvut.kbss.amaplas.environment.TransactionalTestRunner;
import cz.cvut.kbss.amaplas.environment.config.TestPersistenceConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.aspectj.EnableSpringConfigured;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(ConfigProperties.class)
@EnableSpringConfigured
@ContextConfiguration(classes = {TestPersistenceConfig.class}, initializers = {ConfigDataApplicationContextInitializer.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public abstract class BaseDaoTestRunner extends TransactionalTestRunner {
}
