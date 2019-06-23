package com.skcraft.cardinal.service.hive.permission;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A user group with a set of permissions.
 */
@Data
@EqualsAndHashCode(of = "name")
@ToString(of = {"name"})
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="name")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Group implements Subject {

    private String name;
    private Set<Group> parents = Sets.newHashSet();
    private List<String> permissions = Lists.newArrayList();

    /**
     * Query the grant for the given permission string.
     *
     * @param permission The permission string
     * @param visited A set of groups that will not be re-visited (will be modified by the method)
     * @return The permission
     */
    public Grant getPermission(String permission, Set<Group> visited) {
        checkNotNull(permission, "permission");
        checkNotNull(visited, "visited");

        permission = permission.toLowerCase();

        Grant ret = Grant.baseline();

        // Make sure that we don't go over the same group
        if (visited.contains(this)) {
            return Grant.baseline();
        }

        visited.add(this);

        ret = getPermissionHere(permission).add(ret);
        if (ret.isFinal()) return ret;

        for (Group parent : parents) {
            ret = parent.getPermission(permission, visited).add(ret);
            if (ret.isFinal()) return ret;
        }

        return ret;
    }

    private Grant getPermissionHere(String permission) {
        if (permissions.contains("*")) {
            return Grant.baselineAllow();
        }

        String testPerm = permission;

        Grant ret = Grant.baseline();

        while (true) {
            if (permissions.contains("!" + testPerm)) { // Never permission
                return Grant.NEVER;
            } else if (permissions.contains("-" + testPerm)) { // Deny permission
                return Grant.DENY;
            } else if (permissions.contains(testPerm)) { // Allow permission
                ret = Grant.ALLOW;
            }

            int lastDot = testPerm.lastIndexOf(".");

            if (lastDot == -1) {
                return ret;
            }

            testPerm = testPerm.substring(0, lastDot);
        }
    }

    @Override
    public boolean hasPermission(String permission, Context context) {
        Grant ret = Grant.baseline();
        for (String p : LegacyContext.getEffectivePermissions(permission, context)) {
            ret = ret.add(getPermission(p, Sets.<Group>newHashSet()));
        }
        return ret.isPermit();
    }

}
