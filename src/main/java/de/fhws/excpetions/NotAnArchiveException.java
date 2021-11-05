package de.fhws.excpetions;

public class NotAnArchiveException extends Exception {
    public NotAnArchiveException() {
    }

    public NotAnArchiveException(String message) {
        super(message);
    }

    public NotAnArchiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotAnArchiveException(Throwable cause) {
        super(cause);
    }

    public NotAnArchiveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
