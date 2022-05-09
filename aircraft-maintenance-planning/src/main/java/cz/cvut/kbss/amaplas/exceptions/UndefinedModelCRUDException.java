package cz.cvut.kbss.amaplas.exceptions;

public class UndefinedModelCRUDException extends ApplicationException{
    public UndefinedModelCRUDException() {
    }

    public UndefinedModelCRUDException(String message) {
        super(message);
    }

    public UndefinedModelCRUDException(String message, Throwable cause) {
        super(message, cause);
    }

    public UndefinedModelCRUDException(Throwable cause) {
        super(cause);
    }
}
