package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;

@OWLClass(iri = Vocabulary.s_c_phase_plan)
public class PhasePlan extends AbstractComplexPlan<GeneralTaskPlan> {
}
