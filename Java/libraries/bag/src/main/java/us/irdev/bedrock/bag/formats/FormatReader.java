package us.irdev.bedrock.bag.formats;

import us.irdev.bedrock.bag.BagArray;
import us.irdev.bedrock.bag.BagObject;
import us.irdev.bedrock.bag.SourceAdapter;
import us.irdev.bedrock.logger.LogManager;
import us.irdev.bedrock.logger.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FormatReader {
    private static final Logger log = LogManager.getLogger (FormatReader.class);

    protected final char[] input;

    protected FormatReader () {
        this (null);
    }

    /**
     *
     * @param input
     */
    public FormatReader (String input) {
        this.input = (input != null) ? input.toCharArray() : null;
    }

    // static type registration by name
    private static final Map<String, Function<String, FormatReader>> formatReaders = new HashMap<> ();

    /**
     *
     * @param mimeType
     * @param replace
     * @param factory
     */
    public static void registerFormatReader (String mimeType, boolean replace, Function<String, FormatReader> factory) {
        // try to find the mime type first, and if it's not there, add it
        var foundMimeType = MimeType.getFromMimeType (mimeType, () -> MimeType.addMimeTypeMapping (mimeType));
        if ((! replace) || (! formatReaders.containsKey(foundMimeType))) {
            formatReaders.put(foundMimeType, factory);
        }
    }

    private static FormatReader getFormatReader (String stringData, String mimeType, Class iType) {
        // deduce the format, and create the format reader
        var foundMimeType = MimeType.getFromMimeType (mimeType);
        if (foundMimeType != null) {
            var formatReader = formatReaders.get(foundMimeType).apply (stringData);
            if (formatReader != null) {
                if (iType.isInstance (formatReader)) {
                    return formatReader;
                } else {
                    log.error ("Reader for format (" + mimeType + ") doesn't implement " + iType.getName ());
                }
            } else {
                log.error ("No reader for format (" + mimeType + ")");
            }
        } else {
            log.error ("Unknown format (" + mimeType + ")");
        }
        return null;
    }

    /**
     *
     * @param sourceAdapter
     * @return
     */
    public static BagArray readBagArray (SourceAdapter sourceAdapter) {
        var formatReader = getFormatReader(sourceAdapter.getStringData(), sourceAdapter.getMimeType(), ArrayFormatReader.class);
        return (formatReader != null) ? ((ArrayFormatReader)formatReader).readBagArray () : null;
    }

    /**
     *
     * @param sourceAdapter
     * @return
     */
    public static BagObject readBagObject (SourceAdapter sourceAdapter) {
        var formatReader = getFormatReader(sourceAdapter.getStringData(), sourceAdapter.getMimeType(), ObjectFormatReader.class);
        return (formatReader != null) ? ((ObjectFormatReader)formatReader).readBagObject () : null;
    }

    /**
     * static method to forcibly invoke the static initializer
     */
    public static void register () {
    }

    static {
        // rather than have a compile-time and run-time dependency, we just list the subclasses of
        // FormatReader here that need to be loaded. this is just to ensure the types are linked for
        // future use...
        var formatReaders = new Class[] {
                FormatReaderComposite.class,
                FormatReaderTableAdapter.class,
                FormatReaderJson.class,
                FormatReaderTable.class,
                FormatReaderXml.class
        };
        for (var type : formatReaders) {
            try {
                type.getDeclaredConstructor ().newInstance ();
            } catch (IllegalAccessException exception) {
                // do nothing
            } catch (InvocationTargetException exception) {
                log.error ("InvocationTargetException: " + type.getName() + " (" + exception.getCause().getMessage() + ")");
            } catch (InstantiationException | NoSuchMethodException | SecurityException exception) {
                log.error ("static error: " + type.getName() + " (" + exception.toString() + ")");
            }
        }
    }
}
