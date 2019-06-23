package com.skcraft.cardinal.util.event;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used to mark methods as event handlers.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Subscribe {

    /**
     * The priority as far as order of dispatching is concerned.
     *
     * @return the priority
     */
    EventHandler.Priority priority() default EventHandler.Priority.NORMAL;

    boolean ignoreCancelled() default true;

}
