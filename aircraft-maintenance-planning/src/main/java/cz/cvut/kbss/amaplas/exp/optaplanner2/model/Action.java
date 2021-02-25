package cz.cvut.kbss.amaplas.exp.optaplanner2.model;

public interface Action {
    boolean applicableAt(Situation situation);

    Situation apply(Situation in);

}
