package cz.cvut.kbss.amaplas.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Scope {

    public static final Set<String> PRIORITY_SCOPE = new HashSet<>(Arrays.asList(
            "AVIO",
            "MECH-LDG",
            "MECH-INT",
            "MECH-EXT",
            "MECH-ENG",
            "SHM"
    )) ;

    public static boolean isPriority(String scope){
        return PRIORITY_SCOPE.contains(scope);
    }
}
