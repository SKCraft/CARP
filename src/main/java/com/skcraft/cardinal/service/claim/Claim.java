package com.skcraft.cardinal.service.claim;

import com.skcraft.cardinal.util.WorldVector3i;
import com.skcraft.cardinal.profile.MojangId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents a chunk that can be claimed by a user.
 */
@Data
@EqualsAndHashCode(of = {"server", "position"})
public class Claim {
    public static final String SERVER_OWNER_NAME = "~system";
    public static final UUID SERVER_OWNER_UUID = UUID.fromString("99a9d31c-635e-4f18-b1b5-c61b3d2e00a7");

    private final String server;
    private final WorldVector3i position;
    private MojangId owner;
    private String party;
    private Date issueTime;

    public Claim(String server, WorldVector3i position) {
        checkNotNull(server, "server");
        checkNotNull(position, "position");
        this.server = server;
        this.position = position;
    }
}
