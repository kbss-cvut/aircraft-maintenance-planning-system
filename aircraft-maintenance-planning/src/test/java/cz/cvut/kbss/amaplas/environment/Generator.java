package cz.cvut.kbss.amaplas.environment;

import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.*;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Generator {

    private static final Random random = new Random();

    private Generator() {
        throw new AssertionError();
    }

    /**
     * Generates a (pseudo) random URI, usable for test individuals.
     *
     * @return Random URI
     */
    public static URI generateUri() {
        return URI.create(Environment.BASE_URI + "/randomInstance" + randomInt());
    }

    /**
     * Generates a (pseudo-)random integer between the specified lower and upper bounds.
     *
     * @param lowerBound Lower bound, inclusive
     * @param upperBound Upper bound, exclusive
     * @return Randomly generated integer
     */
    public static int randomInt(int lowerBound, int upperBound) {
        int rand;
        do {
            rand = random.nextInt(upperBound);
        } while (rand < lowerBound);
        return rand;
    }

    /**
     * Generates a (pseudo) random integer.
     * <p>
     * This version has no bounds (aside from the integer range), so the returned number may be
     * negative or zero.
     *
     * @return Randomly generated integer
     * @see #randomInt(int, int)
     */
    public static int randomInt() {
        return random.nextInt();
    }

    /**
     * Generates a (pseudo)random index of an element in the collection.
     * <p>
     * I.e. the returned number is in the interval <0, col.size()).
     *
     * @param col The collection
     * @return Random index
     */
    public static int randomIndex(Collection<?> col) {
        assert col != null;
        assert !col.isEmpty();
        return random.nextInt(col.size());
    }

    /**
     * Generates a (pseudo)random index of an element in the array.
     * <p>
     * I.e. the returned number is in the interval <0, arr.length).
     *
     * @param arr The array
     * @return Random index
     */
    public static int randomIndex(Object[] arr) {
        assert arr != null;
        assert arr.length > 0;
        return random.nextInt(arr.length);
    }

    /**
     * Generators a (pseudo) random boolean.
     *
     * @return Random boolean
     */
    public static boolean randomBoolean() {
        return random.nextBoolean();
    }

    public static TaskPlan generatePlan() {
        return generatePlan(TaskPlan.class);
    }

    public static TaskPlan generatePlanWithId() {
        return generatePlanWithId(TaskPlan.class);
    }

    public static <T extends AbstractPlan> T generatePlan(Class<T> planClass) {
        final T plan = generateEntity(planClass);
        plan.setTitle("Plan-" + randomInt());
        return plan;
    }

    public static <T extends AbstractPlan> T generatePlanWithId(Class<T> planClass) {
        final T plan = generatePlan(planClass);
        plan.setEntityURI(Generator.generateUri());
        return plan;
    }

    public static List<TaskPlan> generatePlansWithIds(int count) {
        return generatePlansWithIds(TaskPlan.class, count);
    }

    public static <T extends AbstractPlan> List<T> generatePlansWithIds(Class<T> planClass, int count) {
        return IntStream.range(0, count).mapToObj(i -> generatePlanWithId(planClass))
                .collect(Collectors.toList());
    }

    public static AircraftArea generateResource() {
        return generateResource(AircraftArea.class);
    }

    public static AircraftArea generateResourceWithId() {
        return generateResourceWithId(AircraftArea.class);
    }

    public static <T extends Resource> T generateResource(Class<T> resourcesClass) {
        final T resource = generateEntity(resourcesClass);
        resource.setTitle(resourcesClass.getSimpleName() + "-" + randomInt());
        return resource;
    }

    public static <T extends Resource> T generateResourceWithId(Class<T> resourcesClass) {
        final T resource = generateResource(resourcesClass);
        resource.setEntityURI(Generator.generateUri());
        return resource;
    }


    protected static <T extends AbstractEntity> T generateEntity(Class<T> entityClass) {
        try {
            return entityClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
