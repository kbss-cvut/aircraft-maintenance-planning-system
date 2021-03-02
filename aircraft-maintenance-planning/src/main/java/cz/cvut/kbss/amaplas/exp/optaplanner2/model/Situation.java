package cz.cvut.kbss.amaplas.exp.optaplanner2.model;

public interface Situation {
    boolean contains(Situation situation);
    default boolean isContainedIn(Situation situation){
        return situation.contains(this);
    }



}
