package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.io.EntityRegistry;
import cz.cvut.kbss.amaplas.model.*;
import cz.cvut.kbss.amaplas.model.values.DateParserSerializer;
import cz.cvut.kbss.amaplas.persistence.dao.mapper.Bindings;
import cz.cvut.kbss.amaplas.persistence.dao.mapper.QueryResultMapper;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.query.BindingSet;
import org.springframework.stereotype.Repository;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Repository
public class WorkSessionDao extends BaseDao<WorkSession> {

    public static final String WP_WORK_SESSIONS = "/queries/analysis/work-sessions.sparql";


    protected QueryResultMapper<Pair<URI, WorkSession>> workSessions = new QueryResultMapper<>(WP_WORK_SESSIONS) {
        protected Pattern mechanicIRI_IDPattern = Pattern.compile("^.+/mechanic--(.+)$");
        protected EntityRegistry registry;


        @Override
        public List<Pair<URI, WorkSession> > convert(Iterable<BindingSet> bindingSets) {
            registry = new EntityRegistry();
            return super.convert(bindingSets);
        }
        @Override
        public Pair<URI, WorkSession> convert() {
            String def = "unknown";
            // create mechanic
            String mechanicIRI = optValue( "w", null);
            String mechanicID = optValue( "wId", null);
            String mechanicLabel = optValue( "wLabel", null);
            Mechanic mechanic = null;
            if(mechanicIRI != null){
                mechanic = registry.getOrCreate(mechanicIRI, Mechanic::new, Mechanic.class);
                mechanic.setEntityURI(URI.create(mechanicIRI));
                if(mechanicID == null){
                    Matcher m = mechanicIRI_IDPattern.matcher(mechanicIRI);
                    if(m.matches()){
                        mechanicID = m.group(1);
                    }
                }
                mechanic.setId(mechanicID);
                mechanic.setTitle(mechanicLabel != null ? mechanicLabel : mechanicID);
            }

            // create a work session record
            String sessionURI = optValue("t", null);

            WorkSession workSession = sessionURI == null ? new WorkSession() : registry.getOrCreate(sessionURI, WorkSession::new, WorkSession.class);
            workSession.setEntityURI(URI.create(sessionURI));
//            workSession.setTaskExecution(taskExecution);
            workSession.setMechanic(mechanic);

            String scopeAbbreviation = optValue("scope", def);
            MaintenanceGroup scope = scopeAbbreviation == null ? null : registry.getOrCreate(scopeAbbreviation, MaintenanceGroup::new, MaintenanceGroup.class);
            if(scope != null) {
                scope.setEntityURI(optValue("scopeGroup", URI::create, null));
                scope.setAbbreviation(scopeAbbreviation);
                scope.setTitle(optValue("scopeLabel", null));

            }

            workSession.setScope(scope);

            String start = optValue( "start", null);
            String end = optValue( "end", null);
            if(start != null )
                workSession.setStart(DateParserSerializer.parseDate(DateParserSerializer.df.get(), start));
            if(end != null)
                workSession.setEnd(DateParserSerializer.parseDate(DateParserSerializer.df.get(), end));

            workSession.setDur(optValueNonString("dur", v -> ((Literal)v).longValue(), null));
            return Pair.of(manValue("tt", URI::create), workSession);
        }
    };

    public WorkSessionDao(EntityManager em, Rdf4JDao rdf4JDao) {
        super(WorkSession.class, em, rdf4JDao);
    }

    /**
     * The input workpackage should have its taskExecution field set
     * @param wp
     */
    public void loadWorkSessions(Workpackage wp){
        Bindings bindings = new Bindings();
        bindings.add("wp", wp.getEntityURI());
        List<Pair<URI, WorkSession>> workSessionsByTask = load(workSessions, bindings);

        Map<URI, TaskExecution> taskExecutionMap = new HashMap<>();
        wp.getTaskExecutions().forEach(t -> taskExecutionMap.put(t.getEntityURI(), t));

        workSessionsByTask.stream().collect(Collectors.groupingBy(
                p -> p.getKey(),
                Collectors.mapping(p -> p.getValue(), Collectors.toSet())
        )).entrySet().forEach(e -> Optional.ofNullable(taskExecutionMap.get(e.getKey()))
                .ifPresent(t -> {
                    t.setWorkSessions(e.getValue());
                    e.getValue().forEach(ws -> ws.setTaskExecution(t));
                }));
    }


}
