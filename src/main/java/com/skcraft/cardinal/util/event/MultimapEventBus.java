package com.skcraft.cardinal.util.event;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Dispatches events to listeners, and provides ways for listeners to register
 * themselves.
 *
 * <p>This class is based on Guava's {@link MultimapEventBus} but priority is supported
 * and events are dispatched at the time of call, rather than being queued up.
 * This does allow dispatching during an in-progress dispatch.</p>
 *
 * <p>This implementation utilizes naive synchronization on all getter and
 * setter methods. Dispatch does not occur when a lock has been acquired,
 * however.</p>
 */
public class MultimapEventBus implements EventBus {

    private final Logger logger = Logger.getLogger(MultimapEventBus.class.getCanonicalName());

    private final SetMultimap<Class<?>, EventHandler> handlersByType =
            Multimaps.newSetMultimap(new HashMap<>(), this::newHandlerSet);

    /**
     * Strategy for finding handler methods in registered objects.  Currently,
     * only the {@link AnnotatedSubscriberFinder} is supported, but this is
     * encapsulated for future expansion.
     */
    private final SubscriberFindingStrategy finder = new AnnotatedSubscriberFinder();

    private HierarchyCache flattenHierarchyCache = new HierarchyCache();

    @Override
    public synchronized void subscribe(Class<?> clazz, EventHandler handler) {
        checkNotNull(clazz);
        checkNotNull(handler);
        handlersByType.put(clazz, handler);
    }

    @Override
    public synchronized void subscribeAll(Multimap<Class<?>, EventHandler> handlers) {
        checkNotNull(handlers);
        handlersByType.putAll(handlers);
    }

    @Override
    public synchronized void unsubscribe(Class<?> clazz, EventHandler handler) {
        checkNotNull(clazz);
        checkNotNull(handler);
        handlersByType.remove(clazz, handler);
    }

    @Override
    public synchronized void unsubscribeAll(Multimap<Class<?>, EventHandler> handlers) {
        checkNotNull(handlers);
        for (Map.Entry<Class<?>, Collection<EventHandler>> entry : handlers.asMap().entrySet()) {
            Set<EventHandler> currentHandlers = getHandlersForEventType(entry.getKey());
            Collection<EventHandler> eventMethodsInListener = entry.getValue();

            if (currentHandlers != null &&!currentHandlers.containsAll(entry.getValue())) {
                currentHandlers.removeAll(eventMethodsInListener);
            }
        }
    }

    @Override
    public void register(Object object) {
        subscribeAll(finder.findAllSubscribers(object));
    }

    @Override
    public void unregister(Object object) {
        unsubscribeAll(finder.findAllSubscribers(object));
    }

    @Override
    public void post(Object event) {
        List<EventHandler> dispatching = new ArrayList<EventHandler>();

        synchronized (this) {
            Set<Class<?>> dispatchTypes = flattenHierarchy(event.getClass());

            for (Class<?> eventType : dispatchTypes) {
                Set<EventHandler> wrappers = getHandlersForEventType(eventType);

                if (wrappers != null && !wrappers.isEmpty()) {
                    dispatching.addAll(wrappers);
                }
            }
        }

        Collections.sort(dispatching);

        for (EventHandler handler : dispatching) {
            dispatch(event, handler);
        }
    }

    /**
     * Dispatches {@code event} to the handler in {@code handler}.  This method
     * is an appropriate override point for subclasses that wish to make
     * event delivery asynchronous.
     *
     * @param event  event to dispatch.
     * @param handler  handler that will call the handler.
     */
    protected void dispatch(Object event, EventHandler handler) {
        try {
            handler.handleEvent(event);
        } catch (InvocationTargetException e) {
            logger.log(Level.SEVERE,
                    "Could not dispatch event: " + event + " to handler " + handler, e);
        }
    }

    /**
     * Retrieves a mutable set of the currently registered handlers for
     * {@code type}.  If no handlers are currently registered for {@code type},
     * this method may either return {@code null} or an empty set.
     *
     * @param type  type of handlers to retrieve.
     * @return currently registered handlers, or {@code null}.
     */
    synchronized Set<EventHandler> getHandlersForEventType(Class<?> type) {
        return handlersByType.get(type);
    }

    /**
     * Creates a new Set for insertion into the handler map.  This is provided
     * as an override point for subclasses. The returned set should support
     * concurrent access.
     *
     * @return a new, mutable set for handlers.
     */
    protected synchronized Set<EventHandler> newHandlerSet() {
        return new HashSet<EventHandler>();
    }

    /**
     * Flattens a class's type hierarchy into a set of Class objects.  The set
     * will include all superclasses (transitively), and all interfaces
     * implemented by these superclasses.
     *
     * @param concreteClass  class whose type hierarchy will be retrieved.
     * @return {@code clazz}'s complete type hierarchy, flattened and uniqued.
     */
    Set<Class<?>> flattenHierarchy(Class<?> concreteClass) {
        return flattenHierarchyCache.get(concreteClass);
    }

}
