package cz.cvut.kbss.amaplas.model;


import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;

import java.util.Set;

/**
 * ResourceStatus plans are direct part of revision plan
 */
@OWLClass(iri = Vocabulary.s_c_resource_status_plan)
public class RestrictionPlan extends AbstractPlan{
    @OWLObjectProperty(iri = Vocabulary.s_p_requires)
    protected Set<Restriction> restrictions;

    @OWLDataProperty(iri = Vocabulary.s_p_requiring_plan)
    protected Set<AbstractPlan> requiringPlans;

    public Set<Restriction> getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(Set<Restriction> restrictions) {
        this.restrictions = restrictions;
    }

    public Set<AbstractPlan> getRequiringPlans() {
        return requiringPlans;
    }

    public void setRequiringPlans(Set<AbstractPlan> requiringPlans) {
        this.requiringPlans = requiringPlans;
    }
}
