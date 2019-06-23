package com.skcraft.cardinal.service.remotecommand;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandSpec {
    private String name;
    private Set<String> aliases = new HashSet<>();
    private String description;
    private Set<String> require = new HashSet<>();
}
