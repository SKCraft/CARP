package com.skcraft.cardinal.service.hive;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.skcraft.cardinal.service.hive.permission.Group;
import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HiveData {
    private ConcurrentMap<String, Group> groups = new ConcurrentHashMap<>();
}
