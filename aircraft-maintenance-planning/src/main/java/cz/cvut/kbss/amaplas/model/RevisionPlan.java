package cz.cvut.kbss.amaplas.model;

import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;

@OWLClass(iri = Vocabulary.s_c_workpackage_plan)
public class RevisionPlan extends AbstractComplexPlan<PhasePlan>{
    public void addPlanPart(RestrictionPlan restrictionPlan){
        planParts.add(restrictionPlan);
    }

    public boolean removePlanPart(RestrictionPlan restrictionPlan){
        return planParts.remove(restrictionPlan);
    }

    public void addPlanPart(MilestonePlan milestonePlan){
        planParts.add(milestonePlan);
    }

    public boolean removePlanPart(MilestonePlan milestonePlan){
        return planParts.remove(milestonePlan);
    }
}
