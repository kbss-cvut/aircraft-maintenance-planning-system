package cz.cvut.kbss.amaplas.exp.optaplanner;

import cz.cvut.kbss.amaplas.exp.optaplanner.model.MaintenanceSchedule;
import cz.cvut.kbss.amaplas.exp.optaplanner.model.MaintenanceTask;
import cz.cvut.kbss.amaplas.exp.optaplanner.model.MaintenanceTaskAssignment;
import cz.cvut.kbss.amaplas.exp.optaplanner.model.TimeSlot;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MaintenanceScheduleGenerator{

    public MaintenanceSchedule generate(){
        MaintenanceSchedule schedule =  new MaintenanceSchedule();
        schedule.setTasks(Arrays.asList(
                new MaintenanceTask("name1", 1),
                new MaintenanceTask("name2", 2)
        ));
        schedule.setTimeSlots(Stream.of(1).map(TimeSlot::new).collect(Collectors.toList()));
        schedule.setMaintenanceTaskAssignmentList(
                IntStream.range(0, schedule.getTasks().size())
                        .mapToObj(
                                i -> new MaintenanceTaskAssignment((long)i, schedule.getTasks().get(i), null)
                        ).collect(Collectors.toList())
        );

        return schedule;
    }
}
