package com.skcraft.cardinal.service.hive.permission;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Sets;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implements a Subject for a User.
 */
public class UserSubject implements Subject {

    private final Set<Group> groups;

    private final LoadingCache<String, Grant> permCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Grant>() {
                @Override
                public Grant load(String key) throws Exception {
                    return getPermission(key);
                }
            });

    public UserSubject(Set<Group> groups) {
        this.groups = groups;
    }

    @Override
    public boolean hasPermission(String permission, Context context) {
        Grant ret = Grant.baseline();
        for (String p : LegacyContext.getEffectivePermissions(permission, context)) {
            ret = ret.add(permCache.getUnchecked(p));
        }
        return ret.isPermit();
    }

    /**
     * Get the grant for the given permission.
     *
     * @param permission The permission
     * @return The grant
     */
    public Grant getPermission(String permission) {
        checkNotNull(permission, "permission");

        permission = permission.toLowerCase();
        Set<Group> visited = Sets.newHashSet();
        Grant ret = Grant.baseline();

        for (Group group : groups) {
            ret = group.getPermission(permission, visited).add(ret);
            if (ret.isFinal()) return ret;
        }

        return ret;
    }

}
