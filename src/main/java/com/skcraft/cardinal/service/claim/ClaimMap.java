package com.skcraft.cardinal.service.claim;

import com.skcraft.cardinal.profile.MojangId;
import com.skcraft.cardinal.util.WorldVector3i;
import com.skcraft.cardinal.util.DataAccessException;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

/**
 * A claim map persists claim data and provides access to it.
 *
 * <p>Direct access to claim maps should be used to modify claims, but if
 * claim data needs to be queried for real-time claim information,
 * an instance of {@link ClaimCache} should be used so that claim data
 * can be loaded asynchronously.</p>
 */
public interface ClaimMap {
    /**
     * Get the claim for the given chunk position.
     *
     * @param position Chunk coordinates
     * @return A claim object, or null if there was no claim set
     * @throws DataAccessException If data could not be retrieved or saved
     */
    @Nullable
    Claim get(WorldVector3i position);

    /**
     * Fetch claim data for the the given chunk positions, returning
     * a map with keys corresponding to the chunk position and the
     * values reflective of the claim at that location.
     *
     * <p>If a claim does not exist at a given position, then the returned map
     * will contain {@code null} for that position.</p>
     *
     * @param positions A list of chunk coordinates
     * @return A map of chunk position => claims
     * @throws DataAccessException If data could not be retrieved or saved
     */
    Map<WorldVector3i, Claim> getAll(Collection<WorldVector3i> positions);

    /**
     * Set claim information for the given chunk positions, overwriting any
     * existing claims at those locations.
     *
     * <p>Multiple threads must not call this method at the same time.</p>
     *
     * @param positions A list of chunk coordinates
     * @param owner     The new owner of the claims
     * @param party     An optional party to associate with the claim
     * @throws DataAccessException If data could not be retrieved or saved
     */
    void save(Collection<WorldVector3i> positions, MojangId owner, @Nullable String party);

    /**
     * Update claim information for the given chunk positions, but only if
     * claims at those chunks exist and are owned by {@code existingOwner}
     * (or can be null to indicate unclaimed chunks).
     *
     * <p>Multiple threads must not call this method at the same time.</p>
     *
     * @param positions     A list of chunk coordinates
     * @param owner         The new owner of the claims
     * @param party         An optional party to associate with the claim
     * @param existingOwner The existing owner to match, or null to match unclaimed chunks
     * @throws DataAccessException If data could not be retrieved or saved
     */
    void update(Collection<WorldVector3i> positions, MojangId owner, @Nullable String party, @Nullable MojangId existingOwner);

    /**
     * Remove the claims at the given positions.
     *
     * @param positions A list of chunk coordinates
     * @return The number of changed rows
     */
    int remove(Collection<WorldVector3i> positions);

    /**
     * Get the number of claims that a player owns.
     *
     * @param owner The user ID of the owner
     * @return The number of claims
     */
    int getCountByOwner(MojangId owner);
}
