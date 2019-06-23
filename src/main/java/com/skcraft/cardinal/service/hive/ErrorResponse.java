package com.skcraft.cardinal.service.hive;

public class ErrorResponse extends Exception {
    public ErrorResponse(String message) {
        super(message);
    }

    public ErrorResponse(String message, Throwable cause) {
        super(message, cause);
    }
}
