package com.skcraft.cardinal.service.remotecommand;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandData {
    private Set<CommandSpec> commands = new HashSet<>();
}
