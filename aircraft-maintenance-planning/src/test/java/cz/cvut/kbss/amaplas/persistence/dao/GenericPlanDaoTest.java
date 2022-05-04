package cz.cvut.kbss.amaplas.persistence.dao;

import cz.cvut.kbss.amaplas.environment.Generator;
import cz.cvut.kbss.amaplas.exceptions.PersistenceException;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.AbstractPlan;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.TaskPlan;
import cz.cvut.kbss.jopa.exceptions.OWLPersistenceException;
import cz.cvut.kbss.jopa.model.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@Tag("dao")
class GenericPlanDaoTest extends BaseDaoTestRunner {

    @Autowired
    private EntityManager em;

    private GenericPlanDao sut;

    @BeforeEach
    void setUp() {
        this.sut = new GenericPlanDao(em);
    }

    @Test
    void findAllRetrievesAllExistingInstances() {
        final List<AbstractPlan> plans =
                IntStream.range(0, 5).mapToObj(i -> {
                    final TaskPlan u = Generator.generatePlan();
                    u.setEntityURI(Generator.generateUri());
                    return u;
                }).collect(Collectors.toList());
        transactional(() -> sut.persist(plans));
        final List<TaskPlan> result = sut.findAll(TaskPlan.class);
        assertEquals(plans.size(), result.size());
        assertTrue(plans.containsAll(result));
    }

    @Test
    void existsReturnsTrueForExistingPlan() {
        enableRdfsInference(em);
        final TaskPlan taskPlan = Generator.generatePlan();
        taskPlan.setEntityURI(Generator.generateUri());
        transactional(() -> sut.persist(taskPlan));
        assertTrue(sut.exists(taskPlan.getEntityURI()));
    }

    @Test
    void existsReturnsFalseForNonexistentPlan() {
        assertFalse(sut.exists(Generator.generateUri()));
    }

    @Test
    void findNoTypeReturnsNonEmptyOptionalAbstractPlanForExistingPlan(){
        final TaskPlan taskPlan = Generator.generatePlanWithId();
        transactional(() -> sut.persist(taskPlan));
        final Optional<AbstractPlan> result = sut.find(taskPlan.getEntityURI());
        assertTrue(result.isPresent());
        assertEquals(taskPlan, result.get());
    }


    @Test
    void findReturnsNonEmptyOptionalForExistingPlan() {
        final TaskPlan taskPlan = Generator.generatePlanWithId();
        transactional(() -> sut.persist(taskPlan));
        final Optional<TaskPlan> result = sut.find(taskPlan.getEntityURI(), TaskPlan.class);
        assertTrue(result.isPresent());
        assertEquals(taskPlan, result.get());
    }

    @Test
    void findReturnsEmptyOptionalForUnknownIdentifier() {
        final Optional<AbstractPlan> result = sut.find(Generator.generateUri());
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    void updateReturnsManagedInstance() {
        final TaskPlan taskPlan = Generator.generatePlanWithId();
        transactional(() -> sut.persist(taskPlan));
        final String lastTitleUpdate = "updatedLastTitle";
        taskPlan.setTitle(lastTitleUpdate);
        transactional(() -> {
            final TaskPlan updated = sut.update(taskPlan, TaskPlan.class);
            assertTrue(em.contains(updated));
            assertEquals(lastTitleUpdate, updated.getTitle());
        });
        assertEquals(lastTitleUpdate, em.find(TaskPlan.class, taskPlan.getEntityURI()).getTitle());
    }

    @Test
    void removeRemovesPlan() {
        final TaskPlan taskPlan = Generator.generatePlanWithId();
        transactional(() -> sut.persist(taskPlan));
        transactional(() -> sut.remove(taskPlan));
        assertFalse(sut.exists(taskPlan.getEntityURI()));
    }

    @Test
    void removeHandlesNonexistentPlan() {
        final TaskPlan taskPlan = Generator.generatePlanWithId();
        transactional(() -> sut.remove(taskPlan));
        assertFalse(sut.exists(taskPlan.getEntityURI()));
    }

    @Test
    void removeByIdRemovesEntityWithSpecifiedIdentifier() {
        final TaskPlan taskPlan = Generator.generatePlanWithId();
        transactional(() -> sut.persist(taskPlan));
        transactional(() -> sut.remove(taskPlan));
        assertFalse(sut.find(taskPlan.getEntityURI()).isPresent());
    }

    @Test
    void exceptionDuringPersistIsWrappedInPersistenceException() {
        final PersistenceException e = assertThrows(PersistenceException.class, () -> {
            final TaskPlan taskPlan = Generator.generatePlan();
            transactional(() -> sut.persist(taskPlan));
        });
        assertThat(e.getCause(), is(instanceOf(OWLPersistenceException.class)));
    }

    @Test
    void exceptionDuringCollectionPersistIsWrappedInPersistenceException() {
        final List<AbstractPlan> terms = Collections.singletonList(Generator.generatePlanWithId());
        transactional(() -> sut.persist(terms));

        final PersistenceException e = assertThrows(PersistenceException.class,
                () -> transactional(() -> sut.persist(terms)));
        assertThat(e.getCause(), is(instanceOf(OWLPersistenceException.class)));
    }

    @Test
    void exceptionDuringUpdateIsWrappedInPersistenceException() {
        final TaskPlan taskPlan = Generator.generatePlan();
        final PersistenceException e = assertThrows(PersistenceException.class,
                () -> transactional(() -> sut.update(taskPlan)));
        assertThat(e.getCause(), is(instanceOf(OWLPersistenceException.class)));
    }

    @Test
    void getReferenceRetrievesReferenceToMatchingInstance() {
        final TaskPlan taskPlan = Generator.generatePlanWithId();
        transactional(() -> sut.persist(taskPlan));
        final Optional<TaskPlan> result = sut.getReference(taskPlan.getEntityURI(), TaskPlan.class);
        assertTrue(result.isPresent());
        assertEquals(taskPlan.getEntityURI(), result.get().getEntityURI());
    }

    @Test
    void getReferenceReturnsEmptyOptionalWhenNoMatchingInstanceExists() {
        final Optional<TaskPlan> result = sut.getReference(Generator.generateUri(), TaskPlan.class);
        assertNotNull(result);
        assertFalse(result.isPresent());
    }


}
