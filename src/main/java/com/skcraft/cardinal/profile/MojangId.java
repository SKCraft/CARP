package com.skcraft.cardinal.profile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

import static com.google.common.base.Preconditions.*;

@Data
@EqualsAndHashCode(of = "uuid")
public class MojangId implements ProfileId {

    private final UUID uuid;
    private String name;

    @JsonCreator
    public MojangId(@JsonProperty("uuid") UUID uuid, @JsonProperty("name") String name) {
        checkNotNull(uuid, "uuid");
        checkNotNull(name, "name");
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public String getDomain() {
        return "mojang";
    }
}
