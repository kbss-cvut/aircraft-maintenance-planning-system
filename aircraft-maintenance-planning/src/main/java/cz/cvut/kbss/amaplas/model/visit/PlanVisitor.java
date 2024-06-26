package cz.cvut.kbss.amaplas.model.visit;

import cz.cvut.kbss.amaplas.model.*;

public interface PlanVisitor {
    default void visit(AbstractPlan plan) {};

    default void visit(AbstractComplexPlan plan){}

    default void visit(RevisionPlan plan){}
    default void visit(PhasePlan plan) {};
    default void visit(GeneralTaskPlan plan) {};
    default void visit(TaskPlan plan) {};
    default void visit(SessionPlan plan) {};
}
