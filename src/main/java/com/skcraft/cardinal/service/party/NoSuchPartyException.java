package com.skcraft.cardinal.service.party;

public class NoSuchPartyException extends Exception {

    public NoSuchPartyException() {
    }

    public NoSuchPartyException(String message) {
        super(message);
    }

    public NoSuchPartyException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchPartyException(Throwable cause) {
        super(cause);
    }

}
