package com.skcraft.cardinal.profile;

import java.util.Collection;
import java.util.Map;

/**
 * Maps user IDs to a row ID.
 */
public interface MojangIdMapping {
    int get(MojangId mojangId);

    Map<MojangId, Integer> getAll(Collection<MojangId> mojangIds);
}
