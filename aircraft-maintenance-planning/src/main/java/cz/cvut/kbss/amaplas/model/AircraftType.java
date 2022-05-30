package cz.cvut.kbss.amaplas.model;

import java.util.*;
import java.util.stream.Stream;

public class AircraftType {
    protected String type;
    protected Set<String> models;

    public AircraftType(String type, Set<String> models) {
        this.type = type;
        this.models = models;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<String> getModels() {
        return models;
    }

    public void setModels(Set<String> models) {
        this.models = models;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AircraftType)) return false;
        AircraftType that = (AircraftType) o;
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    public static Map<String, AircraftType> types;
    public static Map<String, AircraftType> modelMap;

    static {
        initAircraftTypes();
    }

    public static String getTypeLabelForModel(String model){
        return Optional.ofNullable(modelMap.get(model)).map(at -> at.getType()).orElse(null);
    }


    public static void initAircraftTypes(){
        Map<String, AircraftType> types = new HashMap<>();
        Stream.of(
            new AircraftType("A3", new HashSet<>(Arrays.asList("319","320", "321","A319","A320", "A333", "32B"))),
            new AircraftType("B", new HashSet<>(Arrays.asList("738", "73G", "73H", "73J", "73W", "B735"))),
//            new AircraftType("A3", new HashSet<>(Arrays.asList("A319","A320", "A333", "32B"))),
//            new AircraftType("ATR", new HashSet<>(Arrays.asList("ATR-42", "ATR-72"))),
            new AircraftType("ATR", new HashSet<>(Arrays.asList("ATR-42", "ATR-72")))

        ).forEach(at -> types.put(at.type, at));
        AircraftType.types = types;
        modelMap = new HashMap<>();
        types.values().forEach(at -> at.models.forEach(m -> modelMap.put(m, at)));
        System.out.println("");
    }
}
