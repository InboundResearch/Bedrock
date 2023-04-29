package us.irdev.bedrock.service;

import us.irdev.bedrock.bag.Bag;
import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagObject;
import us.irdev.bedrock.logger.*;


import javax.servlet.http.HttpServletRequest;

import static us.irdev.bedrock.service.Base.*;

public class Event {
    private static final Logger log = LogManager.getLogger (Event.class);

    private final long startTimeNano;
    private final BagObject query;
    private final HttpServletRequest request;
    private Bag response;

    public Event (BagObject query, HttpServletRequest request) {
        this.query = query;
        this.request = request;
        startTimeNano = System.nanoTime();
    }

    public BagObject getQuery () {
        return query;
    }

    public HttpServletRequest getRequest () {
        return request;
    }

    public Bag getResponse () {
        return response;
    }

    public String getEventName () {
        return query.getString (EVENT);
    }

    public String getIpAddress () {
        var ipAddress = request.getRemoteAddr ();

        // try to get the x-forwarded header, the last one...
        var forwarding = request.getHeader ("x-forwarded-for");
        if (forwarding != null) {
            var forwards = forwarding.split (",");
            for (var forward : forwards) {
                forward = forward.trim ();
                ipAddress = forward.split (":")[0];
            }
        }

        return ipAddress;
    }


    public Event respond (Bag bag) {
        response = bag;
        return this;
    }

    private Event respond (String status, String responseKey, Bag responseValue) {
        return respond (BagObject
                .open (QUERY, query)
                .put (STATUS, status)
                .put (responseKey, responseValue)
                .put (RESPONSE_TIME_NS, System.nanoTime() - startTimeNano)
        );
    }

    public Event ok (Bag bag) {
        return respond (OK, RESPONSE, bag);
    }

    public Event ok () {
        return ok (null);
    }

    public Event error (BagArray errors) {
        // log the errors
        for (int i = 0, end = errors.getCount (); i < end; ++i) {
            log.error (errors.getString (i));
        }

        // and respond to the end user...
        return respond (ERROR, ERROR, errors);
    }

    public Event error (String error) {
        return error (BagArray.open (error));
    }
}
