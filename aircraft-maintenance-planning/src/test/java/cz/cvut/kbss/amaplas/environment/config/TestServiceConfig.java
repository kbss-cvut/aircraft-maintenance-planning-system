package cz.cvut.kbss.amaplas.environment.config;

import cz.cvut.kbss.amaplas.environment.Environment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.mockito.Mockito.mock;

@TestConfiguration
@ComponentScan(basePackages = "cz.cvut.kbss.amaplas.services")
public class TestServiceConfig {

//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

//    @Bean
//    public LocalValidatorFactoryBean validatorFactoryBean() {
//        return new LocalValidatorFactoryBean();
//    }
//
//
//    @Bean
//    public ClassPathResource languageSpecification() {
//        return new ClassPathResource("languages/language.ttl");
//    }
//
//
//    @Bean
//    @Primary
//    public ApplicationEventPublisher eventPublisher() {
//        return mock(ApplicationEventPublisher.class);
//    }
}
