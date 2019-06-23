package com.skcraft.cardinal.profile;

public class UnknownId implements ProfileId {
    @Override
    public String getDomain() {
        return "unknown";
    }
}
