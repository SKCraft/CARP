package com.skcraft.cardinal.api;

import lombok.ToString;

import java.util.List;

@ToString(of = "errors")
public class ServiceError extends Exception {
    private final List<String> errors;

    ServiceError(List<String> errors) {
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
