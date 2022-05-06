package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.controller.dto.EntityReferenceDTO;
import cz.cvut.kbss.amaplas.controller.dto.RelationDTO;
import cz.cvut.kbss.amaplas.environment.Generator;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.AbstractPlan;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.SessionPlan;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskPlan;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.ops.CopySimplePlanProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

class AircraftRevisionPlannerServiceTest extends BaseServiceTestRunner{

    @Autowired
    private AircraftRevisionPlannerService sut;

    @Test
    void createdPlanCanBeRetrievedViaGetPlan(){
        TaskPlan plan = Generator.generatePlanWithId(TaskPlan.class);
        sut.createPlan(plan);
        AbstractPlan persistedPlan = sut.getPlan(new EntityReferenceDTO(plan.getEntityURI()));
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
        sut.addPlanPart(RelationDTO.asDTO(plan, splan1));
        sut.addPlanPart(RelationDTO.asDTO(plan, splan2));

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

        sut.deletePlanPart(new RelationDTO(plan.getEntityURI(), splan2.getEntityURI()));

        TaskPlan retrievedPlan = (TaskPlan)sut.getPlan(plan.getEntityURI());

        assertNotNull(retrievedPlan.getPlanParts());
        assertEquals(1, retrievedPlan.getPlanParts().size());
        assertTrue(retrievedPlan.getPlanParts().contains(splan1));
        assertFalse(retrievedPlan.getPlanParts().contains(splan2));

    }

}
