package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;

/**
 * Milestone plans are direct part of revision plan
 */
@OWLClass(iri = Vocabulary.s_c_milestone_plan)
public class MilestonePlan extends AbstractPlan{

}
