package com.skcraft.cardinal.profile;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=As.PROPERTY, property="domain", defaultImpl=UnknownId.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = MojangId.class, name = "mojang"),
        @JsonSubTypes.Type(value = ConsoleUser.class, name = "console"),
})
public interface ProfileId {
    String getDomain();
}
