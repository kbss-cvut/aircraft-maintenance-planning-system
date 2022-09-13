package cz.cvut.kbss.amaplas.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.AdapterTokenStore;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.OAuthRequestAuthenticator;
import org.keycloak.adapters.RequestAuthenticator;
import org.keycloak.adapters.spi.HttpFacade;
import org.keycloak.adapters.springsecurity.authentication.SpringSecurityRequestAuthenticator;
import org.keycloak.adapters.springsecurity.authentication.SpringSecurityRequestAuthenticatorFactory;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * Allows to rewrite the value of the redirect_uri parameter in the uri in the location header of the response generated
 * by keycloak for an unauthenticated user request.
 */
@Component
public class KeycloackAuthenticationProcessingFilterPostProcessor implements BeanPostProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(KeycloackAuthenticationProcessingFilterPostProcessor.class);

    @Value("${keycloak-other.rewrite-redirect-uri-parameter:false}")
    private boolean shouldRewriteRedirectUri;



    /**
     * The KeycloakAuthenticationProcessingFilter uses an instance of a SpringSecurityRequestAuthenticatorFactory which
     * generates the value for the redirect_uri parameter. The generated value is based on the request url. This method
     * changes the filter instance by replacing its request authenticator factory for a custom implementation.
     * @param filter
     */
    private void process(KeycloakAuthenticationProcessingFilter filter) {
        filter.setRequestAuthenticatorFactory(new SpringSecurityRequestAuthenticatorFactory() {
            @Override
            public RequestAuthenticator createRequestAuthenticator(HttpFacade facade, HttpServletRequest request, KeycloakDeployment deployment, AdapterTokenStore tokenStore, int sslRedirectPort) {
                return new SpringSecurityRequestAuthenticator(facade, request, deployment, tokenStore, sslRedirectPort) {
                    @Override
                    protected OAuthRequestAuthenticator createOAuthAuthenticator() {
                        return new OAuthRequestAuthenticator(this, facade, deployment, sslRedirectPort, tokenStore) {

                            @Override
                            protected String getRequestUrl() {
                                return super.getRequestUrl().replaceFirst("^http([^s])", "https$1");
                            }
                        };
                    }
                };
            }
        });
    }

    /**
     * Get the KeycloakAuthenticationProcessingFilter instance after initialization and changed as required.
     * @param bean the new bean instance
     * @param beanName the name of the bean
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(shouldRewriteRedirectUri) {
            if (bean instanceof KeycloakAuthenticationProcessingFilter) {
                LOG.info("Injecting Custom handler...");
                process(((KeycloakAuthenticationProcessingFilter) bean));
            }
        }
        return bean;
    }
//
//    public static void main(String[] args) {
//        String uri = "http://localhost:8080/api/revisions/";
//
//        System.out.println(uri);
//        System.out.println(uri.replaceFirst("^http([^s])", "https$1"));
//    }
}
