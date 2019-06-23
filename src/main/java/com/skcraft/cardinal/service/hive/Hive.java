package com.skcraft.cardinal.service.hive;

import com.skcraft.cardinal.service.hive.permission.Subject;
import com.skcraft.cardinal.profile.ProfileId;

import javax.annotation.Nullable;

public interface Hive {
    User login(ProfileId profileId) throws SessionRejectedException;

    @Nullable
    User get(ProfileId profileId);

    Subject getSubject(ProfileId profileId);

    void invite(String name, ProfileId referrer) throws ErrorResponse;
}
