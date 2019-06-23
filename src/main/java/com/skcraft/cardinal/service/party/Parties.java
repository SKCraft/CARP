package com.skcraft.cardinal.service.party;

import com.skcraft.cardinal.profile.MojangId;

public final class Parties {

    private Parties() { }

    public static boolean isMember(Party party, MojangId userId) {
        return party.getMembers().contains(new Member(userId, Rank.MEMBER));
    }

    public static boolean canManage(Party party, MojangId userId) {
        for (Member member : party.getMembers()) {
            if (member.getUserId().equals(userId)) {
                return member.getRank().canManage();
            }
        }

        return false;
    }

    public static Member getMemberByUser(Party party, MojangId user) {
        for (Member member : party.getMembers()) {
            if (member.getUserId().equals(user)) {
                return member;
            }
        }

        return null;
    }

    public static String getMemberListStr(Party party) {
        StringBuilder str = new StringBuilder();
        boolean isFirst = true;

        for (Member member : party.getMembers()) {
            switch(member.getRank()) {
                case OWNER:
                    if (!isFirst) str.append(", ");
                    str.append(member.getUserId().getName()).append("[owner]");
                    break;

                case MANAGER:
                    if (!isFirst) str.append(", ");
                    str.append(member.getUserId().getName()).append("*");
                    break;

                case MEMBER:
                    if (!isFirst) str.append(", ");
                    str.append(member.getUserId().getName());
                    break;
            }
            if (isFirst) isFirst = false;
        }

        return str.toString();
    }
}
