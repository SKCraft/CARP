package com.skcraft.cardinal.util.concurrent;

public interface Filter<I, O> {

    O apply(I input) throws Exception;

}
