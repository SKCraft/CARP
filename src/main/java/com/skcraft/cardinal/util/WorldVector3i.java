package com.skcraft.cardinal.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public final class WorldVector3i implements Serializable {

    private final String worldId;
    private final int x;
    private final int y;
    private final int z;

    @JsonCreator
    public WorldVector3i(@JsonProperty("worldId") String worldId, @JsonProperty("x") int x, @JsonProperty("y") int y, @JsonProperty("z") int z) {
        this.worldId = worldId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String getWorldId() {
        return worldId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public WorldVector3i add(int x, int y, int z) {
        return new WorldVector3i(worldId, this.x + x, this.y + y, this.z + z);
    }

    public WorldVector3i sub(int x, int y, int z) {
        return new WorldVector3i(worldId, this.x - x, this.y - y, this.z - z);
    }

    public WorldVector3i mult(int x, int y, int z) {
        return new WorldVector3i(worldId, this.x * x, this.y * y, this.z * z);
    }

    public WorldVector3i div(int x, int y, int z) {
        return new WorldVector3i(worldId, this.x / x, this.y / y, this.z / z);
    }

    public WorldVector3i chunk() {
        return new WorldVector3i(worldId, this.x >> 4, this.y >> 4, this.z >> 4);
    }

    @Override
    public String toString() {
        return "{" + worldId + ":" + x + "," + y + "," + z + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WorldVector3i)) {
            return false;
        }

        WorldVector3i other = (WorldVector3i) obj;
        return other.getWorldId().equalsIgnoreCase(getWorldId()) && getX() == other.getX() && getY() == other.getY() && getZ() == other.getZ();
    }

    @Override
    public int hashCode() {
        return getX() + getZ() << 16 + getY() << 24 + getWorldId().hashCode() << 20;
    }

}
