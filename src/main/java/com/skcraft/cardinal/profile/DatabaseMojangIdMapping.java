package com.skcraft.cardinal.profile;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.skcraft.cardinal.util.DataAccessException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.google.common.base.Preconditions.checkNotNull;

public class DatabaseMojangIdMapping implements MojangIdMapping {
    private final DataSource ds;
    private final ConcurrentMap<MojangId, Integer> cache = new ConcurrentHashMap<>();

    @Inject
    public DatabaseMojangIdMapping(DataSource dataSource) {
        checkNotNull(dataSource, "dataSource");
        this.ds = dataSource;
    }

    @Override
    public int get(MojangId mojangId) {
        checkNotNull(mojangId, "userId");
        return getAll(Lists.newArrayList(mojangId)).get(mojangId);
    }

    @Override
    public Map<MojangId, Integer> getAll(Collection<MojangId> mojangIds) {
        checkNotNull(mojangIds);

        if (mojangIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<MojangId, Integer> results = new HashMap<>();
        List<MojangId> unknown = new ArrayList<>();

        for (MojangId mojangId : mojangIds) {
            Integer id = cache.get(mojangId);
            if (id != null) {
                results.put(mojangId, id);
            } else {
                unknown.add(mojangId);
            }
        }

        if (!unknown.isEmpty()) {
            try (Connection conn = ds.getConnection()) {
                conn.setAutoCommit(true);

                PreparedStatement stmt = conn.prepareStatement("" +
                        "INSERT INTO user_id " +
                        "(uuid, name) " +
                        "VALUES " +
                        Strings.repeat("(?, ?), ", unknown.size() - 1) + "(?, ?) " +
                        "ON DUPLICATE KEY UPDATE name = VALUES(name)");

                int index = 1;
                for (MojangId mojangId : unknown) {
                    stmt.setString(index++, mojangId.getUuid().toString());
                    stmt.setString(index++, mojangId.getName());
                }

                stmt.execute();

                stmt = conn.prepareStatement("SELECT * FROM user_id WHERE uuid IN (" + Strings.repeat("?, ", unknown.size() - 1) + "?)");

                index = 1;
                for (MojangId mojangId : unknown) {
                    stmt.setString(index++, mojangId.getUuid().toString());
                }

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        MojangId mojangId = new MojangId(UUID.fromString(rs.getString("uuid")), rs.getString("name"));
                        results.put(mojangId, rs.getInt("id"));
                        cache.put(mojangId, rs.getInt("id"));
                    }
                }
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }

        return Collections.unmodifiableMap(results);
    }
}
