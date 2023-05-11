package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//@OWLClass(iri = Vocabulary.s_c_maintenance_group)
public class Scope extends AbstractEntity {

    public static final Set<String> PRIORITY_SCOPE = new HashSet<>(Arrays.asList(
            "AVIO",
            "MECH-LDG",
            "MECH-INT",
            "MECH-EXT",
            "MECH-ENG",
            "SHM"
    )) ;

    public static boolean isPriority(String scope){
        return PRIORITY_SCOPE.contains(scope);
    }

    protected String abbreviation;

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }
}
