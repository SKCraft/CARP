package com.skcraft.cardinal.service.hive.permission;

public class PermitSubject implements Subject {
    @Override
    public boolean hasPermission(String permission, Context context) {
        return true;
    }
}
