package cz.cvut.kbss.amaplas.exp.optaplanner;

import cz.cvut.kbss.amaplas.exp.optaplanner.model.MaintenanceJob;
import cz.cvut.kbss.amaplas.exp.optaplanner.model.MaintenanceTask;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This is experiment that demonstrates the use of OptaPlanner https://www.optaplanner.org/.
 * Additional classes and resources used by this example are
 * classes in package - in cz.cvut.kbss.amaplas.exp.optaplanner.model
 * resources - maintenanceJobSolverConfiguration.xml
 * This example was created based on the Tutorial at https://www.baeldung.com/opta-planner
 *
 * Literature on planning (planning agent activity to reach a goal) can be found at http://lavalle.pl/planning/
 * Other libraries for agent activity planning
 *  - http://www.philippe-fournier-viger.com/plplan/plplan_example1.php
 *  - http://oi-by-teaparty.eu/domains/oi-by-teaparty.eu/doku.php/courses/a4m33pah
 *
 */
public class OptaPlannerExperiment1 {
    public static void main(String[] args) {
        // create and configure the solver
        SolverFactory<MaintenanceJob> solverFactory = SolverFactory
                .createFromXmlResource("maintenanceJobSolverConfiguration.xml");
        Solver<MaintenanceJob> solver = solverFactory.buildSolver();

        // construct the problem
        MaintenanceJob unsolvedCourseSchedule = new MaintenanceJob();

        List<Integer> indices = IntStream.range(0, 1000).boxed()
                .collect(Collectors.toList());
        List<MaintenanceTask> tasks = indices.stream().map(i -> new MaintenanceTask("task-" + i, i))
                .collect(Collectors.toList());
        unsolvedCourseSchedule.setTaskOrderIndices(indices);
        unsolvedCourseSchedule.setTasks(tasks);


        // solve the problem
        System.out.println("solving example problem...");
        long solutionTime = System.currentTimeMillis();
        MaintenanceJob solved = solver.solve(unsolvedCourseSchedule);
        solved.getTasks().sort(Comparator.comparing(mt -> mt.getTaskOrderIndex()));
        solutionTime = System.currentTimeMillis() - solutionTime;
        System.out.println(String.format("solution found in %f [s]", solutionTime/1000.));
        System.out.println(String.format("Solution with score = %s", solved.getScore().toString()));

        solved.getTasks().forEach(
                t -> System.out.println(
                        String.format("[%02d] - %s", t.getTaskOrderIndex(), t.getName())
                )
        );


    }
}
