package com.skcraft.cardinal.util.event;

public interface Cancellable {
    boolean isCancelled();

    void setCancelled(boolean cancelled);
}
