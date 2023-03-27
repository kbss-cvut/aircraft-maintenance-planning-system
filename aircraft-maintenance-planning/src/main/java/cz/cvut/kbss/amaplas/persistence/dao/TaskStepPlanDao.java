package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.io.SparqlDataReader;
import cz.cvut.kbss.amaplas.io.SparqlDataReaderRDF4J;
import cz.cvut.kbss.amaplas.model.AbstractEntity;
import cz.cvut.kbss.amaplas.model.TaskStepPlan;
import cz.cvut.kbss.amaplas.util.Vocabulary;
import cz.cvut.kbss.amaplas.utils.ResourceUtils;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.model.query.TypedQuery;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class TaskStepPlanDao extends BaseDao<TaskStepPlan> {

    protected static final URI ID = URI.create(Vocabulary.s_p_id);
    protected static final URI PART_OF_WORKPACKAGE = URI.create(Vocabulary.s_p_is_part_of_workpackage);
    protected static final URI HAS_STEP_PART = URI.create(Vocabulary.s_p_has_work_order_step);
    protected static final URI SUB_CLASS_OF = URI.create(RDFS.SUB_CLASS_OF);
    protected static final URI SUPER_TYPE = URI.create(Vocabulary.s_c_work_order_step);
    protected static final String TYPE_PREFIX = Vocabulary.ONTOLOGY_IRI_csat_maintenance + "/task-step-execution-type--";
    protected static final URI FILTER_TYPE = URI.create("http://www.w3.org/ns/csvw#Row");

    public TaskStepPlanDao(EntityManager em) {
        super(TaskStepPlan.class, em);
    }


    @Override
    protected <E extends AbstractEntity> TypedQuery<E> selectAll(Class<E> type, URI typeUri){
        return getEm().createNativeQuery("SELECT DISTINCT ?x WHERE { ?x a ?type . FILTER(strstarts(str(?type),?typePrefix)) }", type)
                .setParameter("type", FILTER_TYPE)
                .setParameter("typePrefix", TYPE_PREFIX);
    }

    public List<TaskStepPlan> listInWorkpackageId(String wpId){
        return getFindQuery("?wp ?id ?wpId .")
                .setParameter("wpId", wpId)
                .getResultList();
    }

    public List<TaskStepPlan> listInWorkpackageURI(URI workpackageURI){
        Map<String, Value> initialBindings = new HashMap<>();
        initialBindings.put("wp", SimpleValueFactory.getInstance().createIRI(workpackageURI.toString()));
        return SparqlDataReaderRDF4J.readData(SparqlDataReader.TASK_STEP_AND_ANNOTATIONS, getConnection(),
                initialBindings,
                SparqlDataReaderRDF4J::convertToTaskStepPlan
        );
    }
    public List<TaskStepPlan> listURIsInWorkpackageURI(URI workpackageURI){
        return getFindQuery()
                .setParameter("wp", workpackageURI)
                .getResultList();
    }

    protected List<BindingSet> load(Map<String, Value> bindings){
        String queryString = ResourceUtils.loadResource(SparqlDataReader.TASK_STEP_AND_ANNOTATIONS);
        TypedQuery<BindingSet> query = getEm().createNativeQuery(queryString, BindingSet.class);
        return query.getResultList();
    }
    protected TypedQuery<TaskStepPlan> getFindQuery(String ... triples){
        return getEm().createNativeQuery("SELECT DISTINCT ?taskStep ?task WHERE { \n" +
                        Arrays.stream(triples).collect(Collectors.joining("/n")) +
                        "?task ?partOfWp ?wp . \n" +
                        "?task ?hasStepPart ?taskStep . \n" +
                        "?taskStep a ?type, ?filteredType, ?type2 . \n" +
                        "?filteredType ?subClassOf ?superType \n" +
//                        "?taskStep a ?type, ?filteredType . \n" +
//                        "FILTER(strstarts(str(?type),?typePrefix)) \n" +
                        "}", getType())
                .setParameter("partOfWp", PART_OF_WORKPACKAGE)
                .setParameter("hasStepPart", HAS_STEP_PART)
                .setParameter("type", getTypeUri())
                .setParameter("type2", FILTER_TYPE)
                .setParameter("subClassOf", SUB_CLASS_OF)
                .setParameter("superType", SUPER_TYPE);
//                .setParameter("typePrefix", TYPE_PREFIX);
    }

}
