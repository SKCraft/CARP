package com.skcraft.cardinal.service.claim;

public class ClaimAttemptException extends Exception {

    public ClaimAttemptException(String message) {
        super(message);
    }

    public ClaimAttemptException(String message, Throwable cause) {
        super(message, cause);
    }

}
