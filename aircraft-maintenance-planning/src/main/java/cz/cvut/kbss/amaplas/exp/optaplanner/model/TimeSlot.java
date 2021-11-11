package cz.cvut.kbss.amaplas.exp.optaplanner.model;

import org.optaplanner.core.api.domain.lookup.PlanningId;

import java.util.List;

public class TimeSlot {
    @PlanningId
    protected int id;

    public TimeSlot() {
    }

    public TimeSlot(int timeSlots) {
        this.id = timeSlots;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "TimeSlot{" +
                "id=" + id +
                '}';
    }
}
