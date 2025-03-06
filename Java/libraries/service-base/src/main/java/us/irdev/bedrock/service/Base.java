package us.irdev.bedrock.service;

import us.irdev.bedrock.bag.*;
import us.irdev.bedrock.bag.formats.MimeType;
import org.apache.commons.io.input.ReversedLinesFileReader;
import us.irdev.bedrock.logger.*;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

public class Base extends HttpServlet {
    private static final Logger log = LogManager.getLogger (Base.class);

    public static final String CONTENT_TYPE = "Content-Type";
    public static final String DESCRIPTION = "description";
    public static final String DISPLAY_NAME = "display-name";
    public static final String ERROR = "error";
    public static final String EVENT = "event";
    public static final String EVENTS = "events";
    public static final String EXAMPLE = "example";
    public static final String HELP = "help";
    public static final String MULTIPLE = "multiple";
    public static final String NAME = "name";
    public static final String OK = "ok";
    public static final String PARAMETERS = "parameters";
    public static final String POM_NAME = "pom-name";
    public static final String POM_VERSION = "pom-version";
    public static final String QUERY = "query";
    public static final String REQUIRED = "required";
    public static final String RESPONSE = "response";
    public static final String RESPONSE_TIME_NS = "response-time-ns";
    public static final String SERVLET = "servlet";
    public static final String STATUS = "status";
    public static final String STRICT = "strict";
    public static final String VERSION = "version";
    public static final String SCHEMA = "schema";
    public static final String EVENT_FILTER = "event-filter";
    public static final String LOG_FILE = "log-file";
    public static final String TIMESTAMP = "timestamp";
    public static final String LEVEL = "level";
    public static final String METHOD = "method";
    public static final String MESSAGE = "message";
    public static final String LINE_COUNT = "line-count";
    public static final String LOCK = "lock";

    private static ServletContext context;
    private static boolean locked;

    public static ServletContext getContext () {
        return context;
    }

    public static Object getAttribute (String key) {
        return (context != null) ? context.getAttribute (key) : null;
    }

    public static Object setAttribute (String key, Object value) {
        if (context != null) {
            var oldValue = context.getAttribute (key);
            context.setAttribute (key, value);
            return oldValue;
        }
        return null;
    }

    public static String getBedrockVersion () {
        return Base.class.getPackage ().getImplementationVersion ();
    }

    private final Map<String, Handler> handlers = new HashMap<> ();

    private String configurationResourcePath = "/WEB-INF/configuration.json";
    private BagObject configuration;
    private BagObject schema;
    protected EventFilterHandler eventFilterHandler;

    protected BagObject getSchema () {
        // return a deep copy so the user can't accidentally modify it
        return new BagObject (schema);
    }

