package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.exceptions.PersistenceException;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class PlanTypeDao {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    protected static Set<String> supportedPlanTypes;

    protected static Map<String, Class<? extends AbstractPlan>> entityClassMap;

    @PostConstruct
    protected void postConstructor(){
        entityClassMap = new HashMap<>();
        Stream.of(
                        SessionPlan.class,
                        TaskPlan.class,
                        GeneralTaskPlan.class,
                        PhasePlan.class,
                        RevisionPlan.class)
                .forEach(c -> entityClassMap.put(EntityToOwlClassMapper.getOwlClassForEntity(c), c));

        // Declare plan types supported by the application
        supportedPlanTypes = new HashSet<>(entityClassMap.keySet());

    }

    public boolean isSupportedPlanType(URI planType){
        return supportedPlanTypes.contains(planType);
    }


    public boolean containsSupportedPlanType(AbstractPlan plan){
        return containsSupportedPlanType(getTypes(plan));
    }

    public boolean containsSupportedPlanType(Set<String> planTypes){
        return planTypes.stream().filter(supportedPlanTypes::contains).findFirst().isPresent();
    }

    public AbstractPlan getNewPlanTypeInstance(String planType){
        Class<? extends AbstractPlan> planClass = entityClassMap.get(planType);

        try {
            return planClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException | NullPointerException e) {
            throw new PersistenceException(
                    String.format("Could not create a plan instance for type <%s> using java reflections.", planType),
                    e
            );
        }
    }

    public AbstractPlan getNewPlanTypeInstance(URI planType){
        return getNewPlanTypeInstance(planType.toString());
    }

    public URI getType(AbstractEntity e){
        Class cls = e.getClass();
        while(cls != null) {
            String uri = EntityToOwlClassMapper.getOwlClassForEntity(cls);
            if(uri != null)
                return URI.create(uri);
            cls = cls.getSuperclass();
        }
        return null;
    }

    public Set<String> getTypes(AbstractEntity e){
        Set<String> types = new HashSet<>();
        Class cls = e.getClass();

        while(cls != null) {
            String uri = EntityToOwlClassMapper.getOwlClassForEntity(cls);
            if(uri != null) {
                types.add(uri);
            }
            cls = cls.getSuperclass();
        }
        return types;
    }
}
