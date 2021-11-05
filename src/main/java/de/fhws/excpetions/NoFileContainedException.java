package de.fhws.excpetions;

public class NoFileContainedException extends Exception {
    public NoFileContainedException() {
    }

    public NoFileContainedException(String message) {
        super(message);
    }

    public NoFileContainedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoFileContainedException(Throwable cause) {
        super(cause);
    }

    public NoFileContainedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
