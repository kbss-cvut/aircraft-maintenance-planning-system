package cz.cvut.kbss.amaplas.persistence.dao;

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

    public boolean isSupportedPlanType(Set<URI> planTypes){
        return planTypes.stream().filter(supportedPlanTypes::contains).findFirst().isPresent();
    }

    public AbstractPlan getNewPlanTypeInstance(String planType){
        Class<? extends AbstractPlan> planClass = entityClassMap.get(planType);
        if(planClass == null)
            return null;

        try {
            return planClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException e) {
            log.error("", e);
        } catch (IllegalAccessException e) {
            log.error("", e);
        } catch (InvocationTargetException e) {
            log.error("", e);
        } catch (NoSuchMethodException e) {
            log.error("", e);
        }
        return null;
    }

    public AbstractPlan getNewPlanTypeInstance(URI planType){
        return getNewPlanTypeInstance(planType.toString());
    }
}
