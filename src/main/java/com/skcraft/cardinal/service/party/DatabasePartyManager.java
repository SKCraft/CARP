package com.skcraft.cardinal.service.party;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.skcraft.cardinal.profile.MojangId;
import com.skcraft.cardinal.profile.MojangIdMapping;
import com.skcraft.cardinal.util.DataAccessException;
import lombok.Value;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class DatabasePartyManager implements PartyManager {
    private final DataSource ds;
    private final MojangIdMapping idMapping;

    @Inject
    public DatabasePartyManager(DataSource dataSource, MojangIdMapping idMapping) {
        this.ds = dataSource;
        this.idMapping = idMapping;
    }

    @Override
    public Map<String, Party> getAll(Collection<String> names) {
        return updateParties(names, Collections.emptyList()).getParties();
    }

    @Override
    public Set<String> refreshAll(Collection<Party> parties) {
        List<String> names = new ArrayList<>();
        for (Party party : parties) {
            names.add(party.getName());
        }
        return updateParties(names, parties).getRemoved();
    }

    @Override
    public void create(Party party) throws PartyExistsException {
        checkNotNull(party, "party");

        Date now = new Date(Calendar.getInstance().getTime().getTime());
        Set<MojangId> mojangIds = new HashSet<>();

        for (Member member : party.getMembers()) {
            mojangIds.add(member.getUserId());
        }

        Map<MojangId, Integer> userRowIds = idMapping.getAll(mojangIds);

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            try {
                PreparedStatement stmt = conn.prepareStatement("INSERT IGNORE INTO party (name, create_time) VALUES (?, ?)");
                stmt.setString(1, party.getName());
                stmt.setDate(2, now);

                if (stmt.executeUpdate() > 0) {
                    stmt = conn.prepareStatement("INSERT INTO party_member (party_name, user_id, rank) VALUES (?, ?, ?)");
                    for (Member member : party.getMembers()) {
                        stmt.setString(1, party.getName());
                        stmt.setInt(2, userRowIds.get(member.getUserId()));
                        stmt.setString(3, member.getRank().name());
                        stmt.executeUpdate();
                    }
                } else {
                    throw new PartyExistsException("The party " + party.getName() + " already exists");
                }

                conn.commit();
            } catch (SQLException | PartyExistsException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void addMembers(String party, Set<Member> members) {
        Set<MojangId> mojangIds = new HashSet<>();

        for (Member member : members) {
            mojangIds.add(member.getUserId());
        }

        Map<MojangId, Integer> userRowIds = idMapping.getAll(mojangIds);

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            try {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO party_member (party_name, user_id, rank) VALUES (?, ?, ?)");
                for (Member member : members) {
                    stmt.setString(1, party);
                    stmt.setInt(2, userRowIds.get(member.getUserId()));
                    stmt.setString(3, member.getRank().name());
                    stmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void removeMembers(String party, Set<MojangId> members) {
        Map<MojangId, Integer> userRowIds = idMapping.getAll(members);

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            try {
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM party_member WHERE party_name = ? AND user_id = ?");
                for (MojangId mojangId : members) {
                    stmt.setString(1, party);
                    stmt.setInt(2, userRowIds.get(mojangId));
                    stmt.execute();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    private PartyUpdate updateParties(Collection<String> names, Collection<Party> partiesToUpdate) {
        checkNotNull(names, "names");

        if (names.isEmpty()) {
            return new PartyUpdate(Collections.emptyMap(), Collections.emptySet());
        }

        // We'll use this to keep track of parties that don't exist (or no longer exist)
        Set<String> remaining = names.stream().map(String::toLowerCase).collect(Collectors.toSet());
        Map<String, Party> results = new HashMap<>();
        Multimap<Party, Member> members = HashMultimap.create();

        for (Party party : partiesToUpdate) {
            results.put(party.getName().toLowerCase(), party);
        }

        try (Connection conn = ds.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT name, create_time FROM party WHERE party.name IN (" + Strings.repeat("?, ", names.size() - 1) + "?)");

            int index = 1;
            for (String name : names) {
                stmt.setString(index++, name);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String nameLower = name.toLowerCase();
                    remaining.remove(nameLower);

                    Party party = results.get(nameLower);

                    // If we're not updating an existing list of parties, create a new entry
                    if (party == null) {
                        party = new Party(name);
                        results.put(nameLower, party);
                    }

                    party.setCreateTime(rs.getTimestamp("create_time"));
                }
            }

            if (results.isEmpty()) {
                return new PartyUpdate(results, remaining);
            }

            stmt = conn.prepareStatement("" +
                    "SELECT m.party_name, m.user_id, m.rank, u.uuid, u.name " +
                    "FROM party_member m, user_id u " +
                    "WHERE u.id = m.user_id AND party_name IN (" + Strings.repeat("?, ", results.size() - 1) + "?)");

            index = 1;
            for (String name : results.keySet()) {
                stmt.setString(index++, name);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Party party = results.get(rs.getString("party_name").toLowerCase());
                    MojangId mojangId = new MojangId(UUID.fromString(rs.getString("uuid")), rs.getString("name"));
                    Member member = new Member(mojangId, Rank.valueOf(rs.getString("rank")));
                    members.put(party, member);
                }
            }

            for (Map.Entry<Party, Collection<Member>> entry : members.asMap().entrySet()) {
                entry.getKey().setMembers(Sets.newConcurrentHashSet(entry.getValue()));
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }

        return new PartyUpdate(results, remaining);
    }

    @Value
    public static class PartyUpdate {
        private final Map<String, Party> parties;
        private final Set<String> removed;
    }
}
