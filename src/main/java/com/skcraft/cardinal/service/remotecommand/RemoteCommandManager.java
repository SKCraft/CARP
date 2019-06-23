package com.skcraft.cardinal.service.remotecommand;

import com.google.common.util.concurrent.ListenableFuture;
import com.skcraft.cardinal.service.hive.permission.Context;
import com.skcraft.cardinal.profile.ProfileId;

import javax.annotation.Nullable;

public interface RemoteCommandManager {
    @Nullable
    ListenableFuture<Response> execute(ProfileId sender, String message, Context context);
}
