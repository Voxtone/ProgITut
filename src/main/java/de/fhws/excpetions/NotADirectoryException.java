package de.fhws.excpetions;

public class NotADirectoryException extends Exception {
    public NotADirectoryException() {
    }

    public NotADirectoryException(String message) {
        super(message);
    }

    public NotADirectoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotADirectoryException(Throwable cause) {
        super(cause);
    }

    public NotADirectoryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
