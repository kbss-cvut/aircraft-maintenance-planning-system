package cz.cvut.kbss.amaplas.model.ops;

import cz.cvut.kbss.amaplas.model.AbstractComplexPlan;
import cz.cvut.kbss.amaplas.model.AbstractPlan;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CopySimplePlanProperties{

    /**
     * Copies changed simple properties from fromPlan to toPlan if they are instances of the same type. Simple properties
     * are considered properties not referring to other model entities.
     *
     * The copied simple properties are declared in the classes AbstractEntity and AbstractPlan.
     *
     * NOTE: This method should be revised if simple properties are when the plan for models change
     * @param fromPlan
     * @param toPlan
     */
    public void copyTo(AbstractPlan fromPlan, AbstractPlan toPlan){

        assert fromPlan != null && toPlan != null;
        if(fromPlan.equals(toPlan))

        // Copying simple properties declared in AbstractEntity
        setIfChanged(fromPlan::getTitle, toPlan::getTitle, toPlan::setTitle);

        // Copying simple properties declared in AbstractPlan
        setIfChanged(fromPlan::getPlannedStartTime, toPlan::getPlannedStartTime, toPlan::setPlannedStartTime);
        setIfChanged(fromPlan::getPlannedEndTime, toPlan::getPlannedEndTime, toPlan::setPlannedEndTime);
        setIfChanged(fromPlan::getPlannedDuration, toPlan::getPlannedDuration, toPlan::setPlannedDuration);
        setIfChanged(fromPlan::getPlannedWorkTime, toPlan::getPlannedWorkTime, toPlan::setPlannedWorkTime);
        setIfChanged(fromPlan::getStartTime, toPlan::getStartTime, toPlan::setStartTime);
        setIfChanged(fromPlan::getEndTime, toPlan::getEndTime, toPlan::setEndTime);
        setIfChanged(fromPlan::getDuration, toPlan::getDuration, toPlan::setDuration);
        setIfChanged(fromPlan::getWorkTime, toPlan::getWorkTime, toPlan::setWorkTime);

        if(AbstractComplexPlan.class.isAssignableFrom(fromPlan.getClass())){
//            copyTo((AbstractComplexPlan) fromPlan, (AbstractComplexPlan) toPlan);
        }
    }

    protected <T extends Object> void setIfChanged(Supplier<T> fromGetter,
                                Supplier<T> toGetter, Consumer<T> toSetter){
        T fromValue = fromGetter.get();
        T toValue = toGetter.get();
        if(fromValue != toValue)
            toSetter.accept(fromValue);
    }
}

