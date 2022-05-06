package cz.cvut.kbss.amaplas.exceptions;

/**
 * Indicates that invalid data have been passed to the application.
 * <p>
 * The exception message should provide information as to what data are invalid and why.
 */
public class ValidationException extends ApplicationException {


    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }


    public ValidationException() {
    }
}
