package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.jopa.model.annotations.OWLClass;

/**
 * Utility class for getting information about the entity - OWL class mapping.
 */
class EntityToOwlClassMapper {

    private EntityToOwlClassMapper() {
        throw new AssertionError();
    }

    /**
     * Gets IRI of the OWL class mapped by the specified entity.
     *
     * @param entityClass Entity class
     * @return IRI of mapped OWL class (as String)
     */
    static String getOwlClassForEntity(Class<?> entityClass) {
        final OWLClass owlClass = entityClass.getDeclaredAnnotation(OWLClass.class);
        if (owlClass != null) {
            return owlClass.iri();
        }
        return null;
    }

}