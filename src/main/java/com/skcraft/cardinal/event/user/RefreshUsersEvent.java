package com.skcraft.cardinal.event.user;

import com.skcraft.cardinal.service.hive.Hive;
import lombok.Data;

@Data
public class RefreshUsersEvent {
    private final Hive hive;
}
