package com.skcraft.cardinal;

public class CardinalFatalException extends RuntimeException {

    public CardinalFatalException() {
    }

    public CardinalFatalException(String message) {
        super(message);
    }

    public CardinalFatalException(String message, Throwable cause) {
        super(message, cause);
    }

    public CardinalFatalException(Throwable cause) {
        super(cause);
    }

}
