package com.skcraft.cardinal.util.event;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Holds a cache of class hierarchy.
 *
 * <p>This exists because Bukkit has an ancient version of Guava and the cache
 * library in Guava has since changed.</>
 */
class HierarchyCache {

    private final Map<Class<?>, Set<Class<?>>> cache = new WeakHashMap<Class<?>, Set<Class<?>>>();

    public Set<Class<?>> get(Class<?> concreteClass) {
        Set<Class<?>> ret = cache.get(concreteClass);
        if (ret == null) {
            ret = build(concreteClass);
            cache.put(concreteClass, ret);
        }
        return ret;
    }

    protected Set<Class<?>> build(Class<?> concreteClass) {
        List<Class<?>> parents = Lists.newLinkedList();
        Set<Class<?>> classes = Sets.newHashSet();

        parents.add(concreteClass);

        while (!parents.isEmpty()) {
            Class<?> clazz = parents.remove(0);
            classes.add(clazz);

            Class<?> parent = clazz.getSuperclass();
            if (parent != null) {
                parents.add(parent);
            }

            Collections.addAll(parents, clazz.getInterfaces());
        }

        return classes;
    }

}
