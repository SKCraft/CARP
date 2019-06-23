package com.skcraft.cardinal.service.hive.permission;

/**
 * An object that can have permissions assigned to it.
 */
public interface Subject {

    /**
     * Return whether this subject has the given permission.
     *
     * @param permission The permission
     * @param context The context of the request
     * @return Whether the permission is granted
     */
    boolean hasPermission(String permission, Context context);

}
