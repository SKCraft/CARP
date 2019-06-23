package com.skcraft.cardinal.service.hive;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.annotation.Nullable;
import java.util.Set;

@ToString
public class User {
    @Nullable
    @Getter
    @Setter
    private String hostKey;
    @Getter
    @Setter
    private Set<String> groups = Sets.newHashSet();

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
