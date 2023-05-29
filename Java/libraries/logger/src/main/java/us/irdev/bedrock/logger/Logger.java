package us.irdev.bedrock.logger;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private final String name;
    private Level level;
    private String configuration;
    private final PrintStream out;
    private Date date;

    public Logger (String name, Level level, String configuration, PrintStream out) {
        this.name = name;
        this.level = level;
        this.configuration = configuration;
        this.out = out;
    }

    public String getName() { return name; }

    public Level getLevel () { return level; }
    public Logger setLevel (Level level) { this.level = level; return this; }

    public String getConfiguration () { return configuration; }
    public Logger setConfiguration (String configuration) { this.configuration = configuration; return this; }

    public Date getDate () { return date; }

    private Logger logInternal (Level logLevel, String message) {
        if (logLevel.getLevel() >= level.getLevel()) {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            StackTraceElement caller = stackTrace[3];
            // TODO add some formatting from 'configuration', with the level and all that jazz
            date = new Date ();
            out.format("%s [%s] (%s:%s) %s%s", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(date), logLevel.getName(), caller.getClassName(), caller.getMethodName(), message, System.lineSeparator());
        }
        return this;
    }

    public Logger log (Level logLevel, String message) {
        return logInternal (logLevel, message);
    }

    public Logger trace (String message) { return logInternal (Level.TRACE, message); }
    public Logger debug (String message) { return logInternal (Level.DEBUG, message); }
    public Logger info (String message) { return logInternal (Level.INFO, message); }
    public Logger warn (String message) { return logInternal (Level.WARN, message); }
    public Logger error (String message) { return logInternal (Level.ERROR, message); }

    public Logger trace (Throwable cause) { return logInternal (Level.TRACE, cause.getMessage()); }
    public Logger debug (Throwable cause) { return logInternal (Level.DEBUG, cause.getMessage()); }
    public Logger info (Throwable cause) { return logInternal (Level.INFO, cause.getMessage()); }
    public Logger warn (Throwable cause) { return logInternal (Level.WARN, cause.getMessage()); }
    public Logger error (Throwable cause) { return logInternal (Level.ERROR, cause.getMessage()); }

    public Logger error (String message, Throwable cause) {
        // TODO do something with the exception (stack trace, etc.)
        return logInternal (Level.ERROR, message);
    }

}
