package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.environment.Generator;
import cz.cvut.kbss.amaplas.exceptions.NotFoundException;
import cz.cvut.kbss.amaplas.exceptions.UnsupportedOperationException;
import cz.cvut.kbss.amaplas.exceptions.ValidationException;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.AbstractPlan;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.SessionPlan;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskPlan;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.ops.CopySimplePlanProperties;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class AircraftRevisionPlannerServiceTest extends BaseServiceTestRunner{

    @Autowired
    private AircraftRevisionPlannerService sut;

    @Test
    void createdPlanCanBeRetrievedViaGetPlan(){
        TaskPlan plan = Generator.generatePlanWithId(TaskPlan.class);
        sut.createPlan(plan);
        AbstractPlan persistedPlan = sut.getPlan(plan.getEntityURI());
        assertEquals(plan, persistedPlan);
    }

    @Test
    void updatePlanSimplePropertiesChangesOnlyBasicPropertiesNotRelations(){
        TaskPlan plan = Generator.generatePlan(TaskPlan.class);
        SessionPlan splan = Generator.generatePlan(SessionPlan.class);
        plan.setTitle("title1");
        plan.setDuration(100L);

        sut.createPlan(splan);
        sut.createPlan(plan);
        sut.addPlanPart(plan, splan);

        TaskPlan updatedPlan = Generator.generatePlan(TaskPlan.class);
        updatedPlan.setEntityURI(plan.getEntityURI());

        Date updatedStart = Calendar.getInstance().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 3);
        Date updatedEnd = calendar.getTime();
        Long updatedDuration = updatedEnd.getTime() - updatedStart.getTime();
        CopySimplePlanProperties copyOp = new CopySimplePlanProperties();
        updatedPlan.setTitle(plan.getTitle());
        updatedPlan.setStartTime(updatedStart);
        updatedPlan.setEndTime(updatedEnd);
        updatedPlan.setDuration(updatedDuration);

        sut.updatePlanSimpleProperties(updatedPlan);

        TaskPlan retrievedPlan = (TaskPlan)sut.getPlan(updatedPlan.getEntityURI());

        assertNotNull(retrievedPlan.getPlanParts());
        assertEquals(1, retrievedPlan.getPlanParts().size());
        assertEquals(splan, retrievedPlan.getPlanParts().iterator().next());
        assertEquals(retrievedPlan.getTitle(), plan.getTitle());
        assertEquals(retrievedPlan.getTitle(), updatedPlan.getTitle() );
        assertEquals(retrievedPlan.getStartTime(), updatedPlan.getStartTime());
        assertEquals(retrievedPlan.getEndTime(), updatedPlan.getEndTime());
        assertEquals(retrievedPlan.getDuration(), updatedPlan.getDuration());
    }

    @Test
    void addPlanPartAddsPlanTo(){
        TaskPlan plan = Generator.generatePlan(TaskPlan.class);
        SessionPlan splan1 = Generator.generatePlan(SessionPlan.class);
        SessionPlan splan2 = Generator.generatePlan(SessionPlan.class);

        sut.createPlan(splan1);
        sut.createPlan(splan2);
        sut.createPlan(plan);
        sut.addPlanPart(plan, splan1);
        sut.addPlanPart(plan, splan2);

        TaskPlan retrievedPlan = (TaskPlan)sut.getPlan(plan.getEntityURI());
        assertNotNull(retrievedPlan.getPlanParts());
        assertEquals(2, retrievedPlan.getPlanParts().size());
        assertTrue(retrievedPlan.getPlanParts().contains(splan1));
        assertTrue(retrievedPlan.getPlanParts().contains(splan2));
    }

    @Test
    void deletePlanPart(){
        TaskPlan plan = Generator.generatePlan(TaskPlan.class);
        SessionPlan splan1 = Generator.generatePlan(SessionPlan.class);
        SessionPlan splan2 = Generator.generatePlan(SessionPlan.class);

        sut.createPlan(splan1);
        sut.createPlan(splan2);
        sut.createPlan(plan);
        sut.addPlanPart(plan, splan1);
        sut.addPlanPart(plan, splan2);

        sut.deletePlanPart(plan.getEntityURI(), splan2.getEntityURI());

        TaskPlan retrievedPlan = (TaskPlan)sut.getPlan(plan.getEntityURI());

        assertNotNull(retrievedPlan.getPlanParts());
        assertEquals(1, retrievedPlan.getPlanParts().size());
        assertTrue(retrievedPlan.getPlanParts().contains(splan1));
        assertFalse(retrievedPlan.getPlanParts().contains(splan2));

    }

    @Disabled
    @Test
    void getPlanPartsReturnsPersistedPlanParts(){
        TaskPlan plan = Generator.generatePlan(TaskPlan.class);
        SessionPlan splan1 = Generator.generatePlan(SessionPlan.class);
        SessionPlan splan2 = Generator.generatePlan(SessionPlan.class);
        sut.createPlan(splan1);
        sut.createPlan(splan2);
        sut.createPlan(plan);
        sut.addPlanPart(plan, splan1);
        sut.addPlanPart(plan, splan2);

        Collection<AbstractPlan> planParts = sut.getPlanParts(plan.getEntityURI());
        assertEquals(2, planParts.size());
        assertTrue(planParts.contains(splan1));
        assertTrue(planParts.contains(splan2));
    }

    @Test
    void getPlanPartsOnNonComplexPlanThrowsUndefinedModelCRUDException(){
        final SessionPlan plan = Generator.generatePlan(SessionPlan.class);
        sut.createPlan(plan);
        final UnsupportedOperationException e = assertThrows(UnsupportedOperationException.class,
                () -> sut.getPlanParts(plan.getEntityURI()));
    }

    @Test
    void getPlanThrowsNotFoundExceptionIfThereIsNoPlanWithGivenUri(){
        URI uri = Generator.generateUri();
        assertThrows(NotFoundException.class, () -> sut.getPlan(uri));
    }

    @Test
    void updatePlanThrowsValidationExceptionIfTheUpdatePlanHasNoUri(){
        TaskPlan plan = Generator.generatePlan(TaskPlan.class);
        plan.setTitle("plan title");
        plan.setStartTime(new Date());
        sut.createPlan(plan);
        // prepare update
        plan.setEntityURI(null);
        plan.setTitle("plan title change");

        // update
        assertThrows( ValidationException.class, () -> sut.updatePlanSimpleProperties(plan));
    }

    @Test
    void updatePlanThrowsNotFoundExceptionIfThereIsNoPlanWithGivenUri(){
        TaskPlan plan = Generator.generatePlanWithId(TaskPlan.class);
        plan.setTitle("plan title");
        plan.setStartTime(new Date());
        assertThrows( NotFoundException.class, () -> sut.updatePlanSimpleProperties(plan));
    }

}
