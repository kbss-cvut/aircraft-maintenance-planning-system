package cz.cvut.kbss.amaplas.planners;

import cz.cvut.kbss.amaplas.model.TaskExecution;
import cz.cvut.kbss.amaplas.model.TaskType;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.List;
import java.util.function.Supplier;

public class PlanGraph extends DefaultDirectedGraph<TaskPattern, SequencePattern> {
    public PlanGraph() {
        super(null, () -> new SequencePattern(), true );
    }

    public PlanGraph(Class<? extends SequencePattern> edgeClass) {
        super(edgeClass);
    }

    public PlanGraph(Supplier<TaskPattern> vertexSupplier, Supplier<SequencePattern> edgeSupplier, boolean weighted) {
        super(vertexSupplier, edgeSupplier, weighted);
    }

    public TaskPattern addVertex(TaskType taskType, List<TaskExecution> instances, boolean planned){
        TaskPattern taskPattern = new TaskPattern(taskType, planned);
        taskPattern.setInstances(instances);
        super.addVertex(taskPattern);
        return taskPattern;
    }


}
