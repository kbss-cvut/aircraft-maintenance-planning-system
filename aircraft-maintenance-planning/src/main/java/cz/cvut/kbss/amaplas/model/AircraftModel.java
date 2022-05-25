package cz.cvut.kbss.amaplas.model;

public class AircraftModel {
    public String id;
    public String code;
    public String label;
    public AircraftType type;

    public AircraftModel() {
    }

    public AircraftModel(String id, String code, String label, AircraftType type) {
        this.id = id;
        this.code = code;
        this.label = label;
        this.type = type;
    }
}
