package cz.cvut.kbss.amaplas.model;


import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;

import java.util.Set;

/**
 * ResourceStatus plans are direct part of revision plan
 */
@OWLClass(iri = Vocabulary.s_c_resource_status_plan)
public class ResourceStatusPlan extends AbstractPlan{
    @OWLObjectProperty(iri = Vocabulary.s_p_requires)
    protected Set<ResourceRestriction> resourceRestrictions;

    public Set<ResourceRestriction> getResourceRestrictions() {
        return resourceRestrictions;
    }

    public void setResourceRestrictions(Set<ResourceRestriction> resourceRestrictions) {
        this.resourceRestrictions = resourceRestrictions;
    }
}
