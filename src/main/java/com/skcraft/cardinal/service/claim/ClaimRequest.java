package com.skcraft.cardinal.service.claim;

import com.google.common.collect.Sets;
import com.skcraft.cardinal.profile.MojangId;
import com.skcraft.cardinal.util.WorldVector3i;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class ClaimRequest {

    private final ClaimCache claimCache;
    @Getter private final MojangId owner;
    @Getter @Nullable private final String party;
    @Getter private final int currentTotalOwnedCount;
    @Getter private final Set<WorldVector3i> unclaimed = Sets.newHashSet();
    @Getter private final Set<WorldVector3i> alreadyOwned = Sets.newHashSet();
    @Getter private final Set<WorldVector3i> ownedByOthers = Sets.newHashSet();

    public ClaimRequest(ClaimCache claimCache, MojangId owner, String party) {
        checkNotNull(claimCache, "claimCache");
        checkNotNull(owner, "owner");
        this.claimCache = claimCache;
        this.owner = owner;
        this.party = party;
        this.currentTotalOwnedCount = claimCache.getClaimMap().getCountByOwner(owner);
    }

    public void addPositions(Collection<WorldVector3i> positions) {
        if (positions.isEmpty()) {
            return;
        }

        Map<WorldVector3i, Claim> existing = claimCache.getClaimMap().getAll(positions);

        // Sort out positions into free chunks and owned chunks
        for (Map.Entry<WorldVector3i, Claim> entry : existing.entrySet()) {
            Claim claim = existing.get(entry.getKey());
            if (claim.getOwner().equals(owner)) {
                alreadyOwned.add(entry.getKey());
            } else {
                ownedByOthers.add(entry.getKey());
            }
        }

        for (WorldVector3i position : positions) {
            if (!alreadyOwned.contains(position) && !ownedByOthers.contains(position)) {
                unclaimed.add(position);
            }
        }
    }

    public void checkQuota(int max) throws ClaimAttemptException {
        int newTotal = unclaimed.size() + currentTotalOwnedCount;
        if (newTotal > max) {
            throw new ClaimAttemptException("You can only own a maximum of " + max + " chunks and you already on " + currentTotalOwnedCount + ".");
        }
    }

    public void checkRemaining() throws ClaimAttemptException {
        if (getUnclaimed().size() + getAlreadyOwned().size() == 0) {
            throw new ClaimAttemptException("Your selected area is already claimed by others.");
        }
    }

    public boolean hasUnclaimed() {
        return !unclaimed.isEmpty();
    }

    public boolean hasClaimed() {
        return !alreadyOwned.isEmpty();
    }

    public Object getPositionCount() {
        return unclaimed.size() + alreadyOwned.size() + ownedByOthers.size();
    }
}
