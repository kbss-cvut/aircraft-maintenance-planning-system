package cz.cvut.kbss.amaplas.environment.config;

import cz.cvut.kbss.amaplas.config.PersistenceConfig;
import cz.cvut.kbss.amaplas.environment.TestPersistenceFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@TestConfiguration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import({TestPersistenceFactory.class, PersistenceConfig.class})
@ComponentScan(basePackages = "cz.cvut.kbss.amaplas.persistence")
@EnableTransactionManagement
public class TestPersistenceConfig {
}
