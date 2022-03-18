package cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AbstractEntity {
    protected String type = this.getClass().getSimpleName();

    protected Long id;
    protected String title;
}
