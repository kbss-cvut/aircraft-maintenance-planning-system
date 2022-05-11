package cz.cvut.kbss.amaplas.services;

import cz.cvut.kbss.amaplas.exceptions.ValidationException;
import cz.cvut.kbss.amaplas.exp.dataanalysis.timesequences.model.AbstractEntity;

public class BaseService {
    void verifyEntityHasId(AbstractEntity e) {
        if(e.getEntityURI() == null)
            throw new ValidationException(String.format(
                    "Invalid input entity. The entity should have entityURI value. \n%s",
                    e
            )
            );
    }

    void verifyEntityNotNull(AbstractEntity e) {
        if(e == null)
            throw new ValidationException(String.format("Invalid input. Input entity is null"));
    }
}
