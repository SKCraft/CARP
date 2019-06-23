package com.skcraft.cardinal.service.party;

import com.skcraft.cardinal.profile.MojangId;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a member (with associated user and rank) in a party.
 */
@Data
@EqualsAndHashCode(of = "userId")
public class Member {

    private MojangId userId;
    private Rank rank;

    public Member(MojangId userId, Rank rank) {
        this.userId = userId;
        this.rank = rank;
    }

}
