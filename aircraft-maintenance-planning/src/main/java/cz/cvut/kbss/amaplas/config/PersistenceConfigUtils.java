package cz.cvut.kbss.amaplas.config;

import cz.cvut.kbss.jopa.model.JOPAPersistenceProperties;
import cz.cvut.kbss.jopa.model.JOPAPersistenceProvider;
import cz.cvut.kbss.ontodriver.config.OntoDriverProperties;
import cz.cvut.kbss.ontodriver.rdf4j.config.Rdf4jOntoDriverProperties;

import java.util.HashMap;
import java.util.Map;

public class PersistenceConfigUtils {
    public static Map<String, String> createEntityManagerConfiguration(String repositoryUrl, String repositoryUsername,
                                                                String repositoryPassword, String driver){
        final Map<String, String> properties = new HashMap<>();
        properties.put(JOPAPersistenceProperties.SCAN_PACKAGE, "cz.cvut.kbss.amaplas.model");
        properties.put(JOPAPersistenceProperties.JPA_PERSISTENCE_PROVIDER, JOPAPersistenceProvider.class.getName());
        properties.put(JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY, repositoryUrl);
        properties.put(JOPAPersistenceProperties.DATA_SOURCE_CLASS, driver);
//        properties.put(JOPAPersistenceProperties.LANG, "en");
        properties.put(JOPAPersistenceProperties.PREFER_MULTILINGUAL_STRING, Boolean.FALSE.toString());
        properties.put(Rdf4jOntoDriverProperties.LOAD_ALL_THRESHOLD, "1");
        properties.put(OntoDriverProperties.USE_TRANSACTIONAL_ONTOLOGY, Boolean.TRUE.toString());
        if (repositoryUsername != null) {
            properties.put(OntoDriverProperties.DATA_SOURCE_USERNAME, repositoryUsername);
            properties.put(OntoDriverProperties.DATA_SOURCE_PASSWORD, repositoryPassword);
        }
        return properties;
    }
}
