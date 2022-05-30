package cz.cvut.kbss.amaplas.model;

import java.util.List;

/**
 * Used to represent combinations of resources, e.g. specific Maintenance group works on specific aircraft area.
 */
// NOT USED
public class ResourceCombination extends Resource {
    protected List<Resource> resources;

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }
}
