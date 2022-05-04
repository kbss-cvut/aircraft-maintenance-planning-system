package cz.cvut.kbss.amaplas.environment;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cvut.kbss.amaplas.config.JacksonConfig;
import cz.cvut.kbss.amplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jsonld.JsonLd;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class Environment {

    public static final String BASE_URI = Vocabulary.ONTOLOGY_IRI_aircraft_maintenance_planning;

    public static final String LANGUAGE = "en";


    private static ObjectMapper objectMapper;

    private static ObjectMapper jsonLdObjectMapper;




    /**
     * Gets a Jackson {@link ObjectMapper} for mapping JSON-LD to Java and vice versa.
     *
     * @return {@code ObjectMapper}
     */
    public static ObjectMapper getJsonLdObjectMapper() {
        if (jsonLdObjectMapper == null) {
            jsonLdObjectMapper = JacksonConfig.createJsonLdObjectMapper();
        }
        return jsonLdObjectMapper;
    }

    /**
     * Creates a Jackson JSON-LD message converter.
     *
     * @return JSON-LD message converter
     */
    public static HttpMessageConverter<?> createJsonLdMessageConverter() {
        final MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter(
                getJsonLdObjectMapper());
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.valueOf(JsonLd.MEDIA_TYPE)));
        return converter;
    }

    public static HttpMessageConverter<?> createStringEncodingMessageConverter() {
        return new StringHttpMessageConverter(StandardCharsets.UTF_8);
    }

    public static HttpMessageConverter<?> createResourceMessageConverter() {
        return new ResourceHttpMessageConverter();
    }

    public static InputStream loadFile(String file) {
        return Environment.class.getClassLoader().getResourceAsStream(file);
    }

    /**
     * Loads ontological model into the underlying repository, so that RDFS inference (mainly class and property
     * hierarchy) can be exploited.
     * <p>
     * Note that the specified {@code em} has to be transactional, so that a connection to the underlying repository is
     * open.
     *
     * @param em Transactional {@code EntityManager} used to unwrap the underlying repository
     */
    public static void addModelStructureForRdfsInference(EntityManager em) {
        final Repository repo = em.unwrap(Repository.class);
        try (final RepositoryConnection conn = repo.getConnection()) {
            conn.begin();
//            conn.add(new URL("https://www.w3.org/1999/02/22-rdf-syntax-ns"), "", RDFFormat.RDFXML);
//            conn.add(new URL("https://www.w3.org/2000/01/rdf-schema"), "", RDFFormat.RDFXML);
            conn.add(Environment.class.getResource("/aircraft-maintenance-planning.ttl"), BASE_URI, RDFFormat.TURTLE);
            conn.commit();
        } catch (IOException e) {
            throw new RuntimeException("Unable to load ontology model for import.", e);
        }
    }
}
