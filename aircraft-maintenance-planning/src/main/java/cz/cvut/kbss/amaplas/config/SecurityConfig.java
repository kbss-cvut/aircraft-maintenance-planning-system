package cz.cvut.kbss.amaplas.config;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationEntryPoint;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.util.HashMap;

@KeycloakConfiguration
class SecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${security.oauth2.disabled}")
    private Boolean disable;

    @Value("${keycloak-other.rewrite-redirect-uri-parameter:false}")
    private boolean shouldRewriteRedirectUri;

    public SecurityConfig(KeycloakSpringBootProperties keycloakProperties) {
        log.info("Using Keycloak configuration: {}", new HashMap<>() {{
            put("authServerUrl", keycloakProperties.getAuthServerUrl());
            put("proxyUrl", keycloakProperties.getProxyUrl());
            put("realm", keycloakProperties.getRealm());
            put("resource", keycloakProperties.getResource());
        }});
    }

    /**
     * Registers the KeycloakAuthenticationProvider with the authentication manager.
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }

    @Bean
    public KeycloakConfigResolver KeycloakConfigResolver() {
        return new KeycloakSpringBootConfigResolver();
    }

    /**
     * Defines the session authentication strategy.
     */
    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    protected AuthenticationEntryPoint authenticationEntryPoint() throws Exception {
        if(!shouldRewriteRedirectUri) {
            return new KeycloakAuthenticationEntryPoint(adapterDeploymentContext());
        }
        return new KeycloakAuthenticationEntryPoint(adapterDeploymentContext()) {
            @Override
            protected void commenceLoginRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
                HttpServletResponseWrapper wrappedResponse = new HttpServletResponseWrapper(response) {
                    @Override
                    public void sendRedirect(String location) throws IOException {
                        if(location != null && location.endsWith("sso/login")) {
                            StringBuilder sb = new StringBuilder();

                            location = sb
                                    .append("https://")
                                    .append(request.getServerName())
                                    .append(location).toString();
                        }
                        super.sendRedirect(location);
                    }
                };
                super.commenceLoginRedirect(request, wrappedResponse);
            }
        };
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.requestMatcher(disable ? none() : any()).authorizeRequests()
                .antMatchers("/**")
                .authenticated().and()
                .csrf().disable();
    }

    public RequestMatcher none() {
        return request -> false;
    }

    public RequestMatcher any() {
        return request -> true;
    }

}