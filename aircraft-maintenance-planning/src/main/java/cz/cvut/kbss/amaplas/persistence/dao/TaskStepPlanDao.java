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
//    protected static final URI PART_OF_WORKPACKAGE = URI.create(Vocabulary.s_p_is_part_of_workpackage);
//    protected static final URI HAS_STEP_PART = URI.create(Vocabulary.s_p_has_work_order_step);
//    protected static final URI SUB_CLASS_OF = URI.create(RDFS.SUB_CLASS_OF);
//    protected static final URI SUPER_TYPE = URI.create(Vocabulary.s_c_work_order_step);
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
                optional("componentScore", Double::parseDouble, failureAnnotation::setComponentScore);
                optional("failureUri", URI::create, failureAnnotation::setFailureUri);
                optional("failureLabel", failureAnnotation::setFailureLabel);
                optional("failureScore", Double::parseDouble, failureAnnotation::setFailureScore);
                optional("aggregateScore", Double::parseDouble, failureAnnotation::setAggregateScore);
                optional("isConfirmed", failureAnnotation::setConfirmed);
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

//    public List<TaskStepPlan> listInWorkpackageId(String wpId){
//        return getFindQuery("?wp ?id ?wpId .")
//                .setParameter("wpId", wpId)
//                .getResultList();
//    }
//
//    public List<TaskStepPlan> listURIsInWorkpackageURI(URI workpackageURI){
//        return getFindQuery()
//                .setParameter("wp", workpackageURI)
//                .getResultList();
//    }

//    protected TypedQuery<TaskStepPlan> getFindQuery(String ... triples){
//        return getEm().createNativeQuery("SELECT DISTINCT ?taskStep ?task WHERE { \n" +
//                        Arrays.stream(triples).collect(Collectors.joining("/n")) +
//                        "?task ?partOfWp ?wp . \n" +
//                        "?task ?hasStepPart ?taskStep . \n" +
//                        "?taskStep a ?type, ?filteredType, ?type2 . \n" +
//                        "?filteredType ?subClassOf ?superType \n" +
////                        "?taskStep a ?type, ?filteredType . \n" +
////                        "FILTER(strstarts(str(?type),?typePrefix)) \n" +
//                        "}", getType())
//                .setParameter("partOfWp", PART_OF_WORKPACKAGE)
//                .setParameter("hasStepPart", HAS_STEP_PART)
//                .setParameter("type", getTypeUri())
//                .setParameter("type2", FILTER_TYPE)
//                .setParameter("subClassOf", SUB_CLASS_OF)
//                .setParameter("superType", SUPER_TYPE);
////                .setParameter("typePrefix", TYPE_PREFIX);
//    }

}
