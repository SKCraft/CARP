package com.skcraft.cardinal.service.hive.permission;

public class NullSubject implements Subject {
    @Override
    public boolean hasPermission(String permission, Context context) {
        return false;
    }
}
