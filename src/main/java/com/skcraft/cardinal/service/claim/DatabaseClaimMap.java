package com.skcraft.cardinal.service.claim;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.skcraft.cardinal.util.WorldVector3i;
import com.skcraft.cardinal.profile.MojangId;
import com.skcraft.cardinal.profile.MojangIdMapping;
import com.skcraft.cardinal.util.DataAccessException;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class DatabaseClaimMap implements ClaimMap {
    private final DataSource ds;
    private final MojangIdMapping idMapping;
    private final String serverId;

    @Inject
    public DatabaseClaimMap(DataSource dataSource, MojangIdMapping idMapping, String serverId) {
        checkNotNull(dataSource, "dataSource");
        checkNotNull(idMapping, "userIdCache");
        checkNotNull(serverId, "serverId");
        this.ds = dataSource;
        this.idMapping = idMapping;
        this.serverId = serverId;
    }

    private void addPositionsToStatement(PreparedStatement stmt, Collection<WorldVector3i> positions, int index) throws SQLException {
        for (WorldVector3i position : positions) {
            stmt.setString(index++, position.getWorldId());
            stmt.setInt(index++, position.getX());
            stmt.setInt(index++, position.getZ());
        }
    }

    @Override
    @Nullable
    public Claim get(WorldVector3i position) {
        checkNotNull(position, "position");
        return getAll(Lists.newArrayList(position)).get(position);
    }

    @Override
    public Map<WorldVector3i, Claim> getAll(Collection<WorldVector3i> positions) {
        checkNotNull(positions, "positions");

        if (positions.isEmpty()) {
            return Collections.emptyMap();
        }

        ImmutableMap.Builder<WorldVector3i, Claim> resultBuilder = ImmutableMap.builder();

        try (Connection conn = ds.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("" +
                    "SELECT c.*, id.uuid, id.name FROM claim c " +
                    "LEFT JOIN user_id AS id " +
                    "ON (id.id = c.owner_id) " +
                    "WHERE server = ? AND (world, x, z) IN (" +
                    Strings.repeat("(?, ?, ?), ", positions.size() - 1) + "(?, ?, ?))");

            stmt.setString(1, serverId);
            addPositionsToStatement(stmt, positions, 2);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    WorldVector3i position = new WorldVector3i(rs.getString("world"), rs.getInt("x"), 0, rs.getInt("z"));
                    Claim claim = new Claim(rs.getString("server"), position);
                    claim.setOwner(new MojangId(UUID.fromString(rs.getString("uuid")), rs.getString("name")));
                    claim.setParty(rs.getString("party_name"));
                    claim.setIssueTime(rs.getTimestamp("issue_time"));
                    resultBuilder.put(position, claim);
                }
            }

            return resultBuilder.build();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void save(Collection<WorldVector3i> positions, MojangId owner, @Nullable String party) {
        checkNotNull(positions, "positions");
        checkNotNull(owner, "owner");

        if (positions.isEmpty()) {
            return;
        }

        int ownerRowId = idMapping.get(owner);
        Date now = new Date(Calendar.getInstance().getTime().getTime());

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(true);

            PreparedStatement stmt = conn.prepareStatement("" +
                    "REPLACE INTO claim" +
                    "(server, world, x, z, owner_id, party_name, issue_time) " +
                    "VALUES " +
                    Strings.repeat("(?, ?, ?, ?, ?, ?, ?), ", positions.size() - 1) + "(?, ?, ?, ?, ?, ?, ?)");

            int index = 1;
            for (WorldVector3i position : positions) {
                stmt.setString(index++, serverId);
                stmt.setString(index++, position.getWorldId());
                stmt.setInt(index++, position.getX());
                stmt.setInt(index++, position.getZ());
                stmt.setInt(index++, ownerRowId);
                stmt.setString(index++, party);
                stmt.setDate(index++, now);
            }

            stmt.execute();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void update(Collection<WorldVector3i> positions, MojangId owner, @Nullable String party, @Nullable MojangId existingOwner) {
        checkNotNull(positions, "positions");
        checkNotNull(owner, "owner");

        if (positions.isEmpty()) {
            return;
        }

        List<WorldVector3i> filteredPositions = Lists.newArrayList(positions);
        int ownerRowId = idMapping.get(owner);
        Date now = new Date(Calendar.getInstance().getTime().getTime());

        Map<WorldVector3i, Claim> existing = getAll(filteredPositions);
        for (Map.Entry<WorldVector3i, Claim> entry : existing.entrySet()) {
            if (existingOwner != entry.getValue().getOwner()) {
                filteredPositions.remove(entry.getKey());
            }
        }

        if (filteredPositions.isEmpty()) {
            return;
        }

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement stmt = conn.prepareStatement("" +
                    "REPLACE INTO claim " +
                    "(server, world, x, z, owner_id, party_name, issue_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)");

            for (WorldVector3i position : filteredPositions) {
                stmt.setString(1, serverId);
                stmt.setString(2, position.getWorldId());
                stmt.setInt(3, position.getX());
                stmt.setInt(4, position.getZ());
                stmt.setInt(5, ownerRowId);
                stmt.setString(6, party);
                stmt.setDate(7, now);
                stmt.addBatch();
            }

            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public int remove(Collection<WorldVector3i> positions) {
        checkNotNull(positions, "positions");

        if (positions.isEmpty()) {
            return 0;
        }

        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(true);

            PreparedStatement stmt = conn.prepareStatement("" +
                    "DELETE FROM claim WHERE (server, world, x, z) IN (" +
                    Strings.repeat("(?, ?, ?, ?), ", positions.size() - 1) + "(?, ?, ?, ?))");

            int index = 1;
            for (WorldVector3i position : positions) {
                stmt.setString(index++, serverId);
                stmt.setString(index++, position.getWorldId());
                stmt.setInt(index++, position.getX());
                stmt.setInt(index++, position.getZ());
            }

            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public int getCountByOwner(MojangId owner) {
        checkNotNull(owner, "owner");

        Integer ownerRowId = idMapping.get(owner);

        try (Connection conn = ds.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("" +
                    "SELECT COUNT(*) as claim_count FROM claim WHERE server = ? AND owner_id = ?");

            stmt.setString(1, serverId);
            stmt.setInt(2, ownerRowId);

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt("claim_count");
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
