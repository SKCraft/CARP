package com.skcraft.cardinal.service.claim;

import com.skcraft.cardinal.util.WorldVector3i;
import com.skcraft.cardinal.service.party.Party;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Used to store claim information for {@link ClaimCache}.
 *
 * <p>Chunk states are created when a request for claim data is made in
 * {@link ClaimCache}, and then it is associated with the chunk position
 * before chunk data is loaded. Once chunk data arrives, the chunk state
 * instance will be updated with the claim and {@link #isLoaded()} becomes
 * {@code true}.</p>
 *
 * <p>If a request to invalidate a cache entry for a certain chunk
 * position is made, the chunk state for that position is removed from
 * the cache. If a new request for claim data is made for that chunk
 * position, a new ChunkState instance is made.</p>
 */
@ToString
class ChunkState implements ClaimEntry {
    @Getter
    private final WorldVector3i position;
    private ClaimData data;
    @Getter @Setter
    private boolean loaded;

    /**
     * Create a new instance.
     *
     * @param position The position of the chunk
     */
    public ChunkState(WorldVector3i position) {
        checkNotNull(position, "position");
        this.position = position;
    }

    public void setData(Claim claim, @Nullable Party party) {
        this.data = claim != null ? new ClaimData(claim, party) : null;
    }

    @Override
    public Claim getClaim() {
        ClaimData tuple = this.data;
        return tuple != null ? tuple.claim : null;
    }

    @Nullable
    @Override
    public Party getParty() {
        ClaimData tuple = this.data;
        return tuple != null ? tuple.party : null;
    }

    @Nullable
    public ClaimData getData() {
        return data;
    }

    public void setData(@Nullable ClaimData data) {
        this.data = data;
    }

    @ToString
    private static class ClaimData {
        private final Claim claim;
        @Nullable
        private final Party party;

        private ClaimData(Claim claim, Party party) {
            this.claim = claim;
            this.party = party;
        }
    }
}
