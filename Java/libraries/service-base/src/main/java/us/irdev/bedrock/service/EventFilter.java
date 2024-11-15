package us.irdev.bedrock.service;

import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagObject;
import us.irdev.bedrock.secret.Secret;
import us.irdev.bedrock.logger.*;


import java.util.regex.Pattern;

public class EventFilter implements EventFilterHandler {
    private static final Logger log = LogManager.getLogger (EventFilter.class);
    /*
    Filter has:
    type (IP_ADDRESS_LIST, SECRET_LIST, AND_LIST, OR_LIST)
    event-list
     */
    public static final String FILTER_TYPE = "filter-type";

    // filter types
    public static final String IP_ADDRESS_LIST = "ip-address-list";
    public static final String SECRET_LIST = "secret-list";
    public static final String ALL_LIST = "all-list";
    public static final String ANY_LIST = "any-list";

    // general parms in filters
    public static final String EVENT_LIST = "event-list";
    public static final String WILDCARD = "*";
    public static final String SECRET = "secret";
    public static final String ALLOW = "allow";
    public static final String DENY = "deny";

    protected boolean checkEventList (Event event, BagObject filterConfiguration) {
        var eventList = filterConfiguration.getBagArray (EVENT_LIST);
        if (eventList != null) {
            var eventName = event.getEventName ();
            for (int i = 0, end = eventList.getCount (); i < end; ++i) {
                var eventMatch = eventList.getString (i);
                if (eventMatch.equals (eventName) || eventMatch.equals (WILDCARD)) {
                    // the event matched, so this filter applies
                    return true;
                }
            }
            // the event is not included in the list of events this filter applies to
            return false;
        }
        // no event was specified at all, so this filter applies to everything (wildcard)
        return true;
    }

    protected EventFilterResult checkIpAddressList (Event event, BagArray ipAddressList) {
        if (ipAddressList != null) {
            var eventIpAddress = event.getIpAddress();
            for (int i = 0, end = ipAddressList.getCount (); i < end; ++i) {
                var ipAddress = ipAddressList.getBagObject (i);
                if (ipAddress != null) {
                    // in practice, only allow or deny should be set, but both are possible - if we
                    // get an explicit match, it is definitive
                    var allow = ipAddress.getString (ALLOW);
                    if ((allow != null) && (Pattern.compile (allow).matcher (eventIpAddress).find ())) {
                        // this ip address matched an "allow" regex
                        return EventFilterResult.ALLOW;
                    }
                    var deny = ipAddress.getString (DENY);
                    if ((deny != null) && (Pattern.compile (deny).matcher (eventIpAddress).find ())) {
                        // this ip address mathed a "deny" regex
                        return EventFilterResult.DENY;
                    }
                }
            }
            // none of the ip address filters matched, fall through
        }
        // no filters were specified, or none applied to this event
        return EventFilterResult.NOT_APPLICABLE;
    }

    protected EventFilterResult checkSecretList (Event event, BagArray secretList) {
        if (secretList != null) {
            var secret = event.getQuery ().getString (SECRET);
            for (int i = 0, end = secretList.getCount (); i < end; ++i) {
                var secretRecipe = secretList.getBagObject(i);
                if (Secret.check(secret, secretRecipe)) {
                    // the secret matched, this filter allows the event
                    return EventFilterResult.ALLOW;
                }
            }
            // no screts matched, this filter denies the event
            return EventFilterResult.DENY;
        }
        // no secrets were specified...
        return EventFilterResult.NOT_APPLICABLE;
    }

    protected EventFilterResult checkAnyList (Event event, BagArray anyList) {
        if (anyList != null) {
            for (int i = 0, end = anyList.getCount (); i < end; ++i) {
                var anyItem = anyList.getBagObject(i);
                if (anyItem != null) {
                    var eventFilterResult = filterEvent (event, anyItem);
                    if (eventFilterResult != EventFilterResult.NOT_APPLICABLE) {
                        // any one of the child filters matched and returned a result, that's our result
                        return eventFilterResult;
                    }
                }
            }
            // none of the child filters matched, fall through
        }
        // no filters were specified, or none applied
        return EventFilterResult.NOT_APPLICABLE;
    }

    protected EventFilterResult checkAllList (Event event, BagArray allList) {
        //
        if (allList != null) {
            for (int i = 0, end = allList.getCount (); i < end; ++i) {
                var allItem = allList.getBagObject(i);
                if (allItem != null) {
                    var eventFilterResult = filterEvent (event, allItem);
                    if (eventFilterResult != EventFilterResult.ALLOW) {
                        // one of the child filters didn't match, we can exit early on that
                        return EventFilterResult.DENY;
                    }
                }
            }
            // all the child filters matched and allow
            return EventFilterResult.ALLOW;
        }
        // no filters were specified
        return EventFilterResult.NOT_APPLICABLE;
    }

    protected EventFilterResult filterEvent (Event event, BagObject filterConfiguration) {
        if (checkEventList (event, filterConfiguration)) {
            var filterType = filterConfiguration.getString(FILTER_TYPE);
            var filterParmsList = filterConfiguration.getBagArray (filterType);
            switch (filterType) {
                case IP_ADDRESS_LIST: return checkIpAddressList(event, filterParmsList);
                case SECRET_LIST: return checkSecretList(event, filterParmsList);
                case ALL_LIST: return checkAllList(event, filterParmsList);
                case ANY_LIST: return checkAnyList(event, filterParmsList);
                case ALLOW: return EventFilterResult.ALLOW;
            }
            // the type was unknown? fall through... (derived classes should do their work before calling the base class
            // to handle the filter)
            log.error("Unknown filter type '" + filterType + "'");
        }
        // no filters were specified
        return EventFilterResult.NOT_APPLICABLE;
    }

    @Override
    public boolean isAllowedEvent (Event event, BagObject filterConfiguration) {
        return (filterEvent (event, filterConfiguration) == EventFilterResult.ALLOW);
    }
}
