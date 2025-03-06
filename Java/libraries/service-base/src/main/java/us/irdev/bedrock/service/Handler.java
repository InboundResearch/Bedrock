package us.irdev.bedrock.service;

import us.irdev.bedrock.logger.*;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Handler {
    private static final Logger log = LogManager.getLogger (Handler.class);

    public static final String HANDLER_PREFIX = "handleEvent";

    private final Object container;
    private final Method method;
    private final String eventName;

    public Handler (String eventName, Object container) throws NoSuchMethodException {
        this.container = container;
        this.eventName = eventName;

        // construct the method name, and look it up in the container
        int dash;
        var methodName = HANDLER_PREFIX + "-" + eventName;
        while ((dash = methodName.indexOf ('-')) >= 0) {
            int skip = dash + 1, end = skip + 1;
            methodName = methodName.substring (0, dash) + methodName.substring (skip, end).toUpperCase () + methodName.substring (end);
        }

        // this might fail for a variety of reasons, including the method is present but not public
        method = container.getClass ().getMethod (methodName, Event.class);
    }

    public String getEventName () {
        return eventName;
    }

    public void handle (Event event) {
        log.info (eventName);
        try {
            method.invoke (container, event);
        } catch (IllegalAccessException exception) {
            // this will never happen. the handler installation fails if the method is not public,
            // so it will never be installed if this would happen, and therefore never have the
            // opportunity to throw this exception. it is included here because the code won't
            // compile without it.
            // event.error (exception.toString ());
        } catch (InvocationTargetException exception) {
            var cause = exception.getCause ();
            // escape the error string in case it has quotes
            event.error (Base.escapeLine (cause.toString ()));
            log.error (method.getName () + " failed", cause);
        }
    }

    public String getMethodName () {
        return method.getName ();
    }
}