    protected BagObject getConfiguration () {
        // if we already have the configuration, return it
        if (configuration != null) {
            return configuration;
        }

        // otherwise, check to see if the servlet has been initialized
        if (context != null) {
            // try to load the configuration from the specified file resource
            var configurationPath = context.getRealPath (configurationResourcePath);
            log.info ("configuration path: " + configurationPath);
            configuration = BagObjectFrom.inputStream (context.getResourceAsStream (configurationResourcePath), BagObject::new);

            // common values for building the schema
            var help = Key.cat (EVENTS, HELP);
            var version = Key.cat (EVENTS, VERSION);

            // try to fetch the schema
            if ((schema = configuration.getBagObject (SCHEMA)) != null) {
                log.info ("configuration loaded: " + schema.getString(NAME));

                // remove the schema object from the configuration, so it is protected
                configuration.remove (SCHEMA);

                // add the default events

                // add a 'help' event if one isn't supplied
                if (! schema.has (help)) {
                    log.info ("installing default '" + help + "'");
                    schema.put (help, BagObjectFrom.resource (getClass (), "/help.json"));
                }

                // add a 'version' event if one isn't supplied
                if (! schema.has (version)) {
                    log.info ("installing default '" + version + "'");
                    schema.put (version, BagObjectFrom.resource (getClass (), "/version.json"));
                }

                // wire up the handlers specified in the schema - it is treated as authoritative so that
                // only specified events are exposed
                var eventNames = schema.getBagObject (EVENTS).keys ();
                for (var eventName : eventNames) {
                    install (eventName);
                }
            } else {
                // there is no schema, so report the warning
                log.warn ("Starting service with no schema.");

                // create a bootstrap schema and add the help and version descriptors
                schema = BagObjectFrom.resource (getClass (), "/bootstrap.json");
                schema.put (help, BagObjectFrom.resource (getClass (), "/help.json"));
                schema.put (version, BagObjectFrom.resource (getClass (), "/version.json"));

                // bootstrap/autowire... loop over all the methods that match the target signature
                // and install them as bootstraped generics
                var methods = this.getClass ().getMethods ();
                for (var method : methods) {
                    // if the method signature matches the event handler signature
                    if ((method.getName ().startsWith (Handler.HANDLER_PREFIX)) && (method.getParameterCount () == 1) && (method.getParameterTypes()[0] == Event.class)) {
                        // compute the event name (dash syntax), and install the handler
                        var elements = method.getName ().substring (Handler.HANDLER_PREFIX.length ()).split("(?=[A-Z])");
                        var eventName = Arrays.stream (elements).map (String::toLowerCase).collect(joining("-"));
                        install (eventName);

                        // add a default schema entry if one isn't in the bootstrap, so it passes
                        // basic validation
                        var schemaName = Key.cat (EVENTS, eventName);
                        if (!schema.has (schemaName)) {
                            log.info ("Adding bootstrap schema entry for '" + eventName + "'");
                            schema.put (schemaName, BagObject
                                    .open (DESCRIPTION, "Bootstrap " + method.getName ())
                                    .add (STRICT, false)
                            );
                        }
                    }
                }
            }

            // if the schema didn't supply a name, add one
            schema.put (NAME, getName ());

            // return the built configuration
            return configuration;
        } else {
            log.error ("Configuration requested before initialization.");
            return null;
        }
    }

    protected Base () {
    }

    protected Base (String configurationResourcePath) {
        this.configurationResourcePath = configurationResourcePath;
    }

    public String getName () {
        // if the configuration supplies a name,,,
        var name = (configuration != null) ? configuration.getString (NAME) : null;

        // or if the schema supplies a name...
        if ((name == null) && (schema != null)) {
            name = schema.getString (NAME);
        }

        // or if the POM has a name
        if (name == null) {
            name = getClass ().getPackage ().getImplementationTitle ();
        }

        // or if the web context supplies a name... this should be the last resort
        if ((name == null) && (context != null) && (context.getServletContextName () != null)) {
            name = context.getServletContextName ();
        }

        return (name != null) ? name : "[UNNAMED]";
    }

    public String getVersion () {
        return getClass ().getPackage ().getImplementationVersion ();
    }

    @Override
    public void init (ServletConfig config) throws ServletException {
        super.init (config);
        context = config.getServletContext ();
        locked = false;
        log.info ("STARTING " + getName () + " v." + getVersion () + " with Bedrock v." + getBedrockVersion ());
        setAttribute (SERVLET, this);

        // configure the application
        getConfiguration ();

        // if there is a filter setup in the configuration, add a filter handler
        if (configuration.has(EVENT_FILTER)) {
            eventFilterHandler = new EventFilter();
        }
    }

