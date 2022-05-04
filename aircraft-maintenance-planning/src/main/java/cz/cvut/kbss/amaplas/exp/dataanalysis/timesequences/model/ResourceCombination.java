package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Used to represent combinations of resources, e.g. specific Maintenance group works on specific aircraft area.
 */
@Getter
@Setter
// NOT USED
public class ResourceCombination extends Resource {
    protected List<Resource> resources;

}
