package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;

@OWLClass(iri = Vocabulary.s_c_mechanic)
public class Mechanic extends Resource{

    @OWLDataProperty(iri = Vocabulary.s_p_member_of_group)
    protected String belongsToGroup;

    public String getBelongsToGroup() {
        return belongsToGroup;
    }

    public void setBelongsToGroup(String belongsToGroup) {
        this.belongsToGroup = belongsToGroup;
    }
}