    @Override
    public void destroy () {
        super.destroy ();
        log.debug (getName () + " DESTROYING...");
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getMethod().equals("API")){
            // XXX we will eventually migrate away from this to only support the API call within the server, post will
            // XXX be deprecated, but we will provide support JavaScript code to call these APIs as well.
            doPost(request, response);
        } else super.service(request, response);
    }

    @Override
    public void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException {
        // a base get should be equivalent to a help request - we want to support a standard way of interfacing with the
        // bedrock servers so naive users will get an educational response
        var query = new BagObject().put(EVENT, HELP);
        var event = handleEvent (query, request);
        finishRequest (event, response);
    }

    @Override
    public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException {
        // get the request data type, then tease out the response type (use a default if it's not present) and the
        // charset (if given, otherwise default to UTF-8, because that's what it will be in Java)
        var mimeType = MimeType.DEFAULT;
        var contentTypeHeader = request.getHeader (CONTENT_TYPE);
        if (contentTypeHeader != null) {
            var contentType = contentTypeHeader.replace (" ", "").split (";");
            mimeType = contentType[0];
            log.debug ("'Content-Type' is (" + mimeType + ")");
        } else {
            log.warn ("'Content-Type' is not set by the requestor, using default (" + mimeType + ")");
        }

        // extract the bedrock data that's been posted, we require that it's a JSON object
        log.debug ("Extract POST data for (" + mimeType + ") on " + request.getQueryString());
        var sourceAdapter = new SourceAdapterReader(request.getInputStream (), mimeType);
        var requestString = sourceAdapter.getStringData ();
        var query = BagObjectFrom.string (requestString, mimeType);
        var event = (query != null) ? handleEvent (query, request) : errorOnRequest ("Invalid or empty POST data.", request);
        finishRequest (event, response);
    }

    private void addCorsHeaders (HttpServletResponse response) {
        // the base CORS headers
        response.setHeader ("Access-Control-Allow-Origin", "*");
        response.setHeader ("Access-Control-Allow-Headers", "*");
        response.setHeader ("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
    }

    @Override
    public void doOptions (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        addCorsHeaders (response);
        super.doOptions (request, response);
    }

    private void finishRequest (Event event, HttpServletResponse response) throws IOException {
        var UTF_8 = StandardCharsets.UTF_8.name ();
        response.setContentType (MimeType.JSON + "; charset=" + UTF_8);
        response.setCharacterEncoding (UTF_8);

        // tell the browsers we know what we are returning (JSON is "protected")
        response.setHeader ("X-Content-Type-Options", "nosniff");
        addCorsHeaders (response);

        var out = response.getWriter ();
        out.println (event.getResponse ().toString (MimeType.JSON));
        out.close ();
    }

    private Event errorOnRequest (String errorString, HttpServletRequest request) {
        var query = BagObjectFrom.string (request.getQueryString (), MimeType.URL, BagObject::new);
        return new Event (query, request).error(errorString);
    }

    private void validateParameters (BagObject query, boolean strict, BagObject parameterSpecification, BagArray validationErrors) {
        if (strict) {
            // loop over the query parameters to be sure they are all valid
            var queryParameters = query.keys ();
            for (var queryParameter : queryParameters) {
                if (!queryParameter.equals(EVENT)) {
                    if ((parameterSpecification == null) || (!parameterSpecification.has(queryParameter))) {
                        validationErrors.add("Unspecified parameter: '" + queryParameter + "'");
                    }
                }
            }
        }

        // loop over the parameter specification to be sure all the required ones are present
        if (parameterSpecification != null) {
            var expectedParameters = parameterSpecification.keys ();
            for (var expectedParameter : expectedParameters) {
                if (parameterSpecification.getBoolean(Key.cat(expectedParameter, REQUIRED), () -> false)) {
                    if (!query.has(expectedParameter)) {
                        validationErrors.add("Missing required parameter: '" + expectedParameter + "'");
                    }
                }
            }
        }
    }

    private Event handleEvent (BagObject query, HttpServletRequest request) {
        var event = new Event (query, request);
        if (! locked) {
            if (schema != null) {
                // create the event object around the request parameters, and validate it is a known
                // event
                var eventName = event.getEventName ();
                if (eventName != null) {
                    var eventSpecification = schema.getBagObject (Key.cat (EVENTS, eventName));
                    if (eventSpecification != null) {
                        // validate the query parameters
                        var parameterSpecification = eventSpecification.getBagObject (PARAMETERS);
                        var strict = eventSpecification.getBoolean (STRICT, () -> true);
                        var validationErrors = new BagArray ();
                        validateParameters (query, strict, parameterSpecification, validationErrors);

                        // if the validation passed
                        if (validationErrors.getCount () == 0) {
                            // give an opportunity to filter the event before it happens
                            if ((eventFilterHandler == null) || (eventFilterHandler.isAllowedEvent (event, configuration.getBagObject (EVENT_FILTER)))) {
                                // get the handler, and try to take care of business...
                                var handler = handlers.get (eventName);
                                if (handler != null) {
                                    // finally, do your business
                                    handler.handle (event);
                                } else {
                                    event.error ("No handler installed for '" + EVENT + "' (" + eventName + ")");
                                }
                            } else {
                                event.error ("'" + EVENT + "' (" + eventName + ") is not allowed");
                            }
                        } else {
                            event.error (validationErrors);
                        }
                    } else {
                        event.error ("Unknown '" + EVENT + "' (" + eventName + ")");
                    }
                } else {
                    event.error ("Missing '" + EVENT + "'");
                }
            } else {
                // XXX what are the circumstances under which this happens? I ask because it shouldn't
                event.error ("Missing API");
            }
        } else {
            event.error ("Instance locked");
        }
        return event;
    }

    public boolean install (String eventName) {
        try {
            var handler = new Handler (eventName, this);
            handlers.put (handler.getEventName (), handler);
            log.info ("Installed handler '" + handler.getMethodName () + "' for '" + eventName + "'");
            return true;
        } catch (NoSuchMethodException exception) {
            log.error ("Install '" + EVENT + "' failed for (" + eventName + ")", exception);
            return false;
        }
    }

    // default handlers
    public void handleEventOk (Event event) {
        event.ok ();
    }

    public void handleEventLock (Event event) {
        locked = true;
        event.ok ();
    }

    public void handleEventHelp (Event event) {
        event.ok (schema);
    }

    public void handleEventVersion (Event event) {
        event.ok (BagObject
                .open (POM_VERSION, getVersion ())
                .put (DISPLAY_NAME, getName ())
        );
    }

    public static String escapeLine (String line) {
        return line
                .replace ("\\", "\\\\")
                .replace ("\n", "\\n")
                .replace ("\r", "\\r")
                .replace ("\f", "\\f")
                .replace ("\t", "\\t")
                .replace ("\b", "\\b")
                .replace ("\"", "\\\"");
    }

    protected String unbox(String input) {
        var pattern = Pattern.compile("^[\\[(]([^])]+)");
        var matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return input;
    }

    public void handleEventLogFile (Event event) throws IOException {
        var logFile = configuration.getString(LOG_FILE, () -> context.getRealPath(File.separator).replaceFirst("webapps.*", "logs/catalina.out"));
        try (var reader = ReversedLinesFileReader.builder()
                .setFile(new File(logFile))
                .setCharset(UTF_8)
                .get()) {
            var validLevels = Set.of("INFO", "WARNING", "DEBUG", "TRACE", "ERROR");
            var regexTimeStamp = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+Z");

            var nLines = event.getQuery().getInteger(LINE_COUNT, () -> 100);
            var end = Base.class.getCanonicalName() + ":init";
            var result = new BagArray();
            var line = reader.readLine();
            while ((line != null) && (result.getCount() < nLines))  {
                if (regexTimeStamp.matcher(line).find()) {
                    // 2025-01-13T17:08:34.116384214Z http-nio-8080-exec-3 INFO Starting configuration XmlConfiguration[location=/usr/local/tomcat/webapps/ROOT/WEB-INF/classes/log4j2.xml, lastModified=2024-12-07T15:26:56Z]...
                    var array = line.split(" ", 4);
                    if (array.length == 4) {
                        var level = unbox(array[2]);
                        if (validLevels.contains(level)) {
                            result.add(BagObject
                                    .open(TIMESTAMP, array[0])
                                    .put(LEVEL, level)
                                    .put(METHOD, array[1])
                                    .put(MESSAGE, escapeLine(array[3]))
                            );
                        }
                    }
                } else {
                    // 2025-01-13 17:08:33.934 [INFO] (us.irdev.bedrock.service.Base:install) Installed handler 'handleEventUserGet' for 'user-get'
                    var array = line.split(" ", 5);
                    if (array.length == 5) {
                        // vet the resulting array for valid text - check that level is a valid log level
                        var level = unbox(array[2]);
                        if (validLevels.contains(level)) {
                            var method = unbox(array[3]);
                            result.add(BagObject
                                    .open(TIMESTAMP, array[0] + " " + array[1])
                                    .put(LEVEL, level)
                                    .put(METHOD, method)
                                    .put(MESSAGE, escapeLine(array[4]))
                            );

                            // stop after the servlet initialization...
                            if (method.equals(end)) {
                                break;
                            }
                        }
                    }
                }
                line = reader.readLine();
            }
            event.ok(result);
        }
        // let the parent handler deal with an exception
    }

    public void handleEventMultiple (Event event) {
        var eventsArray = event.getQuery ().getBagArray (EVENTS);
        if (eventsArray != null) {
            var eventCount = eventsArray.getCount ();
            var results = new BagArray (eventCount);
            for (int i = 0; i < eventCount; ++i) {
                var subEvent = handleEvent (eventsArray.getBagObject (i), event.getRequest ());
                results.add (subEvent.getResponse ());
            }
            event.ok (results);
        } else {
            event.error ("No events found (expected an array in '" + EVENTS + "')");
        }
    }
}
