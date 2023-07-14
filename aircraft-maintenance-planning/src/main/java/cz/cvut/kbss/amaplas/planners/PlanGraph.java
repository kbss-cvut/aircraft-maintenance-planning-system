package cz.cvut.kbss.amaplas.planners;

import cz.cvut.kbss.amaplas.model.TaskExecution;
import cz.cvut.kbss.amaplas.model.TaskType;
import cz.cvut.kbss.amaplas.utils.GraphmlUtils;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.nio.Attribute;
import org.jgrapht.nio.GraphExporter;
import org.jgrapht.nio.gml.GmlExporter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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

    public static Function<TaskPattern, String> vertexIdProvider = tp -> tp.getTaskType().getCode();
    public static Function<TaskPattern, Map<String, Attribute>> vertexAttributeProvider =
            GraphmlUtils.createDefaultAttributeProvider(
                    TaskPattern.class,
                    (TaskPattern tp) -> Optional.ofNullable(tp.getTaskType()).map(tt ->
                            tt.getCode() + " -- " +
                            (tt.getDefinition() != null ? tt.getDefinition() : tt).getArea() + " -- " +
                            escape(tt.getTitle())
                    ).orElse(""));

    public static String escape(String str){
        return Optional.ofNullable(str).map(s -> s.replaceAll("(&)", "_")).orElse("");
    }

    public static GraphExporter<TaskPattern, SequencePattern> exporter;
    {
        GmlExporter<TaskPattern, SequencePattern> e = new GmlExporter<>();
//        e.setVertexIdProvider(vertexIdProvider);
        e.setVertexAttributeProvider(vertexAttributeProvider);
        e.setParameter(GmlExporter.Parameter.EXPORT_VERTEX_LABELS,true);
        exporter = e;
    }
}
