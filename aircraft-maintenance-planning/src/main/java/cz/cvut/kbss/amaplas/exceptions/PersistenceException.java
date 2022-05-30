package cz.cvut.kbss.amaplas.exceptions;

/**
 * Marks an exception that occurred in the persistence layer.
 */
public class PersistenceException extends ApplicationException {

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(Throwable cause) {
        super(cause);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
