package de.fhws.excpetions;

public class InvalidArchiveException extends Exception {
    public InvalidArchiveException() {
    }

    public InvalidArchiveException(String message) {
        super(message);
    }

    public InvalidArchiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidArchiveException(Throwable cause) {
        super(cause);
    }

    public InvalidArchiveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
