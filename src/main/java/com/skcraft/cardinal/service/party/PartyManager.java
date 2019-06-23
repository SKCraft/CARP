package com.skcraft.cardinal.service.party;

import com.google.common.collect.Lists;
import com.skcraft.cardinal.profile.MojangId;
import com.skcraft.cardinal.util.DataAccessException;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Manages groups of players called "parties."
 */
public interface PartyManager {
    /**
     * Get a party with the given ID.
     *
     * @param name The party ID
     * @return The party, or null if it doesn't exist
     * @throws DataAccessException Thrown if data can't be accessed
     */
    @Nullable
    default Party get(String name) {
        return getAll(Lists.newArrayList(name)).get(name.toLowerCase());
    }

    /**
     * Get a list of parties with the given IDs.
     * <p>
     * <p>If there is no party for the given ID, then its value will be
     * {@code null} in the returned map.</p>
     *
     * @param names The list of party IDs
     * @return A map of parties where the key responds to the party's ID in lower case
     * @throws DataAccessException Thrown if data can't be accessed
     */
    Map<String, Party> getAll(Collection<String> names);

    /**
     * Refresh the party and re-load its data.
     *
     * @param party The party
     * @return True if the party was removed
     */
    default boolean refresh(Party party) {
        return refreshAll(Lists.newArrayList(party)).contains(party.getName().toLowerCase());
    }

    /**
     * Refresh a collection of parties.
     *
     * @param parties Collection of parties to fresh
     * @return A list of lowercase names of parties that have been removed
     */
    Set<String> refreshAll(Collection<Party> parties);

    /**
     * Create a new party.
     *
     * @param party The party
     * @throws DataAccessException Thrown if data can't be accessed
     * @throws PartyExistsException If there's already an existing party with the ID
     */
    void create(Party party) throws PartyExistsException;

    /**
     * Add or update the given members for the given party.
     *
     * <p>If the party does not exist, {@link DataAccessException} will be thrown.</p>
     *
     * @param party The party
     * @param members The members
     */
    void addMembers(String party, Set<Member> members);

    /**
     * Remove the given members from the given party.
     *
     * @param party The party
     * @param members The members
     */
    void removeMembers(String party, Set<MojangId> members);
}
