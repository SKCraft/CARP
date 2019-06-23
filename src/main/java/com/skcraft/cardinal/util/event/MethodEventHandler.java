package com.skcraft.cardinal.util.event;

import java.lang.reflect.Method;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Invokes a {@link Method} to dispatch an event.
 */
public class MethodEventHandler extends EventHandler {

    private final Object object;
    private final Method method;

    /**
     * Create a new event handler.
     *
     * @param priority the priority
     * @param ignoredCancelled whether to ignore cancelled events
     * @param method the method
     */
    public MethodEventHandler(Priority priority, boolean ignoredCancelled, Object object, Method method) {
        super(priority, ignoredCancelled);
        checkNotNull(method);
        this.object = object;
        this.method = method;
    }

    /**
     * Get the method.
     *
     * @return the method
     */
    public Method getMethod() {
        return method;
    }

    @Override
    public void dispatch(Object event) throws Exception {
        method.invoke(object, event);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodEventHandler that = (MethodEventHandler) o;

        if (!method.equals(that.method)) return false;
        if (object != null ? !object.equals(that.object) : that.object != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = object != null ? object.hashCode() : 0;
        result = 31 * result + method.hashCode();
        return result;
    }
}
