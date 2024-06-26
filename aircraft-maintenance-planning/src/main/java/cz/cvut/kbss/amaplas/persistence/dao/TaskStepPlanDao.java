package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.model.AbstractEntity;
import cz.cvut.kbss.amaplas.model.FailureAnnotation;
import cz.cvut.kbss.amaplas.model.TaskStepPlan;
import cz.cvut.kbss.amaplas.persistence.dao.mapper.Bindings;
import cz.cvut.kbss.amaplas.persistence.dao.mapper.QueryResultMapper;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.query.TypedQuery;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.List;
import java.util.function.Supplier;

@Repository
public class TaskStepPlanDao extends BaseDao<TaskStepPlan> {
    public static final String TASK_STEP_AND_ANNOTATIONS = "/queries/analysis/task-step-and-annotations.sparql";

    protected static final URI ID = URI.create(Vocabulary.s_p_id);
    protected static final String TYPE_PREFIX = Vocabulary.ONTOLOGY_IRI_csat_maintenance + "/task-step-execution-type--";
    protected static final URI FILTER_TYPE = URI.create("http://www.w3.org/ns/csvw#Row");


    protected Supplier<QueryResultMapper<TaskStepPlan>> mapper = () -> new QueryResultMapper<>(TASK_STEP_AND_ANNOTATIONS) {
        @Override
        public TaskStepPlan convert() {
            TaskStepPlan taskStepPlan = new TaskStepPlan();

            mandatory("step", URI::create,  taskStepPlan::setEntityURI);
            mandatory("task", URI::create,  taskStepPlan::setParentTask);
            optional("stepIndex", taskStepPlan::setStepIndex);
            optional("workOrderText", taskStepPlan::setDescription);
            optional("workOrderActionText", taskStepPlan::setActionDescription);

            if(bs.hasBinding("annotatedText")){
                FailureAnnotation failureAnnotation = new FailureAnnotation();
                taskStepPlan.setFailureAnnotation(failureAnnotation);
                optional("annotatedText", failureAnnotation::setAnnotatedText);
                optional("componentUri", URI::create, failureAnnotation::setComponentUri);
                optional("componentLabel", failureAnnotation::setComponentLabel);
                optional("failureUri", URI::create, failureAnnotation::setFailureUri);
                optional("failureLabel", failureAnnotation::setFailureLabel);
                optional("aggregateScore", Double::parseDouble, failureAnnotation::setAggregateScore);
            }
            return taskStepPlan;
        }
    };

    public TaskStepPlanDao(EntityManager em, Rdf4JDao rdf4JDao) {
        super(TaskStepPlan.class, em, rdf4JDao);
    }


    @Override
    protected <E extends AbstractEntity> TypedQuery<E> selectAll(Class<E> type, URI typeUri){
        return getEm().createNativeQuery("SELECT DISTINCT ?x WHERE { ?x a ?type . FILTER(strstarts(str(?type),?typePrefix)) }", type)
                .setParameter("type", FILTER_TYPE)
                .setParameter("typePrefix", TYPE_PREFIX);
    }

    public List<TaskStepPlan> listInWorkpackage(URI workpackageURI){
        return load(mapper.get(), new Bindings().add("wp", workpackageURI));
    }

    public List<TaskStepPlan> listInWorkpackage(String workpackageId){
        return load(mapper.get(), new Bindings().add("wpId", workpackageId));
    }
}
