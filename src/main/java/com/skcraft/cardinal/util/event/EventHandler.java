package com.skcraft.cardinal.util.event;

import java.lang.reflect.InvocationTargetException;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Event handler object for {@link MultimapEventBus} that is able to dispatch
 * an event.
 *
 * <p>Original for Guava, licensed under the Apache License, Version 2.0.</p>
 */
public abstract class EventHandler implements Comparable<EventHandler> {

    public enum Priority {
        VERY_EARLY,
        EARLY,
        NORMAL,
        LATE,
        VERY_LATE
    }

    private final Priority priority;
    private final boolean ignoredCancelled;

    /**
     * Create a new event handler.
     *
     * @param priority the priority
     * @param ignoredCancelled whether to ignore cancelled events
     */
    protected EventHandler(Priority priority, boolean ignoredCancelled) {
        checkNotNull(priority);
        this.priority = priority;
        this.ignoredCancelled = ignoredCancelled;
    }

    /**
     * Get the priority.
     *
     * @return the priority
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Dispatch the given event.
     *
     * <p>Subclasses should override {@link #dispatch(Object)}.</p>
     *
     * @param event the event
     * @throws InvocationTargetException thrown if an exception is thrown during dispatch
     */
    public final void handleEvent(Object event) throws InvocationTargetException {
        if (event instanceof Cancellable && ((Cancellable) event).isCancelled() && ignoredCancelled) {
            return;
        }
        try {
            dispatch(event);
        } catch (Throwable t) {
            throw new InvocationTargetException(t);
        }
    }

    /**
     * Dispatch the event.
     *
     * @param event the event object
     * @throws Exception an exception that may be thrown
     */
    public abstract void dispatch(Object event) throws Exception;

    @Override
    public int compareTo(EventHandler o) {
        return getPriority().ordinal() - o.getPriority().ordinal();
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public String toString() {
        return "EventHandler{" +
                "priority=" + priority +
                '}';
    }

}
