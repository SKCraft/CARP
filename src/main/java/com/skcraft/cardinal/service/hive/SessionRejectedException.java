package com.skcraft.cardinal.service.hive;

public class SessionRejectedException extends ErrorResponse {
    public SessionRejectedException(String message) {
        super(message);
    }
}
