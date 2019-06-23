package com.skcraft.cardinal.util.event;

import com.google.common.collect.Multimap;
import com.google.common.eventbus.DeadEvent;

public interface EventBus {

    /**
     * Registers the given handler for the given class to receive events.
     *
     * @param clazz the event class to register
     * @param handler the handler to register
     */
    void subscribe(Class<?> clazz, EventHandler handler);

    /**
     * Registers the given handler for the given class to receive events.
     *
     * @param handlers a map of handlers
     */
    void subscribeAll(Multimap<Class<?>, EventHandler> handlers);

    /**
     * Unregisters the given handler for the given class.
     *
     * @param clazz the class
     * @param handler the handler
     */
    void unsubscribe(Class<?> clazz, EventHandler handler);

    /**
     * Unregisters the given handlers.
     *
     * @param handlers a map of handlers
     */
    void unsubscribeAll(Multimap<Class<?>, EventHandler> handlers);

    /**
     * Registers all handler methods on {@code object} to receive events.
     * Handler methods are selected and classified using this EventBus's
     * {@link SubscriberFindingStrategy}; the default strategy is the
     * {@link AnnotatedSubscriberFinder}.
     *
     * @param object object whose handler methods should be registered.
     */
    void register(Object object);

    /**
     * Unregisters all handler methods on a registered {@code object}.
     *
     * @param object  object whose handler methods should be unregistered.
     * @throws IllegalArgumentException if the object was not previously registered.
     */
    void unregister(Object object);

    /**
     * Posts an event to all registered handlers.  This method will return
     * successfully after the event has been posted to all handlers, and
     * regardless of any exceptions thrown by handlers.
     *
     * <p>If no handlers have been subscribed for {@code event}'s class, and
     * {@code event} is not already a {@link DeadEvent}, it will be wrapped in a
     * DeadEvent and reposted.
     *
     * @param event  event to post.
     */
    void post(Object event);

}
