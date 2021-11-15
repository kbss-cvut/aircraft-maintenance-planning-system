package cz.cvut.kbss.amaplas.exp.optaplanner;

import cz.cvut.kbss.amaplas.exp.optaplanner.model.MaintenanceSchedule;
import cz.cvut.kbss.amaplas.exp.optaplanner.model.MaintenanceTask;
import cz.cvut.kbss.amaplas.exp.optaplanner.model.MaintenanceTaskAssignment;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.SolverJob;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.SolverConfig;
import org.optaplanner.core.config.solver.SolverManagerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ExecutionException;

public class PlannerApp {

    private static final Logger LOG = LoggerFactory.getLogger(PlannerApp.class);


    public static class SolutionHandler{
        private SolverJob<MaintenanceSchedule, Long> solverJob;
        private ScoreManager<MaintenanceSchedule, HardSoftScore> scoreManager;
        public SolverJob<MaintenanceSchedule, Long> getSolverJob() {
            return solverJob;
        }

        public void setSolverJob(SolverJob<MaintenanceSchedule, Long> solverJob) {
            this.solverJob = solverJob;
        }

        public void setScoreManager(ScoreManager<MaintenanceSchedule, HardSoftScore> scoreManager) {
            this.scoreManager = scoreManager;
        }

        public void handleSolution(MaintenanceSchedule ms)  {

            LOG.info("scheduling/planning finished. Scheduling/planning results are: {}");
            LOG.info("problem Id - {}", solverJob.getProblemId());
            LOG.info("solver status - {}", solverJob.getSolverStatus().name());
            LOG.info("scheduling/planning duration - {}", solverJob.getSolvingDuration());
            LOG.info("solution - \n{}", ms.toString());
            scoreManager.updateScore(ms);
            LOG.info("updated solution - \n{}", ms.toString());
            ms.getScore().toString();
//            System.out.println(solverJob.getFinalBestSolution().toString());
        }

    }


    public static void main(String[] args) throws ExecutionException, InterruptedException {

        ScoreDirectorFactoryConfig scoreFactory = new ScoreDirectorFactoryConfig();
        scoreFactory.withConstraintProviderClass(Constraints.class);
        SolverConfig solverConifg = new SolverConfig()
                .withTerminationSpentLimit(Duration.ofMillis(5000))
                .withConstraintProviderClass(Constraints.class)
                .withScoreDirectorFactory(scoreFactory)
                .withSolutionClass(MaintenanceSchedule.class)
                .withEntityClasses(MaintenanceTaskAssignment.class);

        SolverFactory<MaintenanceSchedule> solverFactory = SolverFactory.create(solverConifg);
        SolverManager<MaintenanceSchedule, Long> solverManager = SolverManager.create(solverFactory);
        ScoreManager<MaintenanceSchedule, HardSoftScore> scoreManager = ScoreManager.create(solverFactory);

        LOG.info("generate schedule job");
        MaintenanceSchedule m = new MaintenanceScheduleGenerator().generate();
        scoreManager.updateScore(m);
        LOG.info("initial score of plan - {}", m.getScore());

        SolutionHandler h = new SolutionHandler();
        h.setScoreManager(scoreManager);
        LOG.info("Start scheduling/planning ...");
        SolverJob<MaintenanceSchedule, Long> solverJob = solverManager.solve(1L, m, h::handleSolution);
        h.setSolverJob(solverJob);


    }
}
