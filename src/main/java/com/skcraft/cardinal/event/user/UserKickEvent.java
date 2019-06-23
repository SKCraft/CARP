package com.skcraft.cardinal.event.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.skcraft.cardinal.profile.ProfileId;
import lombok.Data;

import java.util.List;

@Data
public class UserKickEvent {
    private final List<ProfileId> profiles;

    @JsonCreator
    public UserKickEvent(@JsonProperty("profiles") List<ProfileId> profiles) {
        this.profiles = profiles;
    }
}
