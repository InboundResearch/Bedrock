package us.irdev.bedrock.bag.formats;

import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagObject;
import us.irdev.bedrock.logger.LogManager;
import us.irdev.bedrock.logger.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

abstract public class FormatWriter {
    private static final Logger log = LogManager.getLogger (FormatReader.class);

    protected static final String[] QUOTES = { "\"" };

    protected String enclose (String input, String[] bracket) {
        var bracket0 = bracket[0];
        var bracket1 = (bracket.length > 1) ? bracket[1] : bracket0;
        return bracket0 + input + bracket1;
    }

    protected String quote (String input) {
        return enclose (input, QUOTES);
    }

    abstract public String write (BagObject bagObject);
    abstract public String write (BagArray bagArray);

    // static type registration by name
    private static final Map<String, FormatWriter> formatWriters = new HashMap<>();

    public static void registerFormatWriter (String format, boolean replace, Supplier<FormatWriter> supplier) {
        if ((! replace) || (! formatWriters.containsKey(format))) {
            formatWriters.put(format, supplier.get());
        }
    }

    public static String write (BagObject bagObject, String format) {
        if (formatWriters.containsKey(format)) {
            return formatWriters.get(format).write (bagObject);
        }
        return null;
    }

    public static String write (BagArray bagArray, String format) {
        if (formatWriters.containsKey(format)) {
            return formatWriters.get(format).write (bagArray);
        }
        return null;
    }

    static {
        // rather than have a compile-time and run-time dependency, we just list the sub-
        // classes of FormatWriter here that need to be loaded.
        var formatWriters = new Class[] {
                FormatWriterJson.class,
                FormatWriterText.class
        };
        for (var type : formatWriters) {
            try {
                type.getConstructor().newInstance ();
            } catch (IllegalAccessException exception) {
                // do nothing
            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException exception) {
                log.error (exception);
            }
        }
    }
}
