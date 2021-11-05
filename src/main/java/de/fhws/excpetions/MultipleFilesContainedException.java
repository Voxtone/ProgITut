package de.fhws.excpetions;

public class MultipleFilesContainedException extends Exception {
    public MultipleFilesContainedException() {
    }

    public MultipleFilesContainedException(String message) {
        super(message);
    }

    public MultipleFilesContainedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipleFilesContainedException(Throwable cause) {
        super(cause);
    }

    public MultipleFilesContainedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
